package app.ecams.request.checkincnl.service;

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
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.commoncode.service.ICommonCodeService;
import app.ecams.request.autoseq.service.IAutoSeqService;
import app.ecams.request.checkincnl.dao.ICheckInCnlDAO;
import app.ecams.request.confirm.service.IConfirmService;
import app.ecams.request.service.RequestService;
import app.ecams.system.service.ISystemService;
import app.ecams.user.service.IUserService;

@Service
public class CheckInCnlRequestService extends RequestService implements ICheckInCnlRequestService{
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private ISystemService systemService;
	@Autowired private IUserService userService;
	@Autowired private ICommonCodeService commonCodeService;
	@Autowired private IAutoSeqService autoSeqService;
	@Autowired private IConfirmService confirmService;
	@Autowired private PlatformTransactionManager txManager;
	@Autowired private DefaultTransactionDefinition defaultTransactionDefinition;
	@Autowired private ICheckInCnlDAO checkInCnlDAO;
	
	@Override
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
			FileDataList fileDataList = ecamsmsg.getFiledatalist();
			HashMap sysinfoMap = systemService.getSysInfo_detail(ecamsmsg.getSysinfo().getSyscd());
			if(sysinfoMap==null){
				throw new RollBackException("시스템정보 오류입니다.");
			}
			datalength = fileDataList.getFiledatasCount();
			
			
			
			errStr = checkFilesStatus(fileDataList,sysinfoMap,ecamsmsg.getRequestinfo().getQrycd(),ecamsmsg.getUserinfo().getId(), ecamsmsg.getSrinfo().getCcSRId());
			
			if (errStr != null){
				throw new Exception(errStr);
			}
			
			String strTeam = userService.getUserInfo(ecamsmsg.getUserinfo().getId()).getCm_project();
			if (strTeam == null){
				throw new Exception("사용자 팀 정보 오류[개발체크인취소]");
			}
			String strRequest = commonCodeService.getCode("REQUEST", ecamsmsg.getRequestinfo().getQrycd()).getCodename();
			if (strRequest == null){
				throw new Exception("신청구분 오류[개발체크인취소]");
			}
			
	        int wkC = datalength/300;
	        int wkD = datalength%300;
	        if (wkD>0) wkC = wkC + 1;
	        
	        String svAcpt[] = null; 
	        svAcpt = new String [wkC];        
			
//			for (i=0;i<wkC;i++){
//				do{
////					acptNo = autoSeqService.getAcptNo(ecamsmsg.getRequestinfo().getQrycd());
//					
//				}while(!acptnoCheck(acptNo));
//				svAcpt[i] = acptNo;
//			}
			
	        acptNo = autoSeqService.getAcptNo(ecamsmsg.getRequestinfo().getQrycd(), wkC);
		    svAcpt = acptNo.split(":");
		    
		    for (i=0;i<wkC;i++){
		    	if(acptnoCheck(svAcpt[i])==false){
		    		throw new RollBackException("신청번호 오류[개발체크인취소]");
		        }
		    }
			
			boolean insSw = false;
			String msg = "";
			//String confno = "";
			for (i=0;i<datalength;i++){
				param = new HashMap();
				//param.put("PARAM_CR_PRJNO", ecamsmsg.getPrjinfolist().getPrjinfo(0).getScsrno());
				param.put("PARAM_CR_PRJNO", "");
				param.put("PARAM_CR_SYSCD", ecamsmsg.getSysinfo().getSyscd());
				param.put("PARAM_CR_ITEMID", fileDataList.getFiledatas(i).getItemid());
				
				msg = "";
				msg = checkInCnlDAO.selectCnl_count(param);
				if (msg.equals("[ERROR]")){
					param = null;
					throw new RollBackException(msg);
				}
				param = null;
				
				//개발체크인취소진행 시작~!
				insSw = false;
        		if (i == 0){
        			insSw = true;
        		}
        		else{
        			wkC = i%300;
        			if (wkC == 0){
        				insSw = true;
        			}
        		}
        		
        		if (insSw == true) {
        			if (i>=300) {
        				param = new HashMap();
    					param.put("acptno",acptNo);
						List result = updateConfCmr1010(param);
						
						if(result == null){
							throw new RollBackException("updateCmr1010 오류");
						}
						
        				param = null;
        				
        				if (!confirmService.request_confirm(acptNo, ecamsmsg.getSysinfo().getSyscd(), ecamsmsg.getRequestinfo().getQrycd(), ecamsmsg.getUserinfo().getId(), null)){
        					throw new RollBackException("결재정보등록 중 오류가 발생하였습니다. 관리자에게 연락하여 주시기 바랍니다.[개발체크인취소]");
        				}
        			}
        			
        			wkC = i/300;
        			acptNo = svAcpt[wkC];
        			
        			param = new HashMap();
        			param.put("CR_ACPTNO", acptNo);
        			param.put("CR_SYSCD", ecamsmsg.getSysinfo().getSyscd());
        			param.put("CR_SYSGB", (String)sysinfoMap.get("cm_sysgb"));
        			param.put("CR_JOBCD", fileDataList.getFiledatas(i).getJobinfo().getJobcd());
        			param.put("CR_STATUS", "0");
        			param.put("CR_TEAMCD", strTeam);
        			param.put("CR_QRYCD", ecamsmsg.getRequestinfo().getQrycd());
        			param.put("CR_PASSOK", "0");
        			param.put("CR_PASSCD", strRequest);
        			param.put("CR_EDITOR", ecamsmsg.getUserinfo().getId());
        			
        			param.put("CR_PRJNO", ecamsmsg.getPrjinfolist().getPrjinfo(0).getScsrno());
        			param.put("CR_PRJNAME", ecamsmsg.getPrjinfolist().getPrjinfo(0).getScsrtitle());
        			param.put("CR_BIZCODE", ecamsmsg.getPrjinfolist().getPrjinfo(0).getSbizcode());
        			param.put("CR_PRJSTATUS", ecamsmsg.getPrjinfolist().getPrjinfo(0).getSemerrequestyn());
        			
        			param.put("CR_BEFJOB", "N");
        			param.put("CR_SVRYN", "N");
        			
					if (insertCmr1000(param) <= 0){
						param = null;
        				throw new RollBackException("Cmr1000 Insert Error[개발체크인취소]");
        			}
        			param = null;
        			
        			seq = 0;
        		}
        		
//				param = new HashMap();
//				param.put("CR_ACPTNO", acptNo);
//				param.put("CR_ITEMID", fileDataList.getFiledatas(i).getItemid());
//				
//				confno = "";
//				confno = checkInCnlDAO.selectCmr1010_acptno(param);
//				if(confno == null){
//					param = null;
//					throw new RollBackException("confno null[개발체크인취소]");
//				}
//				param = null;

				HashMap fileinfo=null;
        		fileinfo = fileService.getFileInfo(fileDataList.getFiledatas(i).getItemid());
        		if (fileinfo == null){
        			fileinfo = null;
        			throw new RollBackException("fileinfo get error - itemid=["+fileDataList.getFiledatas(i).getItemid()+"][개발체크인취소]");
        		}
        		

        		//insertCmr1010  시작
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
//    			param.put("CR_BASENO", confno);
//    			param.put("CR_CONFNO", confno);
    			
    			param.put("CR_BASEITEM", fileDataList.getFiledatas(i).getItemid());
    			param.put("CR_ITEMID", fileDataList.getFiledatas(i).getItemid());
//    			param.put("CR_COACPT", confno);			
    			
    			if (insertCmr1010(param)<=0){
    				param = null;
    				fileinfo = null;
    				throw new RollBackException("Cmr1010 Insert Error[개발체크인취소]");
    			}
    			param = null;
    			fileinfo = null;
        		
    			param = new HashMap();
    			param.put("SET_CR_STATUS", "2");
    			param.put("SET_CR_EDITOR", ecamsmsg.getUserinfo().getId());
    			param.put("SET_CR_SYSDATE","111");
    			param.put("SET_CR_ISRID", "");
    			param.put("PARAM_CR_ITEMID", fileDataList.getFiledatas(i).getItemid());
    			if (fileService.updateStatus(param) <= 0){
    				param = null;
    				throw new RollBackException("cmr0020 update Error[개발체크인취소]");
    			}
    			param = null;
    			
//	    		//checkInCnlDAO.updateCmr1010_confno  시작
//				param = new HashMap();
//				//param.put("CR_ACPTNO", confno);
//				param.put("CR_ACPTNO", acptNo);
//				//param.put("CR_ITEMID", fileDataList.getFiledatas(i).getItemid());
//				if (checkInCnlDAO.updateCmr1010_confno(param)<1){
//					param = null;
//					throw new RollBackException("Cmr1010_confno update Error[개발체크인취소]");
//				}
//				param = null;
			}
			
			param = new HashMap();
			param.put("acptno",acptNo);
			List result = updateConfCmr1010(param);
			
			if(result == null){
				throw new RollBackException("updateCmr1010 오류");
			}
			
			param=null;
			
			if (!confirmService.request_confirm(acptNo, ecamsmsg.getSysinfo().getSyscd(), ecamsmsg.getRequestinfo().getQrycd(), ecamsmsg.getUserinfo().getId(), null)){
				throw new RollBackException("결재정보등록 중 오류가 발생하였습니다. 관리자에게 연락하여 주시기 바랍니다.[개발체크인취소]");
			}
			
			// TODO Auto-generated method stub
			returnmsg_builder.setReturnval(0);
			returnmsg_builder.setReturnStr(StringUtils.join(svAcpt,":"));
			
			txManager.commit(status);
		}
		catch (RollBackException e){
			logger.error("RequestCheckInCnl.request: "+ecamsmsg.getUserinfo().getId()+" "+e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr(e.getError());
			txManager.rollback(status);
		}
		catch (Exception e){
			logger.error("RequestCheckInCnl.request: "+ecamsmsg.getUserinfo().getId()+" "+e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr(e.getMessage());
			//txManager.rollback(status);
		}		
		return returnmsg_builder.build();
	}
	public String selectCmr1010_acptno(HashMap param) {
		// TODO Auto-generated method stub
		return checkInCnlDAO.selectCmr1010_acptno(param);
	}
	
	public int updateCmr1010_confno(HashMap param){
		return checkInCnlDAO.updateCmr1010_confno(param);
	}
}
