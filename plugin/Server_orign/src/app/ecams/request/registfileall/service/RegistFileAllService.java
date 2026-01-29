package app.ecams.request.registfileall.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import app.core.proto.ProtoEcams.FileData;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.commoncode.service.ICommonCodeService;
import app.ecams.file.service.IFileService;
import app.ecams.job.dao.IJobDAO;
import app.ecams.job.model.Job;
import app.ecams.lang.service.ILangService;

import app.ecams.resourcetype.model.ResourceType;
import app.ecams.resourcetype.service.IResourceTypeService;
import app.ecams.request.autoseq.service.IAutoSeqService;
import app.ecams.request.registfileall.dao.IRegistFileAllDAO;
import app.ecams.request.service.IRequestService;
import app.ecams.request.service.RequestService;
import app.ecams.system.service.ISystemService;
import app.ecams.user.service.IUserService;
import app.ecams.request.checkin.dao.ICheckInDAO;
import app.ecams.request.checkoutcnl.service.ICheckOutCnlRequestService;
import app.ecams.request.confirm.service.IConfirmService;
import app.util.checksum.CheckSum;
import app.util.file.Gzip;

@Service
public class RegistFileAllService extends RequestService implements IRegistFileAllService{
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private IRegistFileAllDAO registFileAllDAO;
	@Autowired private ISystemService systemService;
	@Autowired private IResourceTypeService resourceTypeService;
	@Autowired private IUserService userService;
	@Autowired private IAutoSeqService autoSeqService;
	@Autowired private IConfirmService confirmService;
	@Autowired private PlatformTransactionManager txManager;
	@Autowired private DefaultTransactionDefinition defaultTransactionDefinition;
	@Autowired private Gzip gzip;
	@Autowired private IJobDAO jobDAO;
	@Autowired private IFileService fileService;

	@Override
	@Transactional (readOnly = false, propagation = Propagation.REQUIRED, rollbackForClassName={"app.core.exception.RollBackException"})
	public ReturnMsg request(EcamsMessage ecamsmsg) {
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
		FileDataList.Builder fileDataList_builder = FileDataList.newBuilder();


		TransactionStatus status = txManager.getTransaction(defaultTransactionDefinition);
		
		
		HashMap param = null;
		
		try{
			int i,datalength;
			int seq=0;
			int retCnt=0;
			String acptNo= null;
			String errStr=null;
			
			String jobCd = "";
			
			FileDataList fileDataList = ecamsmsg.getFiledatalist();

			HashMap sysinfoMap = systemService.getSysInfo_detail(ecamsmsg.getSysinfo().getSyscd());
			if(sysinfoMap==null){
				throw new Exception("시스템정보 오류입니다.");
			}
			HashMap fileinfo=null;
			HashMap rsrcinfo=null;
			datalength = fileDataList.getFiledatasCount();

//			logger.error(fileDataList.getFiledatasCount()+":"+ecamsmsg.getRequestinfo().getQrycd()+":"+ecamsmsg.getUserinfo().getId());
			
			
			String strTeam = userService.getUserInfo(ecamsmsg.getUserinfo().getId()).getCm_project();
			if (strTeam == null){
				throw new Exception("사용자 팀 정보 오류");
			}
			/*String strRequest = commonCodeService.getCode("REQUEST", ecamsmsg.getRequestinfo().getQrycd()).getCodename();
			if (strRequest == null){
				throw new Exception("신청구분 오류");
			}*/
			
						
			String tmp = ecamsmsg.getRequestinfolist().getRequestinfo(0).getQrycd();
        	
			acptNo = autoSeqService.getAcptNo(tmp, 1);

			boolean Resout = acptnoCheck(acptNo);
			if(!Resout){
				throw new Exception("신청번호 채번 중 오류가 발생했습니다.");
			}
			
			param=null;
			String itemid="";
			
			boolean cmr1000Flg = true;
			
			for (i=0;i<datalength;i++){
				String srid = "";
				srid = ecamsmsg.getSrinfolist().getSrinfo(i).getCcSRId();
				

				jobCd = ecamsmsg.getJobinfolist().getJobinfo(i).getJobcd();
				
				HashMap params = new HashMap();
				params.put("CM_SYSCD", ecamsmsg.getSysinfo().getSyscd());
				params.put("CM_USERID", ecamsmsg.getUserinfo().getId());

				boolean hasJobcd = false;
				List<Job> jobList = jobDAO.getJobInfo(params);
				for(int jobCnt=0;jobCnt<jobList.size(); jobCnt++){
					if(jobList.get(jobCnt).getJobcd().equals(jobCd)){
						hasJobcd = true;
						break;
					}
				}
				
				if(!hasJobcd){
					System.out.println(">>>FAIL JOBCD IS NULL["+jobCd+"]");
					throw new Exception("권한이 없는 업무입니다. (업무코드:"+jobCd+")");
				}
				
//				if(true) {
//					returnmsg_builder.setReturnval(1);
//					returnmsg_builder.setReturnStr("err");
//					return returnmsg_builder.build();
//				}
				/*
				if(ecamsmsg.getSrinfolist().getSrinfo(i).hasCcSRId()){
					srid = ecamsmsg.getSrinfolist().getSrinfo(i).getCcSRId();
					if( null == srid ) {
						srid = "";
					}
				} else {
					srid = "";
				}
				*/
				
				//if(!"16".equals(ecamsmsg.getRequestinfolist().getRequestinfo(i).getQrycd())){
					errStr = checkFilesStatus(fileDataList,sysinfoMap,ecamsmsg.getRequestinfolist().getRequestinfo(i).getQrycd(),ecamsmsg.getUserinfo().getId(), srid);
					
					if (errStr != null && !errStr.equals("")){
						throw new Exception(errStr);
					}
				//}
				
				itemid = fileDataList.getFiledatas(i).getItemid();
				
				if(itemid == null || itemid.equals("") || itemid.length()==0){
					HashMap<String, String> newList = new HashMap<String, String>();

					newList.put("dirpath",fileDataList.getFiledatas(i).getPathinfo().getRelativitePath());
					//newList.put("syscd", fileDataList.getFiledatas(i).getSysinfo().getSyscd());
					newList.put("syscd", ecamsmsg.getSysinfo().getSyscd());
//					newList.put("jobcd", fileDataList.getFiledatas(i).getJobinfo().getJobcd());
//					newList.put("jobcd", ecamsmsg.getJobinfolist().getJobinfo(i).getJobcd());
					newList.put("jobcd", jobCd);
					newList.put("userid", ecamsmsg.getUserinfo().getId());
					newList.put("rsrcname", fileDataList.getFiledatas(i).getFilename());
					newList.put("rsrccd", fileDataList.getFiledatas(i).getRsrcinfo().getRsrccd());
					newList.put("sayu", ecamsmsg.getRequestinfolist().getRequestinfo(i).getSayu());
					newList.put("srid", srid);
					itemid = fileService.registAllCheckInFile(newList);
					newList = null;
					if(itemid == null || itemid.equals("") || itemid.length()==0){
						System.out.println(">>>FAIL "+ecamsmsg.getUserinfo().getId()+" "+fileDataList.getFiledatas(i).getFilename()
								+" "+jobCd+" "+ecamsmsg.getSysinfo().getSyscd()+" "+
								fileDataList.getFiledatas(i).getPathinfo().getRelativitePath());
						
						throw new Exception("신규등록에 실패했습니다. 다시 진행해 주시기 바랍니다.");
					}
	        	}
				
				fileinfo = null;
        		fileinfo = fileService.getFileInfo(itemid);
        		if (fileinfo == null){
        			throw new Exception("No data fileinfo : ["+itemid+"]");
        		}
        		
        		String qrycd="";
        		if(((BigDecimal)fileinfo.get("CR_LSTVER")).intValue()<1){
        			qrycd="03";
        		}else{
        			//qrycd="04";
        			logger.error("RegistFileAllService 204: "+itemid);
        			throw new Exception(fileDataList.getFiledatas(i).getFilename().toString() + " 현재 운영중인 프로그램입니다. 신규등록이 불가합니다. 관리자에게 문의하세요.");
        		}
        		
        		rsrcinfo = null;
        		rsrcinfo = resourceTypeService.getRsrcInfo_detail(ecamsmsg.getSysinfo().getSyscd(),(String)fileinfo.get("CR_RSRCCD"));
        		if (rsrcinfo == null){
        			throw new Exception("No data rsrcinfo");
        		}
        		
				FileData.Builder fileData_builder = FileData.newBuilder(fileDataList.getFiledatas(i));    			
				fileData_builder.setItemid(itemid);
				
				fileDataList_builder.addFiledatas(fileData_builder.build());				
				fileData_builder=null;
				
        		if (cmr1000Flg){
        			param = new HashMap();
        			param.put("CR_ACPTNO", acptNo);
        			param.put("CR_SYSCD", ecamsmsg.getSysinfo().getSyscd());
        			param.put("CR_SYSGB", (String)sysinfoMap.get("cm_sysgb"));
        			param.put("CR_JOBCD", (String) fileinfo.get("CR_JOBCD"));
        			param.put("CR_STATUS", "0");
        			param.put("CR_TEAMCD", strTeam);
    	        	
    	        	param.put("CR_QRYCD", tmp);
        			//param.put("CR_QRYCD", ecamsmsg.getRequestinfo().getQrycd());
        			param.put("CR_PASSOK", "0");
        			//param.put("CR_PASSCD", strRequest);
        			if( "".equals(srid) ){
        				param.put("CR_PASSCD", "이클립스 체크인");
        				param.put("CR_SAYU", "최초일괄등록");
        			}else{
        				param.put("CR_PASSCD", "["+ecamsmsg.getSrinfolist().getSrinfo(i).getCcSRId()+"]"+ecamsmsg.getSrinfolist().getSrinfo(i).getCcTitle());
            			param.put("CR_ITSMID", ecamsmsg.getSrinfolist().getSrinfo(i).getCcSRId());
            			param.put("CR_ITSMTITLE", ecamsmsg.getSrinfolist().getSrinfo(i).getCcTitle());
            			param.put("CR_SAYU", ecamsmsg.getSrinfolist().getSrinfo(i).getCcTitle());
        			}
        			param.put("CR_EMGCD", ecamsmsg.getRequestinfolist().getRequestinfo(i).getReqgbn());
        			param.put("CR_PASSSUB", "0");
        			if(ecamsmsg.getRequestinfolist().getRequestinfo(i).getReqgbn()!=null && !ecamsmsg.getRequestinfolist().getRequestinfo(i).getReqgbn().equals("")){
	        			if(!ecamsmsg.getRequestinfolist().getRequestinfo(i).getReqgbn().equals("11")){
	        				param.put("CR_DOCNO", ecamsmsg.getRequestinfolist().getRequestinfo(i).getGbnsayu());
	        			}
        			}
        			param.put("CR_EDITOR", ecamsmsg.getUserinfo().getId());
        			//param.put("CR_SAYU", ecamsmsg.getSrinfolist().getSrinfo(i).getCcTitle());
        			//param.put("CR_SAYUCD", ecamsmsg.getRequestinfo().getGbnsayu());
        			//param.put("CR_ITSMID", ecamsmsg.getPrjinfolist().getPrjinfo(0).getScsrno());
        			//param.put("CR_ITSMTITLE", ecamsmsg.getPrjinfolist().getPrjinfo(0).getScsrtitle());
        			param.put("CR_SAYUCD", "9");
        			param.put("CR_BEFJOB", "N");
        			
        			param.put("CR_VERSION", ecamsmsg.getRequestinfolist().getRequestinfo(i).getVersion());//버전업:Y, 버전업미적용:N
        			param.put("CR_SVRYN", ecamsmsg.getRequestinfolist().getRequestinfo(i).getSvrYN());//개발서버적용:Y, 개발서버미적용:N
        			
					if (insertCmr1000(param) <= 0){
        				throw new Exception("Cmr1000 Insert Error");
        			}
        			param = null;
        			seq = 0;
        			
        			cmr1000Flg = false;
        		}

        		param = new HashMap();
    			param.put("CR_ACPTNO", acptNo);
    			param.put("CR_SERNO", ++seq);
    			param.put("CR_SYSCD", ecamsmsg.getSysinfo().getSyscd());
    			param.put("CR_SYSGB", (String)sysinfoMap.get("cm_sysgb"));
//    			param.put("CR_JOBCD", ecamsmsg.getJobinfolist().getJobinfo(0).getJobcd());
//    			param.put("CR_JOBCD", fileDataList.getFiledatas(i).getJobinfo().getJobcd());   
//    			param.put("CR_JOBCD", jobCd);
    			param.put("CR_JOBCD", (String) fileinfo.get("CR_JOBCD"));
    			param.put("CR_STATUS", "0");
    			param.put("CR_QRYCD", qrycd);
    			param.put("CR_RSRCCD", (String) fileinfo.get("CR_RSRCCD"));
    			param.put("CR_LANGCD", (String) fileinfo.get("CR_LANGCD"));
    			param.put("CR_DSNCD",  (String) fileinfo.get("CR_DSNCD"));
    			param.put("CR_RSRCNAME",  (String) fileinfo.get("CR_RSRCNAME"));
    			param.put("CR_RSRCNAM2",  (String) fileinfo.get("CR_RSRCNAME"));
    			
    			param.put("CR_CHGCD", "0");
    			param.put("CR_TSTCHG", ecamsmsg.getRequestinfolist().getRequestinfo(i).getTstchg());
    			
    			param.put("CR_SRCCHG", "1");
    			param.put("CR_SRCCMP", "Y");
    			
    			param.put("CR_PRIORITY", rsrcinfo.get("CM_STEPSTA"));

				param.put("CR_VERSION", ((BigDecimal)fileinfo.get("CR_LSTVER")).intValue()+1);
    			param.put("CR_BEFVER", ((BigDecimal)fileinfo.get("CR_LSTVER")).intValue());
    			
    			param.put("CR_EDITOR", ecamsmsg.getUserinfo().getId());
    			param.put("CR_BASEITEM", itemid);
    			param.put("CR_ITEMID", itemid);
    			param.put("CR_EDITCON", ecamsmsg.getRequestinfolist().getRequestinfo(i).getSayu());
    			param.put("CR_PGMTYPE", fileinfo.get("CR_PGMTYPE"));

    			param.put("CR_VERYN", ecamsmsg.getRequestinfolist().getRequestinfo(i).getVersion());//버전업:Y, 버전업미적용:N
    			param.put("CR_SVRYN", ecamsmsg.getRequestinfolist().getRequestinfo(i).getSvrYN());//개발서버적용:Y, 개발서버미적용:N
    			
    			if (insertCmr1010(param)<=0){
    				throw new Exception("Cmr1010 Insert Error");
    			}
    			param = null;
			}
        	if (!confirmService.request_confirm(acptNo, ecamsmsg.getSysinfo().getSyscd(), tmp, ecamsmsg.getUserinfo().getId(), null)){
			//if (!confirmService.request_confirm(acptNo, ecamsmsg.getSysinfo().getSyscd(), ecamsmsg.getRequestinfo().getQrycd(), ecamsmsg.getUserinfo().getId(), null)){
				logger.error("RegistFileAllService 311:" + acptNo);
				throw new Exception("결재정보등록 중 오류가 발생하였습니다. 관리자에게 연락하여 주시기 바랍니다.");
			}
			List result = null;
			param = new HashMap();
			param.put("acptno",acptNo);
			result = registFileAllDAO.updateCmr1010(param);
			
			if(result == null){
				throw new Exception("updateCmr1010 Error");
			}
			param=null;
			
			returnmsg_builder.setReturnval(0);
			returnmsg_builder.setReturnStr(acptNo);
			if (fileDataList_builder.getFiledatasCount()>0){
				builder_msg.setMsgtype("return");
				builder_msg.setFiledatalist(fileDataList_builder.build());
				returnmsg_builder.setEcamsmsg(builder_msg.build());
				builder_msg = null;
			}
			txManager.commit(status);
		}
		catch (RollBackException e){
			logger.error("RequestCheckInRegAll.request: "+ecamsmsg.getUserinfo().getId()+" "+e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getError());
			//txManager.rollback(status);
		}
		catch (Exception e){
			logger.error("RequestCheckInRegAll.request: "+ecamsmsg.getUserinfo().getId()+" "+e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}
		return returnmsg_builder.build();
	}	
}
