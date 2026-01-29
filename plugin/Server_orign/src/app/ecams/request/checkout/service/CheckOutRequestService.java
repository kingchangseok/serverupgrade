package app.ecams.request.checkout.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import app.core.exception.RollBackException;
import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileDataList;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.commoncode.service.ICommonCodeService;
import app.ecams.job.dao.IJobDAO;
import app.ecams.job.model.Job;
import app.ecams.request.autoseq.service.IAutoSeqService;
import app.ecams.request.checkin.dao.ICheckInDAO;
import app.ecams.request.confirm.service.IConfirmService;
import app.ecams.request.service.RequestService;
import app.ecams.srjob.dao.ISRJobDAO;
import app.ecams.system.service.ISystemService;
import app.ecams.user.service.IUserService;

@Service
public class CheckOutRequestService extends RequestService implements ICheckOutRequestService{
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private ISystemService systemService;
	@Autowired private IUserService userService;
	@Autowired private IAutoSeqService autoSeqService;
	@Autowired private IConfirmService confirmService;
	@Autowired private PlatformTransactionManager txManager;
	@Autowired private DefaultTransactionDefinition defaultTransactionDefinition;
	@Autowired private IJobDAO jobDAO;
	@Autowired private ICheckInDAO checkInDAO;	
	
	@Transactional (readOnly = false, propagation = Propagation.REQUIRED, rollbackForClassName={"app.core.exception.RollBackException"})
	public ReturnMsg request(EcamsMessage ecamsmsg) {
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();

		TransactionStatus status = txManager.getTransaction(defaultTransactionDefinition);
		
		HashMap param = null;
		
		try{
			int i,datalength;
			int seq=0;
			String acptNo= null;
			String errStr;
			
			List itemidlist = new ArrayList();
			FileDataList fileDataList = ecamsmsg.getFiledatalist();
			HashMap sysinfoMap = systemService.getSysInfo_detail(ecamsmsg.getSysinfo().getSyscd());
			if(sysinfoMap==null){
				throw new Exception("시스템정보 오류입니다.");
			}
			HashMap fileinfo=null;
			datalength = fileDataList.getFiledatasCount();
			
			int k=0;
			for (i=0;i<datalength;i++){
				if(fileDataList.getFiledatas(i).getItemid() != null && !fileDataList.getFiledatas(i).getItemid().equals("")){
					itemidlist.add(fileDataList.getFiledatas(i).getItemid());
					++k;
				}
			}
			if(k==0){
				logger.error("CheckOutRequestService 95");
				throw new Exception("신청할 대상이 없습니다.");
			}
			
			datalength = k;
			
			boolean flg = true;
			if(ecamsmsg.getRequestinfo().getQrycd().equals("02")){
				if(ecamsmsg.getRequestinfo().getTstchg().equals("0")){
					flg = false;
				}
			}
			

			String srid = null;
			if(ecamsmsg.hasSrinfo()){
				srid = ecamsmsg.getSrinfo().getCcSRId();
			}
			
			
			if(flg){
				errStr = checkFilesStatus(fileDataList,sysinfoMap,ecamsmsg.getRequestinfo().getQrycd(),ecamsmsg.getUserinfo().getId(), srid);
				if (errStr != null && !errStr.equals("")){
					throw new Exception (errStr);
				}
			}
			
			String strTeam = userService.getUserInfo(ecamsmsg.getUserinfo().getId()).getCm_project();
			if (strTeam == null || strTeam.equals("")){
				throw new Exception("사용자 팀 정보 오류");
			}
			
			String tmpSyscd = "";
			
			tmpSyscd = checkInDAO.getSysCd(fileDataList.getFiledatas(0).getItemid());
			
			if (tmpSyscd != null && !tmpSyscd.equals(ecamsmsg.getSysinfo().getSyscd())) {
				throw new Exception("시스템코드가 일치하지 않아 오류가 발생하였습니다.");
			}
			
			if (datalength > 500) {
				throw new Exception("최대 500건 까지만 신청이 가능합니다.");
			}
			
			/*String strRequest = commonCodeService.getCode("REQUEST", ecamsmsg.getRequestinfo().getQrycd()).getCodename();
			if (strRequest == null || strRequest.equals("")){
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
        			
        			param = new HashMap();
        			param.put("CR_ACPTNO", acptNo);
        			param.put("CR_SYSCD", ecamsmsg.getSysinfo().getSyscd());
        			param.put("CR_SYSGB", (String)sysinfoMap.get("cm_sysgb"));
        			param.put("CR_JOBCD", ecamsmsg.getJobinfolist().getJobinfo(0).getJobcd());
        			param.put("CR_STATUS", "0");
        			param.put("CR_TEAMCD", strTeam);
        			param.put("CR_QRYCD", ecamsmsg.getRequestinfo().getQrycd());
        			param.put("CR_PASSOK", "0");
        			//param.put("CR_PASSCD", strRequest);
        			
        			if(null == srid){
        				param.put("CR_PASSCD", "이클립스 체크아웃");
            			param.put("CR_ITSMID", "");
            			param.put("CR_ITSMTITLE", "");
        			}else{
        				param.put("CR_PASSCD", "["+ecamsmsg.getSrinfo().getCcSRId()+"]"+ecamsmsg.getSrinfo().getCcTitle());
            			param.put("CR_ITSMID", ecamsmsg.getSrinfo().getCcSRId());
            			param.put("CR_ITSMTITLE", ecamsmsg.getSrinfo().getCcTitle());
        			}
        			
        			
        			param.put("CR_EMGCD", "9");
        			param.put("CR_EDITOR", ecamsmsg.getUserinfo().getId());
        			//param.put("CR_SAYUCD", ecamsmsg.getRequestinfo().getGbnsayu());
        			//param.put("CR_ITSMID", ecamsmsg.getPrjinfolist().getPrjinfo(0).getScsrno());
        			//param.put("CR_ITSMTITLE", ecamsmsg.getPrjinfolist().getPrjinfo(0).getScsrtitle());
        			param.put("CR_SAYUCD", "9");
        			
        			param.put("CR_BEFJOB", "N");
        			param.put("CR_SVRYN", "N");
        			
        			if(!ecamsmsg.getRequestinfo().getSayu().equals("") || !ecamsmsg.getRequestinfo().getSayu().equals(null)){
        				param.put("CR_SAYU", ecamsmsg.getRequestinfo().getSayu());
        			}
        			
					if (insertCmr1000(param) <= 0){
        				throw new Exception("Cmr1000 Insert Error");
        			}
        			param = null;
        			
        			seq = 0;
        			
        			cmr1000Flg = false;
        		}
        		
        		fileinfo = fileService.getFileInfo(fileDataList.getFiledatas(i).getItemid());
        		if (fileinfo == null || fileinfo.equals("")){
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
    			if(ecamsmsg.getRequestinfo().getTstchg() != null && !ecamsmsg.getRequestinfo().getTstchg().equals("")){
    				param.put("CR_TSTCHG", ecamsmsg.getRequestinfo().getTstchg());
    				if(ecamsmsg.getRequestinfo().getTstchg().equals("0")){
    					param.put("CR_CONFNO", acptNo);
    				}
    			}else{
    				param.put("CR_TSTCHG", "0");
    			}
    			/*
    			if(ecamsmsg.getRequestinfo().getQrycd().equals("01")){
    				param.put("CR_VERSION", ((BigDecimal)fileinfo.get("CR_LSTVER")).intValue());
    				param.put("CR_BEFVER", ((BigDecimal)fileinfo.get("CR_LSTVER")).intValue());
    				//param.put("CR_VERGBN", "R");
    			}else{
    				param.put("CR_VERSION", (ecamsmsg.getFiledata().getBasever()));
    				param.put("CR_BEFVER", (ecamsmsg.getFiledata().getBasever()));
    				//param.put("CR_VERGBN", (ecamsmsg.getFiledata().getVergbn()));
    			}
    			*/
				param.put("CR_VERSION", ((BigDecimal)fileinfo.get("CR_LSTVER")).intValue());
				param.put("CR_BEFVER", ((BigDecimal)fileinfo.get("CR_LSTVER")).intValue());
				
    			param.put("CR_EDITOR", ecamsmsg.getUserinfo().getId());
    			param.put("CR_BASENO", acptNo);
    			
    			param.put("CR_BASEITEM", fileDataList.getFiledatas(i).getItemid());
    			param.put("CR_ITEMID", fileDataList.getFiledatas(i).getItemid());
    			if(!ecamsmsg.getRequestinfo().getSayu().equals("") || !ecamsmsg.getRequestinfo().getSayu().equals(null)){
    				param.put("CR_EDITCON", ecamsmsg.getRequestinfo().getSayu());
    			}

    			if(ecamsmsg.getRequestinfo().getQrycd().equals("01")){
					param.put("CR_BEFVIEWVER", (String)fileinfo.get("CR_VIEWVER"));
					param.put("CR_AFTVIEWVER", (String)fileinfo.get("CR_VIEWVER"));
    			} else {
    				param.put("CR_BEFVIEWVER", (String)fileinfo.get("CR_VIEWVER"));
    				param.put("CR_AFTVIEWVER", ecamsmsg.getFiledata().getBasever());
    			}
    			
    			if (insertCmr1010(param)<=0){
    				throw new Exception("Cmr1010 Insert Error");
    			}
    			
    			param = null;
    			
    			fileinfo = null;
        		
    			if(flg){
    				List result = null;
    				param = new HashMap();
    				param.put("acptno",acptNo);
    				result = checkInDAO.updateCmr1010(param);
    				
    				if(result == null){
    					throw new Exception("updateCmr1010 Error");
    				}
    				param=null;
    				
//	    			param = new HashMap();
//	    			param.put("SET_CR_STATUS", "4");
//	    			param.put("SET_CR_EDITOR", ecamsmsg.getUserinfo().getId());
//	    			if(null != srid){
//	    				param.put("SET_CR_ISRID", ecamsmsg.getSrinfo().getCcSRId());
//	    			}
//	    			param.put("SET_CR_CKOUTUSER", ecamsmsg.getUserinfo().getId());
//	    			param.put("SET_CR_CKOUTACPT", acptNo);
//	    			param.put("PARAM_CR_ITEMID", fileDataList.getFiledatas(i).getItemid());
//	    			if (fileService.updateStatus(param) <= 0){
//	    				throw new Exception("cmr0020 update Error");
//	    			}
    			}
			}
			
			if (!confirmService.request_confirm(acptNo, ecamsmsg.getSysinfo().getSyscd(), ecamsmsg.getRequestinfo().getQrycd(), ecamsmsg.getUserinfo().getId(), null)){
				logger.error("CheckOutRequestService 310:"+acptNo);
				throw new Exception("결재정보등록 중 오류가 발생하였습니다. 관리자에게 연락하여 주시기 바랍니다.");
			}
			
			// TODO Auto-generated method stub
			returnmsg_builder.setReturnval(0);
			returnmsg_builder.setReturnStr(acptNo);
			
			txManager.commit(status);
		}
		catch (RollBackException e){
			logger.error("RequestCheckOut.request: "+ecamsmsg.getUserinfo().getId()+" "+e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getError());
			//txManager.rollback(status);
		}
		catch (Exception e){
			logger.error("RequestCheckOut.request: "+ecamsmsg.getUserinfo().getId()+" "+e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}
		return returnmsg_builder.build();
	}


	
}
