package app.ecams.request.checkin.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.fasoo.sparrow.commons.util.hash.MD5HashGenerator;
import com.fasoo.sparrow.commons.util.hash.SHA1HashGenerator;
import com.fasoo.sparrow.core.dto.VcsFileInspectDto;
import com.fasoo.sparrow.vcs.rest.client.VcsFileInspectRestClient;
import com.fasoo.sparrow.vcs.rest.client.VcsRestClient;

import app.core.exception.RollBackException;
import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileDataList;
import app.core.proto.ProtoEcams.FileData;
import app.core.proto.ProtoEcams.PathInfo;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.core.proto.ProtoEcams.RsrcInfo;
import app.ecams.common.setting.commonSettings;
import app.ecams.commoncode.service.ICommonCodeService;
import app.ecams.file.dao.IFileDAO;
import app.ecams.file.model.FileInfo;
import app.ecams.file.service.IFileService;
import app.ecams.job.dao.IJobDAO;
import app.ecams.job.model.Job;
import app.ecams.lang.service.ILangService;

import app.ecams.path.dao.IPathDAO;
import app.ecams.path.service.IPathService;
import app.ecams.resourcetype.model.ResourceType;
import app.ecams.resourcetype.service.IResourceTypeService;
import app.ecams.request.autoseq.service.IAutoSeqService;
import app.ecams.request.service.IRequestService;
import app.ecams.request.service.RequestService;
import app.ecams.system.service.ISystemService;
import app.ecams.user.service.IUserService;
import app.ecams.request.checkin.dao.ICheckInDAO;
import app.ecams.request.checkin.model.CheckInfo;
import app.ecams.request.checkoutcnl.service.ICheckOutCnlRequestService;
import app.ecams.request.confirm.service.IConfirmService;
import app.util.checksum.CheckSum;
import app.util.file.FileToByteArray;
import app.util.file.Gzip;

@Service
public class CheckInRequestService extends RequestService implements ICheckInRequestService{
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private ICheckInDAO checkInDAO;
	@Autowired private ISystemService systemService;
	@Autowired private IResourceTypeService resourceTypeService;
	@Autowired private IUserService userService;
	@Autowired private IAutoSeqService autoSeqService;
	@Autowired private IConfirmService confirmService;
	@Autowired private PlatformTransactionManager txManager;
	@Autowired private DefaultTransactionDefinition defaultTransactionDefinition;
	@Autowired private Gzip gzip;
	@Autowired private IJobDAO jobDAO;
	@Autowired private IFileDAO fileDAO;
	@Autowired private IPathDAO pathDAO;
	@Autowired private IFileService fileService;
	@Autowired private IPathService pathService;

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
			String acptNo= null;
			String errStr=null;
			commonSettings cs = new commonSettings();
			
			String basePackage = cs.getBasePackage();//com/shc/wink
			String baseJSDir = cs.getBaseJSDir();
			String baseJSName = cs.getBaseJSName();
			String baseJobCd = cs.getBaseJobCd();
			
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
			
			
			String srid = null;
			if(ecamsmsg.hasSrinfo()){
				srid = ecamsmsg.getSrinfo().getCcSRId();
			}
			
			if(!"16".equals(ecamsmsg.getRequestinfo().getQrycd())){
				errStr = checkFilesStatus(fileDataList,sysinfoMap,ecamsmsg.getRequestinfo().getQrycd(),ecamsmsg.getUserinfo().getId(), srid);
				
				if (errStr != null && !errStr.equals("")){
					throw new Exception(errStr);
				}
			}
			
			String strTeam = userService.getUserInfo(ecamsmsg.getUserinfo().getId()).getCm_project();
			if (strTeam == null){
				throw new Exception("사용자 팀 정보 오류");
			}
			/*String strRequest = commonCodeService.getCode("REQUEST", ecamsmsg.getRequestinfo().getQrycd()).getCodename();
			if (strRequest == null){
				throw new Exception("신청구분 오류");
			}*/
			
			
			String tmp = ecamsmsg.getRequestinfo().getQrycd();
        	
			acptNo = autoSeqService.getAcptNo(tmp, 1);

			boolean Resout = acptnoCheck(acptNo);
			if(!Resout){
				throw new Exception("신청번호 채번 중 오류가 발생했습니다.");
			}
			
			param=null;
			String itemid="";
			String baseitem = "";
			
			HashMap params = new HashMap();
			params.put("CM_SYSCD", ecamsmsg.getSysinfo().getSyscd());
			params.put("CM_USERID", ecamsmsg.getUserinfo().getId());

			boolean hasJobcd = false;
			List<Job> jobList = jobDAO.getJobInfo(params);
			
			boolean cmr1000Flg = true;
			
			String tmpSyscd = "";
			
			tmpSyscd = checkInDAO.getSysCd(fileDataList.getFiledatas(0).getItemid());
			
			if (tmpSyscd != null && !tmpSyscd.equals(ecamsmsg.getSysinfo().getSyscd())) {
				throw new Exception("시스템코드가 일치하지 않아 오류가 발생하였습니다.");
			}
			
			if (datalength > 500) {
				throw new Exception("최대 500건 까지만 신청이 가능합니다.");
			}
			
			String viewver = "";
			
			for (i=0;i<datalength;i++){
				itemid = fileDataList.getFiledatas(i).getItemid();
				if(itemid == null || itemid.equals("") || itemid.length()==0){
					jobCd = "";
					
					if(ecamsmsg.getTooltype().equals("I")) { //iStudio사용
						String tmpPath = fileDataList.getFiledatas(i).getPathinfo().getRelativitePath();
						//tmpPath     : .istudiometa/page/com/ibks/common/biz
						//basePackage : com/shc/wink
						//indexof     : > -1

						if(tmpPath.indexOf(basePackage) > -1){ //현재디렉토리에 'com/shc/wink' 포함됨
							
							if(tmpPath.indexOf(basePackage+"/cc/bv") > -1){ //현재디렉토리 '/com/shc/wink/cc/bv' 포함됨
								jobCd = "BV";
							}else if(tmpPath.indexOf(basePackage+"/cc/ch") > -1){ //현재디렉토리 '/com/shc/wink/cc/ch' 포함됨
								jobCd = "CH"; //ADMIN
							}else if(tmpPath.indexOf(basePackage+"/cc") > -1){ //현재디렉토리 '/com/shc/wink/cc' 포함됨
								jobCd = "CC";
							}else{
								tmpPath = tmpPath.substring(tmpPath.indexOf(basePackage)+basePackage.length());								
								if(tmpPath.split("/").length > 2){
									jobCd = tmpPath.split("/")[2].substring(0,2).toUpperCase();
								}else{
									jobCd = baseJobCd; //ADMIN
								}
							}
						}else{ //모두 관리자업무로
							jobCd = baseJobCd; //ADMIN

							if(tmpPath.indexOf(baseJSDir) > -1){
								//파일명이 BizCommon*.js 는 *업무 제외
								String tmpFileName = fileDataList.getFiledatas(i).getFilename();
								if(tmpFileName.indexOf("BizCommon") > -1){
									if(!"BizCommon.js".equals(tmpFileName)) {
										tmpFileName = tmpFileName.replaceAll("BizCommon", "").replaceAll("\\.js", "");
										
										if(tmpFileName.length()>1){
											jobCd = tmpFileName.substring(0,2).toUpperCase();
										}
									}
								}
							}
						}
						
						for(int j=0; j<jobList.size(); j++) {
							if( jobCd.equals(jobList.get(j).getJobcd()) ) {
								hasJobcd = true;
								break;
							}
						}
						
						if( !hasJobcd ) {
							if("I".equals(ecamsmsg.getTooltype())) System.out.println("iStudio: Not Found JobCd:"+jobCd+", "+tmpPath+"/"+fileDataList.getFiledatas(i).getFilename());
							else System.out.println("eClipse: Not Found JobCd:"+jobCd+", "+tmpPath+"/"+fileDataList.getFiledatas(i).getFilename());
							//logger.error("\n>>>>>>>Not Found JobCd ["+jobCd+"] ["+fileDataList.getFiledatas(i).getFilename()+"]");
							/*if(ecamsmsg.getRequestinfo().getQrycd().equals("16")){
								continue;
							}else{*/
								returnmsg_builder.setReturnval(1);
								if("I".equals(ecamsmsg.getTooltype())) returnmsg_builder.setReturnStr("[JOBCD:"+jobCd+"]등록되지 않았거나 권한이없는 업무입니다. (TOOL:iStudio)");
								else returnmsg_builder.setReturnStr("[JOBCD:"+jobCd+"]등록되지 않았거나 권한이없는 업무입니다. (TOOL:eClipse)");
								return returnmsg_builder.build();
							//}
						}

					}else{
						jobCd = ecamsmsg.getSysinfo().getSyscd();
					}
					
					
					HashMap<String, String> newList = new HashMap<String, String>();

					newList.put("dirpath",fileDataList.getFiledatas(i).getPathinfo().getRelativitePath());
					//newList.put("syscd", fileDataList.getFiledatas(i).getSysinfo().getSyscd());
					newList.put("syscd", ecamsmsg.getSysinfo().getSyscd());
//					newList.put("jobcd", fileDataList.getFiledatas(i).getJobinfo().getJobcd());
//					newList.put("jobcd", ecamsmsg.getJobinfolist().getJobinfo(i).getJobcd());
					newList.put("jobcd", jobCd);
					newList.put("userid", ecamsmsg.getUserinfo().getId());
					newList.put("rsrcname", fileDataList.getFiledatas(i).getFilename());
					newList.put("sayu", ecamsmsg.getRequestinfo().getSayu());
					itemid = fileService.registCheckInFile(newList);
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
        		
        		hasJobcd = false;
    			for(int jobCnt=0;jobCnt<jobList.size(); jobCnt++){
    				if(jobList.get(jobCnt).getJobcd().equals(fileinfo.get("CR_JOBCD").toString())){
    					hasJobcd = true;
    					break;
    				}
    			}
        		if(!hasJobcd){
        			System.out.println(">>>FAIL JOBCD IS NULL["+jobCd+"]");
					throw new Exception("권한이 없는 업무입니다. (업무코드:"+jobCd+")");
        		}
        		
        		rsrcinfo = null;
        		rsrcinfo = resourceTypeService.getRsrcInfo_detail(ecamsmsg.getSysinfo().getSyscd(),(String)fileinfo.get("CR_RSRCCD"));
        		if (rsrcinfo == null){
        			throw new Exception("No data rsrcinfo");
        		}
        		
        		
				FileData.Builder fileData_builder = FileData.newBuilder(fileDataList.getFiledatas(i));    			
				fileData_builder.setItemid(itemid);
				
				if( fileDataList.getFiledatas(i).hasBaseitem() ) {
					baseitem = fileDataList.getFiledatas(i).getBaseitem();
				} else {
					baseitem = itemid;
				}
				
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
        			
        			if(null == srid){
        				param.put("CR_PASSCD", "이클립스 체크인");
        			}else{
        				param.put("CR_PASSCD", "["+ecamsmsg.getSrinfo().getCcSRId()+"]"+ecamsmsg.getSrinfo().getCcTitle());
            			param.put("CR_ITSMID", ecamsmsg.getSrinfo().getCcSRId());
            			param.put("CR_ITSMTITLE", ecamsmsg.getSrinfo().getCcTitle());
        			}
        			
        			param.put("CR_EMGCD", ecamsmsg.getRequestinfo().getReqgbn());
        			param.put("CR_PASSSUB", "0");
        			if(ecamsmsg.getRequestinfo().getReqgbn()!=null && !ecamsmsg.getRequestinfo().getReqgbn().equals("")){
	        			if(!ecamsmsg.getRequestinfo().getReqgbn().equals("11")){
	        				param.put("CR_DOCNO", ecamsmsg.getRequestinfo().getGbnsayu());
	        			}
        			}
        			param.put("CR_EDITOR", ecamsmsg.getUserinfo().getId());
        			param.put("CR_SAYU", ecamsmsg.getRequestinfo().getSayu());
        			//param.put("CR_SAYUCD", ecamsmsg.getRequestinfo().getGbnsayu());
        			//param.put("CR_ITSMID", ecamsmsg.getPrjinfolist().getPrjinfo(0).getScsrno());
        			//param.put("CR_ITSMTITLE", ecamsmsg.getPrjinfolist().getPrjinfo(0).getScsrtitle());
        			param.put("CR_SAYUCD", "9");
        			param.put("CR_BEFJOB", "N");
        			
        			param.put("CR_VERSION", ecamsmsg.getRequestinfo().getVersion());//버전업:Y, 버전업미적용:N
        			param.put("CR_SVRYN", ecamsmsg.getRequestinfo().getSvrYN());//개발서버적용:Y, 개발서버미적용:N
        			
        			
					if (insertCmr1000(param) <= 0){
        				throw new Exception("Cmr1000 Insert Error");
        			}
        			param = null;
        			seq = 0;
        			
        			cmr1000Flg = false;
        		}

        		String qrycd="";
        		if(((BigDecimal)fileinfo.get("CR_LSTVER")).intValue()<1){
        			qrycd="03";
        		}else{
        			qrycd="04";
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
    			param.put("CR_TSTCHG", ecamsmsg.getRequestinfo().getTstchg());
    			
    			param.put("CR_SRCCHG", "1");
    			param.put("CR_SRCCMP", "Y");
    			
    			param.put("CR_PRIORITY", rsrcinfo.get("CM_STEPSTA"));

				param.put("CR_VERSION", ((BigDecimal)fileinfo.get("CR_LSTVER")).intValue()+1);
    			param.put("CR_BEFVER", ((BigDecimal)fileinfo.get("CR_LSTVER")).intValue());
    			
    			param.put("CR_EDITOR", ecamsmsg.getUserinfo().getId());
    			param.put("CR_BASEITEM", baseitem);
    			param.put("CR_ITEMID", itemid);
    			param.put("CR_EDITCON", ecamsmsg.getRequestinfo().getSayu());
    			param.put("CR_PGMTYPE", fileinfo.get("CR_PGMTYPE"));

    			param.put("CR_VERYN", ecamsmsg.getRequestinfo().getVersion());//버전업:Y, 버전업미적용:N
    			param.put("CR_SVRYN", ecamsmsg.getRequestinfo().getSvrYN());//개발서버적용:Y, 개발서버미적용:N
    			

				param.put("CR_BEFVIEWVER", (String)fileinfo.get("CR_VIEWVER"));
				
				
				viewver = (String)fileinfo.get("CR_VIEWVER");
				if (null == viewver) viewver = "0.0.0";
				
				String aftver = viewver.substring(viewver.lastIndexOf(".")+1);
				aftver = viewver.substring(0, viewver.lastIndexOf("."))+"."+(Integer.parseInt(aftver)+1);
				param.put("CR_AFTVIEWVER", aftver);
				
    			if (insertCmr1010(param)<=0){
    				throw new Exception("Cmr1010 Insert Error");
    			}
    			param = null;
			}
        	
        	if (!confirmService.request_confirm(acptNo, ecamsmsg.getSysinfo().getSyscd(), tmp, ecamsmsg.getUserinfo().getId(), null)){
			//if (!confirmService.request_confirm(acptNo, ecamsmsg.getSysinfo().getSyscd(), ecamsmsg.getRequestinfo().getQrycd(), ecamsmsg.getUserinfo().getId(), null)){
				logger.error("CheckInRequestService 318:" + acptNo);
				throw new Exception("결재정보등록 중 오류가 발생하였습니다. 관리자에게 연락하여 주시기 바랍니다.");
			}
			List result = null;
			param = new HashMap();
			param.put("acptno",acptNo);
			result = checkInDAO.updateCmr1010(param);
			
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
			logger.error("RequestCheckIn.request: "+ecamsmsg.getUserinfo().getId()+" "+e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getError());
			//txManager.rollback(status);
		}
		catch (Exception e){
			logger.error("RequestCheckIn.request: "+ecamsmsg.getUserinfo().getId()+" "+e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}
		return returnmsg_builder.build();
	}
	
	
	@Transactional (readOnly = false, propagation = Propagation.REQUIRED, rollbackForClassName={"app.core.exception.RollBackException"})
	public ReturnMsg transfile(EcamsMessage ecamsmsg) {
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		FileDataList.Builder fileDataList_builder = FileDataList.newBuilder();
		TransactionStatus status = txManager.getTransaction(defaultTransactionDefinition);
		int i,datalength;
		
		try{
			FileDataList fileDataList = ecamsmsg.getFiledatalist();
			datalength = fileDataList.getFiledatasCount();
			HashMap fileinfo=null;
    		String md5sum = "";
    		
			String acptNo = ecamsmsg.getRequestinfo().getAcptno();
			for (i=0;i<datalength;i++){
				fileinfo = fileService.getFileInfo(fileDataList.getFiledatas(i).getItemid());
        		if (fileinfo == null || fileinfo.equals("")){
        			throw new Exception("fileinfo get error - itemid=["+fileDataList.getFiledatas(i).getItemid()+"]");
        		}
        		
        		
        		/*
        		스패로우 hash값 테스트
        		try{
	        		File filez = new File("/ecams/data_logs/test/"+fileDataList.getFiledatas(i).getFilename());

					logger.error(">>>>>>>>>>>>>>test filez:"+filez); ///ecams/data_logs/test/temp4.java
					
	        		if (!filez.exists()) filez.createNewFile();
	        		FileOutputStream fw = new FileOutputStream(filez);
					fw.write(fileDataList.getFiledatas(i).getFilebytes().toByteArray());
					fw.flush();
					fw.close();
					
					String hash = SHA1HashGenerator.getInstance().getValue(filez);
					logger.error(">>>>>>>>>>>>>>SHA1HASH:"+hash); //162cfe217146013a48788fb975353b8513ffcc4d
					hash = MD5HashGenerator.getInstance().getValue(filez); 
					logger.error(">>>>>>>>>>>>>>Sparrow MD5:"+hash); //d58ac6794ab45b07c5c747bdb8a1a0a0
					logger.error(">>>>>>>>>>>>>>eCAMS MD5:"+fileDataList.getFiledatas(i).getMd5Sum()); //d58ac6794ab45b07c5c747bdb8a1a0a0
					
        		}catch(Exception e) {
        			logger.error(e);
        		}
        		*/
        		
	    		md5sum = CheckSum.MD5SumVal(fileDataList.getFiledatas(i).getFilebytes().toByteArray());
	    		
	    		if(fileDataList.getFiledatas(i).getMd5Sum().equals(md5sum)){
	    			HashMap param = new HashMap();
	        		param.put("CR_ACPTNO",(String)acptNo);
	        		param.put("CR_ITEMID",(String)fileDataList.getFiledatas(i).getItemid());
	        		param.put("CR_FILE",(byte[])gzip.getCompressedByte(fileDataList.getFiledatas(i).getFilebytes().toByteArray()));

	        		param.put("CR_VER",(int)((BigDecimal)fileinfo.get("CR_LSTVER")).intValue()+1);
	        		
	        		param.put("CR_FILESIZE",(int)fileDataList.getFiledatas(i).getFilebytes().size());
	        		param.put("CR_FILEDATE",fileDataList.getFiledatas(i).getLstdate());
	        		param.put("CR_MD5SUM",(String)fileDataList.getFiledatas(i).getMd5Sum());
					
					if (fileService.insertFileData(param) <= 0){
						FileData.Builder fileData_builder = FileData.newBuilder();
		    			fileData_builder.setItemid(fileDataList.getFiledatas(i).getItemid());
						fileData_builder.setFilename(fileDataList.getFiledatas(i).getFilename());
						fileDataList_builder.addFiledatas(fileData_builder.build());
						EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
						builder_msg.setMsgtype(ecamsmsg.getMsgtype());
						builder_msg.setFiledatalist(fileDataList_builder.build());
						returnmsg_builder.setReturnval(1);
						returnmsg_builder.setReturnStr("Cmr0027 Insert Error");
						returnmsg_builder.setEcamsmsg(builder_msg.build());
	    				throw new RollBackException("Cmr0027 Insert Error");
	    			}
					param=null;
					
	    		}else{
	    			FileData.Builder fileData_builder = FileData.newBuilder();
	    			fileData_builder.setItemid(fileDataList.getFiledatas(i).getItemid());
					fileData_builder.setFilename(fileDataList.getFiledatas(i).getFilename());
					fileDataList_builder.addFiledatas(fileData_builder.build());
	    		}
	    		
			}
			returnmsg_builder.setReturnval(0);
			returnmsg_builder.setReturnStr("정상");
			if (fileDataList_builder.getFiledatasCount()>0){
				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype(ecamsmsg.getMsgtype());
				builder_msg.setFiledatalist(fileDataList_builder.build());
				returnmsg_builder.setEcamsmsg(builder_msg.build());
			}
			txManager.commit(status);
		}
		catch (RollBackException e){
			logger.error("RequestCheckIn.request: ",e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getError());
			txManager.rollback(status);
		}
		catch (Exception e){
			logger.error("RequestCheckIn.request: ",e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}
		return returnmsg_builder.build();
	}
	
	
	//splitFileSend
	@Transactional (readOnly = false, propagation = Propagation.REQUIRED, rollbackForClassName={"app.core.exception.RollBackException"})
	public ReturnMsg splitFileSend(EcamsMessage ecamsmsg) {
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		FileDataList.Builder fileDataList_builder = FileDataList.newBuilder();
		TransactionStatus status = txManager.getTransaction(defaultTransactionDefinition);
		
		try{

			//FileDataList fileDataList = ecamsmsg.getFiledatalist();
			FileData fileData = ecamsmsg.getFiledata();

			String acptNo = ecamsmsg.getRequestinfo().getAcptno();
			
			String tmpPath = pathDAO.getTempPath("99")+"/"+acptNo;
			File tmpFolder = new File(tmpPath);
			if (!tmpFolder.isDirectory()) {
				tmpFolder.mkdirs();
			}
			String itemId = fileData.getItemid();
			File tmpFile = new File(tmpPath+"/"+itemId);
			if (ecamsmsg.getPagenum() == 1 && tmpFile.exists()) {
				tmpFile.delete();
			}
			if (!tmpFile.exists()) {
				tmpFile.createNewFile();
			}
			boolean appendFlg = false;
			if (ecamsmsg.getPagenum() > 1) {
				appendFlg = true;
			}
			logger.error("appendFlg:"+appendFlg);
			
			byte[] filebyte = fileData.getFilebytes().toByteArray();

			logger.error("tmpFile write end1:"+filebyte.length);
			
			FileOutputStream fo = new FileOutputStream(tmpFile, appendFlg);
			fo.write(filebyte);
			fo.close();
			fo = null;
			
			logger.error("tmpFile write end:"+tmpFile);
			
			if (ecamsmsg.getPagenum() == ecamsmsg.getTotpage()) {//마지막옴
				HashMap fileinfo=null;
				
				fileinfo = fileService.getFileInfo(fileData.getItemid());
	    		if (fileinfo == null || fileinfo.equals("")){
	    			throw new Exception("fileinfo get error - itemid=["+fileData.getItemid()+"]");
	    		}

	    		//md5sum = CheckSum.MD5SumVal(fileData.getFilebytes().toByteArray());
	    		String md5sum = CheckSum.MD5SumVal(tmpFile);
				logger.error("md5sum:"+md5sum);
	    		
	    		if(fileData.getMd5Sum().equals(md5sum)){
	    			HashMap param = new HashMap();
	        		param.put("CR_ACPTNO",(String)acptNo);
	        		param.put("CR_ITEMID",(String)fileData.getItemid());
	        		//param.put("CR_FILE",(byte[])gzip.getCompressedByte(fileData.getFilebytes().toByteArray()));
	        		
	        		filebyte = FileToByteArray.FileToByteArray(tmpFile);
	    			logger.error("tmpFile write end2:"+filebyte.length);
	        		param.put("CR_FILE",(byte[])gzip.getCompressedByte(filebyte));

	        		param.put("CR_VER",(int)((BigDecimal)fileinfo.get("CR_LSTVER")).intValue()+1);
	        		
	        		//param.put("CR_FILESIZE",(int)fileData.getFilebytes().size());
	        		param.put("CR_FILESIZE",(int)tmpFile.length());
	        		param.put("CR_FILEDATE",fileData.getLstdate());
	        		param.put("CR_MD5SUM",(String)fileData.getMd5Sum());
					
					if (fileService.insertFileData(param) <= 0){
						FileData.Builder fileData_builder = FileData.newBuilder();
		    			fileData_builder.setItemid(fileData.getItemid());
						fileData_builder.setFilename(fileData.getFilename());
						fileDataList_builder.addFiledatas(fileData_builder.build());
						EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
						builder_msg.setMsgtype(ecamsmsg.getMsgtype());
						builder_msg.setFiledatalist(fileDataList_builder.build());
						returnmsg_builder.setReturnval(1);
						returnmsg_builder.setReturnStr("Cmr0027 Insert Error");
						returnmsg_builder.setEcamsmsg(builder_msg.build());
	    				throw new RollBackException("Cmr0027 Insert Error");
	    			}
					param=null;
					
	    		}else{
	    			FileData.Builder fileData_builder = FileData.newBuilder();
	    			fileData_builder.setItemid(fileData.getItemid());
					fileData_builder.setFilename(fileData.getFilename());
					fileDataList_builder.addFiledatas(fileData_builder.build());
	    		}
		    		
				returnmsg_builder.setReturnval(0);
				returnmsg_builder.setReturnStr("정상");
				if (fileDataList_builder.getFiledatasCount()>0){
					EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
					builder_msg.setMsgtype(ecamsmsg.getMsgtype());
					builder_msg.setFiledatalist(fileDataList_builder.build());
					returnmsg_builder.setEcamsmsg(builder_msg.build());
				}
				txManager.commit(status);
				
			} else {
				returnmsg_builder.setReturnval(0);
				returnmsg_builder.setReturnStr("정상");
			}
		}
		catch (RollBackException e){
			logger.error("RequestCheckIn.splitFileSend: ",e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getError());
			txManager.rollback(status);
		}
		catch (Exception e){
			logger.error("RequestCheckIn.splitFileSend: ",e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}
		return returnmsg_builder.build();
	}
	
	
	@Transactional (readOnly = false, propagation = Propagation.REQUIRED, rollbackForClassName={"app.core.exception.RollBackException"})
	public ReturnMsg getDownFileList(EcamsMessage ecamsmsg) {
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		TransactionStatus status = txManager.getTransaction(defaultTransactionDefinition);
		int i=0;
    	int k=0;
    	
		try{
			FileDataList fileDataList = ecamsmsg.getFiledatalist();
			List<FileInfo> fileinfo = null;
			List<CheckInfo> sameRsrcList = null;
			
			HashMap<String, String> param = new HashMap<String, String>();
			
			HashMap<String,String> rst = new HashMap<String,String>();
			ArrayList<HashMap<String, String>> rtList = new ArrayList<HashMap<String, String>>();
			int addCnt = 0;
			int svCnt = 0;
			
			String strWork1 = "";
			String strWork3 = "";
			String strDirPath = "";
			
			boolean ErrSw = false;
			
			rtList.clear();
			for (i=0;i<fileDataList.getFiledatasCount();i++){
				fileinfo = (List<FileInfo>) fileDAO.getAllFileList(fileDataList.getFiledatas(i).getItemid());
				
        		if (fileinfo == null || fileinfo.equals("")){
        			throw new Exception("fileinfo get error - itemid=["+fileDataList.getFiledatas(i).getItemid()+"]");
        		}
        		
        		rst = new HashMap<String,String>();
				rst.put("mod","P");
				rst.put("rsrcname",fileinfo.get(0).getRsrcname());
				rst.put("itemid",fileinfo.get(0).getItemid());
				rst.put("baseitem",fileinfo.get(0).getItemid());
				rst.put("dirpath",fileinfo.get(0).getDirpath());
				rst.put("rsrccd",fileinfo.get(0).getResourceType());
				rst.put("rsrctypename",fileinfo.get(0).getResourceTypeName());
				rst.put("jobcd", fileinfo.get(0).getJobcd());
				rst.put("cminfo", fileinfo.get(0).getCm_info());
				rtList.add(addCnt++, rst);
				rst = null;

				svCnt = addCnt - 1;
				
        		ErrSw = false;
        		if(fileinfo.get(0).getCm_info().substring(3,4).equals("1")) {
        			sameRsrcList = null;
        			
        			param = new HashMap<String, String>();
        			param.put("syscd", fileinfo.get(0).getCmSyscd());
        			param.put("rsrccd", fileinfo.get(0).getResourceType());
        			
        			sameRsrcList = (List<CheckInfo>) checkInDAO.getSameRsrc(param);
        			param = null;
        			
        			if(sameRsrcList != null){
        				for(int j=0; j<sameRsrcList.size(); j++){
        					ErrSw = false;
        					
        					if (fileinfo.get(0).getRsrcname().lastIndexOf(".") > 0) {
    			        		strWork1 = fileinfo.get(0).getRsrcname().substring(0,fileinfo.get(0).getRsrcname().lastIndexOf("."));
    			        	} else {
    			        		strWork1 = fileinfo.get(0).getRsrcname();
    			        	}
        					
        					if (sameRsrcList.get(j).getSamename().indexOf("?#")>=0) {
        						
        						param = new HashMap<String, String>();
        	        			param.put("INRSRCNAME", fileinfo.get(0).getRsrcname());
        	        			param.put("INDIRPATH", fileinfo.get(0).getDirpath());
        	        			param.put("INSAMENAME", sameRsrcList.get(j).getSamename());
        	        			param.put("INSYSCD", fileinfo.get(0).getCmSyscd());
        	        			param.put("INBASEITEM", fileinfo.get(0).getItemid());
        	        			param.put("INACPTNO", "");
        	        			param.put("INRSRCCD", fileinfo.get(0).getResourceType());
      	   					  	
        						strWork3 = checkInDAO.getNameChange(param);
        						param = null;
    			        		if (strWork3.startsWith("ERROR")) {
    			        			for (k=rtList.size()-1;k>=svCnt;k--) {
    									rtList.remove(k);
    								}

    			        			addCnt = addCnt - 1;
    			        			rst = new HashMap<String,String>();
    			    				rst.put("rsrcname",fileinfo.get(0).getRsrcname());
    			    				rst.put("itemid","ERROR");
    			    				rst.put("baseitem",fileinfo.get(0).getItemid());
    			    				rst.put("dirpath","["+fileinfo.get(0).getRsrcname()+"]에 대한 동시적용모듈정보가 정확하지 않습니다.\n"+strWork3.substring(5));
    			    				rst.put("rsrccd",fileinfo.get(0).getResourceType());
    			    				rst.put("rsrctypename",fileinfo.get(0).getResourceTypeName());
    			    				rst.put("jobcd", fileinfo.get(0).getJobcd());
    			    				rtList.add(svCnt, rst);
    			    				rst = null;
    	    						
    			        			ErrSw = true;
    			        		}
        					} else if (sameRsrcList.get(j).getSamename().equals("*.*")) {	
    			        		strWork3 = fileinfo.get(0).getRsrcname();
    			        	} else {	
    			        		strWork3 = sameRsrcList.get(j).getSamename().replace("*", strWork1);
    			        	}
        					
        					strDirPath = fileinfo.get(0).getDirpath();
        					if(sameRsrcList.get(j).getBasedir() != null){
        						if (!sameRsrcList.get(j).getBasedir().equals(sameRsrcList.get(j).getSamedir())){
        							if( !sameRsrcList.get(j).getBasedir().equals("*") ) {
        								if( strDirPath.indexOf(sameRsrcList.get(j).getBasedir()) < 0 ){
        									continue;
        								}
        							}
        							
        							if (sameRsrcList.get(j).getBasedir().equals("*")) strDirPath = sameRsrcList.get(j).getSamedir();
    			        			else if (sameRsrcList.get(j).getSamedir().indexOf("?#")>=0) {
    	        	        			param = new HashMap<String, String>();
    	        	        			param.put("INRSRCNAME", fileinfo.get(0).getRsrcname());
    	        	        			param.put("INDIRPATH", fileinfo.get(0).getDirpath());
    	        	        			param.put("INSAMENAME", sameRsrcList.get(j).getSamedir());
    	        	        			param.put("INSYSCD", fileinfo.get(0).getCmSyscd());
    	        	        			param.put("INBASEITEM", fileinfo.get(0).getItemid());
    	        	        			param.put("INACPTNO", "");
    	        	        			param.put("INRSRCCD", fileinfo.get(0).getResourceType());
    	      	   					  	
    	        						strWork3 = checkInDAO.getNameChange(param);
    	        						param = null;
    	        						if (strWork3.startsWith("ERROR")) {
    	    			        			for (k=rtList.size()-1;k>=svCnt;k--) {
    	    									rtList.remove(k);
    	    								}

    	    			        			addCnt = addCnt - 1;
    	    			        			rst = new HashMap<String,String>();
    	    			    				rst.put("rsrcname",fileinfo.get(0).getRsrcname());
    	    			    				rst.put("itemid","ERROR");
    	    			    				rst.put("baseitem",fileinfo.get(0).getItemid());
    	    			    				rst.put("dirpath","["+fileinfo.get(0).getRsrcname()+"]에 대한 동시적용모듈정보가 정확하지 않습니다.\n"+strWork3.substring(5));
    	    			    				rst.put("rsrccd",fileinfo.get(0).getResourceType());
    	    			    				rst.put("rsrctypename",fileinfo.get(0).getResourceTypeName());
    	    			    				rst.put("jobcd", fileinfo.get(0).getJobcd());
    	    			    				rtList.add(svCnt, rst);
    	    			    				rst = null;
            	    						
    	    			        			ErrSw = true;
    	    			        		}
    			        			} else {
    			        				strDirPath = strDirPath.replace(sameRsrcList.get(j).getBasedir(), sameRsrcList.get(j).getSamedir());
    			        			}
        						}else {
        							if( sameRsrcList.get(j).getBasename().equals("*") && sameRsrcList.get(j).getSamename().equals("*.*") ){
        								continue;
        							}
        						}
        					}
        					
        					if (!ErrSw) {
        						String itemid = null;
        						
        						param = new HashMap<String, String>();
        	        			param.put("syscd", fileinfo.get(0).getCmSyscd());
        	        			param.put("rsrccd", sameRsrcList.get(j).getSamersrc());
        	        			param.put("rsrcname", strWork3);
        	        			
        	        			if (fileinfo.get(0).getCm_info().substring(25,26).equals("0") || !strDirPath.equals("*")) {
        	        				param.put("dirpath", strDirPath);
        	        			}
        	        			itemid = fileDAO.getFind_Itemid(param);
        	        			param = null;
        	        			
        	        			if (null == itemid) {
        	        				if (fileinfo.get(0).getCm_info().substring(25,26).equals("1")) continue;
        	        				
        	        				String DsnCd = null;
            						param = new HashMap<String, String>();
            	        			param.put("cm_syscd", fileinfo.get(0).getCmSyscd());
            	        			param.put("cm_rsrccd", sameRsrcList.get(j).getSamersrc());
            	        			param.put("cm_jobcd", fileinfo.get(0).getJobcd());
            	        			param.put("dirpath", strDirPath);
            	        			param.put("cm_userid", ecamsmsg.getUserinfo().getId());
            	        			
            	        			//DsnCd = pathDAO.getPathCD(param);
	            	        		DsnCd = pathService.getPathCD(param);
            	        			param = null;
            	        			
            	        			if (null == DsnCd) {
	    			        			for (k=rtList.size()-1;k>=svCnt;k--) {
	    									rtList.remove(k);
	    								}

	    			        			addCnt = addCnt - 1;
	    			        			rst = new HashMap<String,String>();
	    			    				rst.put("rsrcname",fileinfo.get(0).getRsrcname());
	    			    				rst.put("itemid","ERROR");
	    			    				rst.put("baseitem",fileinfo.get(0).getItemid());
	    			    				rst.put("dirpath","["+strDirPath+"]에 대한 디렉토리등록에 실패하였습니다.");
	    			    				rst.put("rsrccd",fileinfo.get(0).getResourceType());
	    			    				rst.put("rsrctypename",fileinfo.get(0).getResourceTypeName());
	    			    				rst.put("jobcd", fileinfo.get(0).getJobcd());
	    			    				rtList.add(svCnt, rst);
	    			    				rst = null;
        	    						
            	        				ErrSw = true;
            	        			} else {
            	        				param = new HashMap<String, String>();
            	        				//param.put("dirpath","/"+ecamsmsg.getFiledatalist().getFiledatas(i).getPathinfo().getRelativitePath());
            	        				param.put("dsncd", DsnCd);
            	        				param.put("syscd", fileinfo.get(0).getCmSyscd());
            	        				param.put("jobcd", fileinfo.get(0).getJobcd());
            	        				param.put("userid", ecamsmsg.getUserinfo().getId());
            	        				param.put("rsrcname", strWork3);
            	        				param.put("sayu", "자동신규등록");
            	        				param.put("srid", "");
            	        				param.put("rsrccd", sameRsrcList.get(j).getSamersrc());
            	    					
            	        				itemid = fileService.registCheckInFile(param);
            	    					param = null;
            	    					
            	    					if(itemid == null){
    	    			        			for (k=rtList.size()-1;k>=svCnt;k--) {
    	    									rtList.remove(k);
    	    								}

    	    			        			addCnt = addCnt - 1;
    	    			        			rst = new HashMap<String,String>();
    	    			    				rst.put("rsrcname",fileinfo.get(0).getRsrcname());
    	    			    				rst.put("itemid","ERROR");
    	    			    				rst.put("baseitem",fileinfo.get(0).getItemid());
    	    			    				rst.put("dirpath","["+strWork3+"]에 대한 프로그램등록에 실패하였습니다.");
    	    			    				rst.put("rsrccd",fileinfo.get(0).getResourceType());
    	    			    				rst.put("rsrctypename",fileinfo.get(0).getResourceTypeName());
    	    			    				rst.put("jobcd", fileinfo.get(0).getJobcd());
    	    			    				rtList.add(svCnt, rst);
    	    			    				rst = null;
            	    						
    										ErrSw = true;
            	    					}
            	        			}
        	        			}
        	        			if (!ErrSw) {
        	        				List<FileInfo> samefileinfo = (List<FileInfo>) fileDAO.getAllFileList(itemid);
        	        				
        	        				if (null != samefileinfo) {
        	        					boolean fileSw = false;
        	        					
    	        						fileSw = false;
        	        					if (samefileinfo.get(0).getCm_info().substring(25,26).equals("1")) {
        	        						param = new HashMap<String, String>();
                	        				param.put("itemid", fileinfo.get(0).getItemid());
                	        				param.put("makeitem", samefileinfo.get(0).getItemid());
        	        						if(checkInDAO.getCmd0010Cnt(param) > 0){
        	        							fileSw = true;
        	        						}
        	        						param = null;
        	        					}
        	        					
        	        					if (!fileSw) {
        	        						for (k=0; k<fileDataList.getFiledatasCount(); k++) {
    						            		if (fileDataList.getFiledatas(k).getItemid().equals(samefileinfo.get(0).getItemid())) {
    						            			fileSw = true;
    						            			break;
    						            		}
    						            	}
        	        					}
        	        					
        	        					if (!fileSw) {
        	        						rst = new HashMap<String,String>();
        	        						rst.put("mod","M");
        	        						rst.put("rsrcname",samefileinfo.get(0).getRsrcname());
        	        						rst.put("itemid",samefileinfo.get(0).getItemid());
        	        						rst.put("baseitem",fileinfo.get(0).getItemid());
        	        						rst.put("dirpath",samefileinfo.get(0).getDirpath());
        	        						rst.put("rsrccd",samefileinfo.get(0).getResourceType());
        	        						rst.put("rsrctypename",samefileinfo.get(0).getResourceTypeName());
        				    				rst.put("jobcd", fileinfo.get(0).getJobcd());
        				    				rst.put("cminfo", samefileinfo.get(0).getCm_info());
        	        						rtList.add(addCnt++, rst);
        	        						rst = null;
        	        					}
        	        					
        	        					samefileinfo = null;
        	        					
        	        				} else {
	    			        			for (k=rtList.size()-1;k>=svCnt;k--) {
	    									rtList.remove(k);
	    								}

	    			        			addCnt = addCnt - 1;
	    			        			rst = new HashMap<String,String>();
	    			    				rst.put("rsrcname",fileinfo.get(0).getRsrcname());
	    			    				rst.put("itemid","ERROR");
	    			    				rst.put("baseitem",fileinfo.get(0).getItemid());
	    			    				rst.put("dirpath","프로그램정보를 찾을 수가 없습니다.");
	    			    				rst.put("rsrccd",fileinfo.get(0).getResourceType());
	    			    				rst.put("rsrctypename",fileinfo.get(0).getResourceTypeName());
	    			    				rst.put("jobcd", fileinfo.get(0).getJobcd());
	    			    				rtList.add(svCnt, rst);
	    			    				rst = null;
        	    						
        	        					ErrSw = true;
        	        				}
        	        			}
        					}
        				}
        			}
        		}
        		
        		if (!ErrSw && fileinfo.get(0).getCm_info().substring(8,9).equals("1")) {
        			int readCnt = 0;
        			
        			List<FileInfo> sameModinfo = (List<FileInfo>) fileDAO.getExecModList(fileinfo.get(0).getItemid());
        			
        			if(null != sameModinfo){
        				for(int j=0; j<sameModinfo.size(); j++){
			            	boolean fileSw = false;
			            	++readCnt;
			            	
	        				for(k=0; k<rtList.size(); k++){
	    		            	if (rtList.get(k).get("itemid").equals(sameModinfo.get(j).getItemid())) {
	    		            		fileSw = true;
	    		            		break;
	    		            	}
	        				}
	        				if (!fileSw) {
	    						rst = new HashMap<String,String>();
	    						rst.put("mod","M");
	    						rst.put("rsrcname",sameModinfo.get(j).getRsrcname());
	    						rst.put("itemid",sameModinfo.get(j).getItemid());
	    						rst.put("baseitem",fileinfo.get(0).getItemid());
	    						rst.put("dirpath",sameModinfo.get(j).getDirpath());
	    						rst.put("rsrccd",sameModinfo.get(j).getResourceType());
	    						rst.put("rsrctypename",sameModinfo.get(j).getResourceTypeName());
			    				rst.put("jobcd", fileinfo.get(0).getJobcd());
			    				rst.put("cminfo", sameModinfo.get(0).getCm_info());
	    						rtList.add(addCnt++, rst);
	    						rst = null;
	        				}
        				}
        			}
        			
        			if (readCnt == 0) {
	        			for (k=rtList.size()-1;k>=svCnt;k--) {
							rtList.remove(k);
						}
	        			
	        			addCnt = addCnt - 1;
	        			rst = new HashMap<String,String>();
	    				rst.put("rsrcname",fileinfo.get(0).getRsrcname());
	    				rst.put("itemid","ERROR");
	    				rst.put("baseitem",fileinfo.get(0).getItemid());
	    				rst.put("dirpath","실행모듈정보를 찾을 수가 없습니다.");
	    				rst.put("rsrccd",fileinfo.get(0).getResourceType());
	    				rst.put("rsrctypename",fileinfo.get(0).getResourceTypeName());
	    				rst.put("jobcd", fileinfo.get(0).getJobcd());
	    				rtList.add(svCnt, rst);
	    				rst = null;
						
        				ErrSw = true;
        			}
        			sameModinfo = null;
        		}
        		fileinfo = null;
			}
			

			FileDataList.Builder fileDataList_builder = FileDataList.newBuilder();
			if(rtList.size() > 0){
				ArrayList<HashMap<String, String>> filteredList = new ArrayList<>();
				HashSet<String> uniqueItemIds = new HashSet<>();
				
				 for (HashMap<String, String> item : rtList) {
		            String itemId = item.get("itemid");
		            if (!uniqueItemIds.contains(itemId)) {
		                uniqueItemIds.add(itemId);
		                filteredList.add(item);
		            }
		        }
				rtList = null;
				rtList = filteredList;
				
				for(k=0; k<rtList.size(); k++){
					FileData.Builder filedata_builder = FileData.newBuilder();
		    		filedata_builder = FileData.newBuilder();
					filedata_builder.setFilename(rtList.get(k).get("rsrcname"));
					filedata_builder.setItemid(rtList.get(k).get("itemid"));
					filedata_builder.setBaseitem(rtList.get(k).get("baseitem"));
					
					PathInfo.Builder pathinfo_builder = PathInfo.newBuilder();
					
					param = new HashMap<String, String>();
					
					param.put("cm_syscd", ecamsmsg.getSysinfo().getSyscd());
					param.put("cm_rsrccd", rtList.get(k).get("rsrccd"));
					param.put("cm_jobcd", rtList.get(k).get("jobcd"));
					String basepath = pathDAO.getBasePath(param);
					param = null;

					String tmpPath = rtList.get(k).get("dirpath");
//					System.out.println("===tmpPath==["+tmpPath+"]==basepath==["+basepath+"]"); // 20201231 로그 찍는거 삭제해버리기
					if (null == basepath || "".equals(basepath)) {
						throw new Exception("체크아웃서버 프로그램종류 연결정보 등록확인 rsrccd["+rtList.get(k).get("rsrccd")+"]");
					}
					
					if ( tmpPath.equals(basepath) || rtList.get(k).get("itemid").equals("ERROR") ) {
						pathinfo_builder.setRelativitePath(rtList.get(k).get("dirpath"));
					} else {
						pathinfo_builder.setRelativitePath(tmpPath.substring(basepath.length()));
					}
					//pathinfo_builder.setRelativitePath(rtList.get(k).get("dirpath"));
					filedata_builder.setPathinfo(pathinfo_builder.build());
					pathinfo_builder = null;
					
					RsrcInfo.Builder rsrcinfo_builder = RsrcInfo.newBuilder();
					rsrcinfo_builder.setRsrccd(rtList.get(k).get("rsrccd"));
					rsrcinfo_builder.setRsrcmsg(rtList.get(k).get("rsrctypename"));
					rsrcinfo_builder.setCminfo(rtList.get(k).get("cminfo"));
					filedata_builder.setRsrcinfo(rsrcinfo_builder.build());
					rsrcinfo_builder = null;
					
					fileDataList_builder.addFiledatas(filedata_builder.build());
					filedata_builder = null;
				}
			}

			if(fileDataList_builder.getFiledatasCount() > 0){
				returnmsg_builder.setReturnval(0);
				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype(ecamsmsg.getMsgtype());
				builder_msg.setFiledatalist(fileDataList_builder.build());
				returnmsg_builder.setEcamsmsg(builder_msg.build());
			}else{
				returnmsg_builder.setReturnval(1);
			}
			returnmsg_builder.setReturnStr("정상");
			
			txManager.commit(status);
		}
		catch (RollBackException e){
			logger.error("getDownFileList: ",e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getError());
			txManager.rollback(status);
		}
		catch (Exception e){
			logger.error("getDownFileList: ",e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getMessage());
		}
		return returnmsg_builder.build();
	}	
	
	

	/*
	//취약점점검 결과확인
	public String sparrowChk(String acptNo) throws Exception {
		
		try {
	        // 3-1. 입력 목록 생성
	        List<VcsFileInspectDto> list = new ArrayList<VcsFileInspectDto>();
	        // 입력값 정의
	        // - key값 : 목록을 입력했을 경우 입력된 데이터와 결과을 매핑하기 위해서 사용 , 예)"1"
	        // - 파일명 : 파일 전체 경로 혹은 파일명, 예) "admin.jsp"
	        // - 해쉬값 : 파일 내용의 해쉬값, 예) "04a507f7da17e102073d4f8f6413f05a051684e4"
	        // 입력값: 키값, 파일명, 해쉬값
	        
	        List<HashMap<String,String>> inspectList = checkInDAO.selectFileInspectList(acptNo);
	
			for (HashMap<String,String> inspect:inspectList) {
	        	//key, file, hash
	        	list.add(new VcsFileInspectDto(inspect.get("CR_SERNO"), inspect.get("CR_RSRCNAME"), inspect.get("CR_MD5SUM")));
	        }
			
			if (null == list || list.size() == 0) {
				inspectList = null;
				list = null;
				return "OK[대상없음]";
			}
			
			// 1. NEST REST 클라이언트 객체 생성한다.
			VcsRestClient vcsRestClient = new VcsRestClient("https://10.17.80.68:18080");
	
			logger.error("++++++++++vcsRestClient.connect()  :"+vcsRestClient.connect());
			
			// 2. 연결 체크한다.
			if (vcsRestClient.connect() == false) {
				return "Sparrow DB Connecting failed.";
	        }
			
			// 3. 파일 검사 검사
	        VcsFileInspectRestClient vcsFileInspectRestClient = vcsRestClient.getFileInspectRestClient();
	        
	        // 3-2. 입력 목록 전송
	        List<VcsFileInspectDto> results = vcsFileInspectRestClient.getList(list);
	
			
			HashMap<String,String> params = null;
	        // 3-3. 결과 표시
	        for (VcsFileInspectDto fileInspect : results) {
	        	logger.error("++++++++++fileInspect  :"+fileInspect.getKey()+","+ fileInspect.getCertifiedCode()+","+fileInspect.getUrl()+","
	        +fileInspect.getRisk1()+","+fileInspect.getRisk2()+fileInspect.getRisk3()+","+fileInspect.getRisk4()+","+fileInspect.getRisk5());
	        	
	            // certifiedCode : 결함주의보 설정 값에 따른 안정성 여부 코드 값
	            // - 분석성공[1] : 관리자가 설정해 놓은 소스코드 안정성 기준에 부합
	            // - 분석기준미달[0] : 관리자가 설정해 놓은 소스코드 안정성 기준에 미달
	            // - 분석정보없음[-1] : 소스코드 인스펙션 수행정보 없음
	        	
	        	params = new HashMap<String, String>();
	        	params.put("CR_ACPTNO", acptNo);
	        	params.put("CR_SERNO", fileInspect.getKey());
	        	
	        	if (fileInspect.getCertifiedCode() == 0) {
	        		params.put("CR_SPARROWRST", "N");
	        	} else if (fileInspect.getCertifiedCode() == 1) {
	        		params.put("CR_SPARROWRST", "Y");
	        	} else {
	        		params.put("CR_SPARROWRST", "X");
	        	}
	        	if (null != fileInspect.getUrl()) params.put("CR_SPARROWURL", fileInspect.getUrl());
	        	else params.put("CR_SPARROWURL", "");
	        	
	        	if (null != fileInspect.getRisk1()) params.put("CR_RISK1", Integer.toString(fileInspect.getRisk1()));
	        	else params.put("CR_RISK1", "0");
	        	if (null != fileInspect.getRisk2()) params.put("CR_RISK2", Integer.toString(fileInspect.getRisk2()));
	        	else params.put("CR_RISK2", "0");
	        	if (null != fileInspect.getRisk3()) params.put("CR_RISK3", Integer.toString(fileInspect.getRisk3()));
	        	else params.put("CR_RISK3", "0");
	        	if (null != fileInspect.getRisk4()) params.put("CR_RISK4", Integer.toString(fileInspect.getRisk4()));
	        	else params.put("CR_RISK4", "0");
	        	if (null != fileInspect.getRisk5()) params.put("CR_RISK5", Integer.toString(fileInspect.getRisk5()));
	        	else params.put("CR_RISK5", "0");
	        	
	        	checkInDAO.update_cmr1010_sparrowRst(params);
	        	params = null;
			}
			
			return "OK";
		} catch (Exception e) {

			logger.error("++++++++++Sparrow Exception:"+e);
			return "FAIL:"+e;
		} finally {
			
		}
	}
	*/
	
}
