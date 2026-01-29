package app.ecams.request.registfilenew.service;

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
public class RegistFileNewService extends RequestService implements IRegistFileNewService{
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private ISystemService systemService;
	@Autowired private IResourceTypeService resourceTypeService;
	@Autowired private IUserService userService;
//	@Autowired private IAutoSeqService autoSeqService;
//	@Autowired private IConfirmService confirmService;
	@Autowired private PlatformTransactionManager txManager;
	@Autowired private DefaultTransactionDefinition defaultTransactionDefinition;
//	@Autowired private Gzip gzip;
	@Autowired private IJobDAO jobDAO;
//	@Autowired private IFileService fileService;

	@Override
	@Transactional (readOnly = false, propagation = Propagation.REQUIRED, rollbackForClassName={"app.core.exception.RollBackException"})
	public ReturnMsg request(EcamsMessage ecamsmsg) {
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
		TransactionStatus status = txManager.getTransaction(defaultTransactionDefinition);
		
		
		try{
			int i,datalength;
			String jobCd = "";
			String errStr=null;
			
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
			
			String itemid="";
			
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
				if(!"16".equals(ecamsmsg.getRequestinfolist().getRequestinfo(i).getQrycd())){
					errStr = checkFilesStatus(fileDataList,sysinfoMap,ecamsmsg.getRequestinfolist().getRequestinfo(i).getQrycd(),ecamsmsg.getUserinfo().getId(), srid);
					
					if (errStr != null && !errStr.equals("")){
						throw new Exception(errStr);
					}
				}
				
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
	        	}else {
        			logger.error("RegistFileNewService 204: "+itemid);
        			throw new Exception(fileDataList.getFiledatas(i).getFilename().toString() + " 현재 운영중인 프로그램입니다. 신규등록이 불가합니다. 관리자에게 문의하세요.");
	        	}
				
				fileinfo = null;
        		fileinfo = fileService.getFileInfo(itemid);
        		if (fileinfo == null){
        			throw new Exception("No data fileinfo : ["+itemid+"]");
        		}
        		
        		rsrcinfo = null;
        		rsrcinfo = resourceTypeService.getRsrcInfo_detail(ecamsmsg.getSysinfo().getSyscd(),(String)fileinfo.get("CR_RSRCCD"));
        		if (rsrcinfo == null){
        			throw new Exception("No data rsrcinfo");
        		}
			}
			
			returnmsg_builder.setReturnval(0);
			returnmsg_builder.setReturnStr("");
			builder_msg.setMsgtype("return");
			returnmsg_builder.setEcamsmsg(builder_msg.build());
			builder_msg = null;
			
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
