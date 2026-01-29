package app.ecams.request.checkoutcnl.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import app.core.exception.RollBackException;
import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileDataList;
import app.core.proto.ProtoEcams.RequestInfo;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.commoncode.service.ICommonCodeService;
import app.ecams.file.dao.IFileDAO;
import app.ecams.job.dao.IJobDAO;
import app.ecams.job.model.Job;
import app.ecams.request.autoseq.service.IAutoSeqService;
import app.ecams.request.checkin.dao.ICheckInDAO;
import app.ecams.request.checkoutcnl.dao.ICheckOutCnlDAO;
import app.ecams.request.confirm.service.IConfirmService;
import app.ecams.request.service.RequestService;
import app.ecams.system.service.ISystemService;
import app.ecams.user.service.IUserService;

@Service
public class CheckOutCnlRequestService extends RequestService implements ICheckOutCnlRequestService{
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private ISystemService systemService;
	@Autowired private IUserService userService;
	@Autowired private ICommonCodeService commonCodeService;
	@Autowired private IAutoSeqService autoSeqService;
	@Autowired private IConfirmService confirmService;
	@Autowired private PlatformTransactionManager txManager;
	@Autowired private DefaultTransactionDefinition defaultTransactionDefinition;
	@Autowired private ICheckOutCnlDAO checkOutCnlDAO;
	@Autowired private IJobDAO jobDAO;
	@Autowired private IFileDAO fileDAO;
	
	@Override
	@Transactional (readOnly = false, propagation = Propagation.REQUIRED, rollbackForClassName={"app.core.exception.RollBackException"})
	public ReturnMsg request(EcamsMessage ecamsmsg) {
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		TransactionStatus status = txManager.getTransaction(defaultTransactionDefinition);
		
		HashMap param = null;
		
		try{			
			/*
			commonSettings cs = new commonSettings();
			
			String basePackage = cs.getBasePackage();
			String baseJSDir = cs.getBaseJSDir();
			String baseJSName = cs.getBaseJSName();
			String baseJobCd = cs.getBaseJobCd();
			String jobCd = "";
			*/
			
			int i,datalength;
			int seq=0;
			String acptNo= null;
			String errStr;
			
			FileDataList fileDataList = ecamsmsg.getFiledatalist();
			HashMap sysinfoMap = systemService.getSysInfo_detail(ecamsmsg.getSysinfo().getSyscd());
			if(sysinfoMap==null){
				throw new Exception("시스템정보 오류입니다.");
			}
			datalength = fileDataList.getFiledatasCount();
			
			int k=0;
			for (i=0;i<datalength;i++){
				if(fileDataList.getFiledatas(i).getItemid() != null && !fileDataList.getFiledatas(i).getItemid().equals("")){
					++k;
				}
			}
			datalength = k;
			
			if(datalength == 0){
				logger.error("CheckOutCnlRequestService 75");
				throw new Exception("취소 할 데이터가 없습니다.");
			}


			String srid = null;
			if(ecamsmsg.hasSrinfo()){
				srid = ecamsmsg.getSrinfo().getCcSRId();
			}
			
			errStr = checkFilesStatus(fileDataList,sysinfoMap,ecamsmsg.getRequestinfo().getQrycd(),ecamsmsg.getUserinfo().getId(), srid);
			
			if (errStr != null){
				throw new Exception(errStr);
			}
			
			String strTeam = userService.getUserInfo(ecamsmsg.getUserinfo().getId()).getCm_project();
			if (strTeam == null){
				throw new Exception("사용자 팀 정보 오류");
			}
			/*String strRequest = commonCodeService.getCode("REQUEST", ecamsmsg.getRequestinfo().getQrycd()).getCodename();
			if (strRequest == null){
				throw new Exception("신청구분 오류");
			}*/

			acptNo = autoSeqService.getAcptNo(ecamsmsg.getRequestinfo().getQrycd(), 1);

			boolean Resout = acptnoCheck(acptNo);
			if(!Resout){
				throw new Exception("신청번호 채번 중 오류가 발생했습니다.");
			}
			
			boolean cmr1000Flg = true;
			
			for (i=0;i<datalength;i++){
				if(fileDataList.getFiledatas(i).getItemid() == null || fileDataList.getFiledatas(i).getItemid().equals("")){
					continue;
				}
				HashMap params = new HashMap();
				params.put("CM_SYSCD", ecamsmsg.getSysinfo().getSyscd());
				params.put("CM_USERID", ecamsmsg.getUserinfo().getId());
				
				List<Job> jobList = jobDAO.getJobInfo(params);
				
        		
				boolean hasJobcd = false;
				for(int j=0; j<jobList.size(); j++) {
					if( ecamsmsg.getJobinfolist().getJobinfo(i).getJobcd().equals(jobList.get(j).getJobcd()) ) {
					//if( jobCd.equals(jobList.get(j).getJobcd()) ) {
						hasJobcd = true;
						break;
					}
				}
				
				if( !hasJobcd ) {
					logger.error("Not Found JobCd ["+ecamsmsg.getJobinfolist().getJobinfo(i)+"] ["+fileDataList.getFiledatas(i).getFilename()+"]");
					returnmsg_builder.setReturnval(1);
					returnmsg_builder.setReturnStr("message:"+"등록되지 않은 업무코드 입니다.");
					return returnmsg_builder.build();
				}   
				
        		if (cmr1000Flg) {
        			if (i>=300) {
        				if (!confirmService.request_confirm(acptNo, ecamsmsg.getSysinfo().getSyscd(), ecamsmsg.getRequestinfo().getQrycd(), ecamsmsg.getUserinfo().getId(), null)){
        					logger.error("CheckOutCnlRequestService 150:"+acptNo);
        					throw new Exception("결재정보등록 중 오류가 발생하였습니다. 관리자에게 연락하여 주시기 바랍니다.");
        				}
        			}
        			param = new HashMap();
        			param.put("CR_ACPTNO", acptNo);
        			param.put("CR_SYSCD", ecamsmsg.getSysinfo().getSyscd());
        			param.put("CR_SYSGB", (String)sysinfoMap.get("cm_sysgb"));
        			param.put("CR_JOBCD", fileDataList.getFiledatas(i).getJobinfo().getJobcd());
        			param.put("CR_STATUS", "0");
        			param.put("CR_TEAMCD", strTeam);
        			param.put("CR_QRYCD", ecamsmsg.getRequestinfo().getQrycd());
        			param.put("CR_PASSOK", "0");
        			//param.put("CR_PASSCD", strRequest);
        			if(null == srid){
            			param.put("CR_PASSCD", "이클립스 체크아웃취소");
        			}else{
            			param.put("CR_PASSCD", "["+ecamsmsg.getSrinfo().getCcSRId()+"]"+ecamsmsg.getSrinfo().getCcTitle());
            			param.put("CR_ITSMID", ecamsmsg.getSrinfo().getCcSRId());
            			param.put("CR_ITSMTITLE", ecamsmsg.getSrinfo().getCcTitle());
        			}
        			param.put("CR_EDITOR", ecamsmsg.getUserinfo().getId());
        			param.put("CR_SAYUCD", "9");
        			
        			param.put("CR_BEFJOB", "N");
        			param.put("CR_SVRYN", "N");
        			
					if (insertCmr1000(param) <= 0){
        				throw new Exception("Cmr1000 Insert Error");
        			}
        			param = null;
        			
        			seq = 0;
        			
        			cmr1000Flg = false;
        		}
        		
				HashMap fileinfo=null;
        		fileinfo = fileService.getFileInfo(fileDataList.getFiledatas(i).getItemid());
        		if (fileinfo == null){
        			throw new Exception("fileinfo get error - itemid=["+fileDataList.getFiledatas(i).getItemid()+"]");
        		}
        		
        		param = new HashMap();
    			param.put("CR_ACPTNO", acptNo);
    			param.put("CR_SERNO", ++seq);
    			param.put("CR_SYSCD", ecamsmsg.getSysinfo().getSyscd());
    			param.put("CR_SYSGB", (String)sysinfoMap.get("cm_sysgb"));
    			param.put("CR_JOBCD", (String) fileinfo.get("CR_JOBCD"));
    			param.put("CR_STATUS", "0");
    			param.put("CR_QRYCD", ecamsmsg.getRequestinfo().getQrycd());
    			param.put("CR_RSRCCD", (String) fileinfo.get("CR_RSRCCD"));
    			param.put("CR_LANGCD", (String) fileinfo.get("CR_LANGCD"));
    			param.put("CR_DSNCD",  (String) fileinfo.get("CR_DSNCD"));
    			param.put("CR_RSRCNAME",  (String) fileinfo.get("CR_RSRCNAME"));
    			param.put("CR_RSRCNAM2",  (String) fileinfo.get("CR_RSRCNAME"));
    			
    			param.put("CR_CHGCD", "0");
    			param.put("CR_TSTCHG", "0");    			
    			param.put("CR_VERSION", ((BigDecimal)fileinfo.get("CR_LSTVER")).intValue());
    			
    			param.put("CR_EDITOR", ecamsmsg.getUserinfo().getId());
    			
    			param.put("CR_BASEITEM", fileDataList.getFiledatas(i).getItemid());
    			param.put("CR_ITEMID", fileDataList.getFiledatas(i).getItemid());
    			
				param.put("CR_BEFVIEWVER", (String)fileinfo.get("CR_VIEWVER"));
				param.put("CR_AFTVIEWVER", (String)fileinfo.get("CR_VIEWVER"));
				
    			if (insertCmr1010(param)<=0){
    				throw new Exception("Cmr1010 Insert Error");
    			}

    			param = new HashMap();
    			param.put("acptno",acptNo);
    			List result = updateConfCmr1010(param);
    			
    			if(result == null){
    				throw new Exception("updateCmr1010 오류");
    			}
				
//    			param = null;
//    			
//    			param = new HashMap();
//				param.put("SET_CR_STATUS", "6");
//    			param.put("SET_CR_SYSDATE","111");
//    			param.put("SET_CR_EDITOR", ecamsmsg.getUserinfo().getId());
//    			param.put("PARAM_CR_ITEMID", fileDataList.getFiledatas(i).getItemid());
//    			if (fileService.updateStatus(param) <= 0){
//    				throw new Exception("cmr0020 update Error");
//    			}
//    			
//				param = null; 
    			fileinfo = null; 
			}
			
			param = new HashMap();
			param.put("acptno",acptNo);
			List result = updateConfCmr1010(param);
			
			if(result == null){
				throw new Exception("updateCmr1010 오류");
			}
			
			param=null;
			
			if (!confirmService.request_confirm(acptNo, ecamsmsg.getSysinfo().getSyscd(), ecamsmsg.getRequestinfo().getQrycd(), ecamsmsg.getUserinfo().getId(), null)){
				logger.error("CheckOutCnlRequestService 262:"+acptNo);
				throw new Exception("결재정보등록 중 오류가 발생하였습니다. 관리자에게 연락하여 주시기 바랍니다.");
			}
			
			// TODO Auto-generated method stub
			returnmsg_builder.setReturnval(0);
			returnmsg_builder.setReturnStr(acptNo);
			
			txManager.commit(status);
		}
		catch (RollBackException e){
			logger.error("RequestCheckOutCnl.request: "+ecamsmsg.getUserinfo().getId()+" "+e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr(e.getError());
			txManager.rollback(status);
		}
		catch (Exception e){
			logger.error("RequestCheckOutCnl.request: "+ecamsmsg.getUserinfo().getId()+" "+e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr(e.getMessage());
		}		
		return returnmsg_builder.build();
	}

	public String selectCmr1010_acptno(HashMap param) {
		// TODO Auto-generated method stub
		return checkOutCnlDAO.selectCmr1010_acptno(param);
	}
	
	public int updateCmr1010_confno(String acptno){
		return checkOutCnlDAO.updateCmr1010_confno(acptno);
	}
}
