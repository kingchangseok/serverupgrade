package app.ecams.srjob.service;

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

import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileData;
import app.core.proto.ProtoEcams.FileDataList;
import app.core.proto.ProtoEcams.JobInfo;
import app.core.proto.ProtoEcams.PathInfo;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.core.proto.ProtoEcams.RsrcInfo;
import app.core.proto.ProtoEcams.SRInfo;
import app.core.proto.ProtoEcams.SRInfoList;
import app.core.proto.ProtoEcams.SysInfo;
import app.core.proto.ProtoEcams.SysInfoList;
import app.ecams.path.dao.IPathDAO;
import app.ecams.srjob.dao.ISRJobDAO;
import app.ecams.srjob.model.SRJobInfo;

@Service
public class SRJobService implements ISRJobService {
private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private ISRJobDAO serviceRequestDAO;
	@Autowired private IPathDAO pathDAO;
	@Autowired private PlatformTransactionManager txManager;
	@Autowired private DefaultTransactionDefinition defaultTransactionDefinition;

	public ReturnMsg getSRInfo(EcamsMessage ecamsmsg) {
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		SRInfoList.Builder srinfolist_builder = SRInfoList.newBuilder();
		
		int returnval = 0;
		String returnStr = "";
		
		try{ 
			HashMap param = new HashMap();
			param = new HashMap<String, String>();
			param.put("ccEditor" , ecamsmsg.getUserinfo().getId());
			param.put("isAdmin" , "N");

			List<SRJobInfo> srlist = serviceRequestDAO.getSRInfo(param);

			if (null != srlist) {
				SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
				for(int i=0; i<srlist.size(); i++){
					srinfo_builder = SRInfo.newBuilder();
					srinfo_builder.setCcSRId(srlist.get(i).getCcID());
					srinfo_builder.setCcTitle(srlist.get(i).getCcTitle());
					srinfo_builder.setCcComment(srlist.get(i).getCcComment());
					srinfo_builder.setCcStatusCD(srlist.get(i).getCcStatusCD());
					srinfo_builder.setCcEditorName(srlist.get(i).getCcEditorName());
					srinfo_builder.setCcEditor(srlist.get(i).getCcEditor());
					srinfo_builder.setCcCattype(srlist.get(i).getCcCatType());
					srinfo_builder.setCcChgtype(srlist.get(i).getCcChgType());
					srinfo_builder.setCcWorkrank(srlist.get(i).getCcWorkRank());
					srinfolist_builder.addSrinfo(srinfo_builder.build());
					srinfo_builder = null;
				}
				
				returnval = 0; 
				returnStr = "정상";
				ecamsmsg_builder.setSrinfolist(srinfolist_builder.build());
				returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
				srinfolist_builder = null;
			} else {
				returnval = 1;
				returnStr = "오류";			
			}
			
			returnmsg_builder.setReturnval(returnval);
			returnmsg_builder.setReturnStr(returnStr);
				
		}catch(Exception e){
			logger.error("ServiceRequest Service: ",e);
		    returnmsg_builder.setReturnval(1);
		    returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}
		return returnmsg_builder.build();
	}
	
	public ReturnMsg getSResource(EcamsMessage ecamsmsg) {
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		
		int returnval = 0;
		String returnStr = "";
		
		try{ 
			
			HashMap cmc0100STA = serviceRequestDAO.getCmc0100(ecamsmsg.getSrinfo().getCcSRId());
			
			//반려, 중단, 완료인 경우 END
			if(null == cmc0100STA){
				returnStr = "END";
			}else if("3".equals(cmc0100STA.get("cc_status")) || "8".equals(cmc0100STA.get("cc_status")) || "9".equals(cmc0100STA.get("cc_status"))){
				returnStr = "END";
			}else{
				HashMap<String, String> param = new HashMap<String, String>();
				param.put("srid", ecamsmsg.getSrinfo().getCcSRId());
				param.put("editor", ecamsmsg.getUserinfo().getId());
				
				List<SRJobInfo> srlist = (List<SRJobInfo>) serviceRequestDAO.getSResource(param);
				param = null;
				
				if (null != srlist){
					String tmpPath = "";
					FileDataList.Builder filedatalist_builder = FileDataList.newBuilder();
					for(int i=0; i<srlist.size(); i++){
						FileData.Builder filedata = FileData.newBuilder();
						filedata.setItemid(srlist.get(i).getItemid());
						filedata.setFilename(srlist.get(i).getRsrcname());
						filedata.setStatus(srlist.get(i).getStatus());
						
						SysInfo.Builder sysInfo = SysInfo.newBuilder();
						sysInfo.setSyscd(srlist.get(i).getSyscd());
						if( null != srlist.get(i).getPrjname() ) {
							sysInfo.setPrjname(srlist.get(i).getPrjname());
						}
						filedata.setSysinfo(sysInfo);
						
						JobInfo.Builder jobInfo = JobInfo.newBuilder();
						jobInfo.setJobcd(srlist.get(i).getJobcd());
						filedata.setJobinfo(jobInfo);
						
						PathInfo.Builder pathInfo = PathInfo.newBuilder();
						
						param = new HashMap<String, String>();
						param.put("cm_syscd", srlist.get(i).getSyscd());
						param.put("cm_rsrccd", srlist.get(i).getRsrccd());
						param.put("cm_jobcd", srlist.get(i).getJobcd());
						String baseHome = pathDAO.getBasePath(param);
						param = null;
						
						if (null == baseHome || "".equals(baseHome)) {
							throw new Exception("체크아웃서버 프로그램종류 연결정보 등록확인 rsrccd["+srlist.get(i).getRsrccd()+"]");
						}
						//String baseHome =  pathDAO.getHomePath(srlist.get(i).getSyscd());
						//String baseHome = "/home/ecamss/ecamsdev/";//"/ecams/"+srlist.get(i).getJobcd();

						tmpPath = srlist.get(i).getDirpath();

						logger.error("tmpPath=="+tmpPath);
						logger.error("baseHome=="+baseHome);
						if (tmpPath.equals(baseHome)) {
							pathInfo.setRelativitePath("");
						} else {
							pathInfo.setRelativitePath(tmpPath.substring(baseHome.length()));
						}
						//pathInfo.setRelativitePath(srlist.get(i).getDirpath().substring(baseHome.length(), srlist.get(i).getDirpath().length()));
						filedata.setPathinfo(pathInfo);
						
						SRInfo.Builder srInfo = SRInfo.newBuilder();
						srInfo.setCcEditor(srlist.get(i).getCcEditor());
						srInfo.setCcSRId(srlist.get(i).getCcID());
						srInfo.setCcStatusCD(srlist.get(i).getCcStatusCD());
						filedata.setSrinfo(srInfo);
						
						filedatalist_builder.addFiledatas(filedata.build());
					}
				
					ecamsmsg_builder.setFiledatalist(filedatalist_builder.build());
					returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());				
				} 
				returnStr = "정상";
			}

			returnval = 0; 
			returnmsg_builder.setReturnval(returnval);
			returnmsg_builder.setReturnStr(returnStr);
				
		}catch(Exception e){
			logger.error("ServiceRequest Service: ",e);
		    returnmsg_builder.setReturnval(1);
		    returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}
		return returnmsg_builder.build();
	}
	
	public ReturnMsg getSRAcess(EcamsMessage ecamsmsg) {
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		
		try{
			HashMap<String, String> param = new HashMap<String, String>();
			param.put("srid", ecamsmsg.getSrinfo().getCcSRId());
			param.put("editor", ecamsmsg.getUserinfo().getId());
			
			int cnt = serviceRequestDAO.chkMySR(param);
			param = null;
			
			if (cnt > 0) {
				returnmsg_builder.setReturnval(0);
				returnmsg_builder.setReturnStr("정상");
				returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
			}else{
				returnmsg_builder.setReturnval(1);
				returnmsg_builder.setReturnStr("count:0");
				returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
			}

		}catch(Exception e){
			logger.error("ServiceRequest Service Exception : ",e);
		    returnmsg_builder.setReturnval(1);
		    returnmsg_builder.setReturnStr("Error Message: "+e.getCause() + "\n" + e.getMessage());
		}
		return returnmsg_builder.build();
	}

	public ReturnMsg getSResource2(EcamsMessage ecamsmsg) {	// 20210106 SR사용안함일때도 리소스목록 새로고침시 가져올수있게 수정
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		
		int returnval = 0;
		String returnStr = "";
		String baseHome = "";
		
		try{ 
			HashMap<String, String> param = new HashMap<String, String>();
			param.put("srid", "");
			param.put("editor", ecamsmsg.getUserinfo().getId());
			
			List<SRJobInfo> srlist = (List<SRJobInfo>) serviceRequestDAO.getSResource2(param);
			param = null;
			
			if (null != srlist){
				FileDataList.Builder filedatalist_builder = FileDataList.newBuilder();
				for(int i=0; i<srlist.size(); i++){
					FileData.Builder filedata = FileData.newBuilder();
					filedata.setItemid(srlist.get(i).getItemid());
					filedata.setFilename(srlist.get(i).getRsrcname());
					filedata.setStatus(srlist.get(i).getStatus());
					
					SysInfo.Builder sysInfo = SysInfo.newBuilder();
					sysInfo.setSyscd(srlist.get(i).getSyscd());
					if( null != srlist.get(i).getPrjname() ) {
						sysInfo.setPrjname(srlist.get(i).getPrjname());
					}
					filedata.setSysinfo(sysInfo);
					
					JobInfo.Builder jobInfo = JobInfo.newBuilder();
					jobInfo.setJobcd(srlist.get(i).getJobcd());
					filedata.setJobinfo(jobInfo);
					
					PathInfo.Builder pathInfo = PathInfo.newBuilder();
					
//					param = new HashMap<String, String>();
//					param.put("cm_syscd", srlist.get(i).getSyscd());
//					param.put("cm_rsrccd", srlist.get(i).getRsrccd());
//					param.put("cm_jobcd", srlist.get(i).getJobcd());
//					baseHome = pathDAO.getBasePath(param);
//					param = null;
					
					baseHome = srlist.get(i).getBasepath();
					
					if (null == baseHome || "".equals(baseHome)) {
						throw new Exception("체크아웃서버 프로그램종류 연결정보 등록확인 rsrccd["+srlist.get(i).getRsrccd()+"]");
					}

					pathInfo.setRelativitePath(srlist.get(i).getDirpath().substring(baseHome.length(), srlist.get(i).getDirpath().length()));
					filedata.setPathinfo(pathInfo);
					
					SRInfo.Builder srInfo = SRInfo.newBuilder();
					srInfo.setCcEditor(srlist.get(i).getCcEditor());
					srInfo.setCcSRId("");
					srInfo.setCcStatusCD("");
					filedata.setSrinfo(srInfo);
					
					filedatalist_builder.addFiledatas(filedata.build());
				}
			
				ecamsmsg_builder.setFiledatalist(filedatalist_builder.build());
				returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());				
			} 
			returnStr = "정상";

		returnval = 0; 
		returnmsg_builder.setReturnval(returnval);
		returnmsg_builder.setReturnStr(returnStr);
				
		}catch(Exception e){
			logger.error("ServiceRequest Service: ",e);
		    returnmsg_builder.setReturnval(1);
		    returnmsg_builder.setReturnStr("message:"+e.getMessage());
		} finally {
			baseHome = null;
			returnStr = null;
			ecamsmsg_builder = null;
		}
		return returnmsg_builder.build();
	}
}
