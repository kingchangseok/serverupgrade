package app.ecams.request.checkinreal.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import app.core.proto.ProtoEcams.FileData;
import app.core.proto.ProtoEcams.FileDataList;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.ecams.commoncode.service.ICommonCodeService;
import app.ecams.job.dao.IJobDAO;
import app.ecams.job.model.Job;

import app.ecams.resourcetype.service.IResourceTypeService;
import app.ecams.request.autoseq.service.IAutoSeqService;
import app.ecams.request.confirm.service.IConfirmService;
import app.ecams.request.service.RequestService;
import app.ecams.system.service.ISystemService;
import app.ecams.user.service.IUserService;
import app.ecams.request.checkinreal.dao.ICheckInRealDAO;
import app.util.checksum.CheckSum;
import app.util.file.Gzip;

@Service
public class CheckInRealRequestService extends RequestService implements ICheckInRealRequestService{
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private ICheckInRealDAO checkInRealDAO;
	@Autowired private ISystemService systemService;
	@Autowired private IResourceTypeService resourceTypeService;
	@Autowired private IUserService userService;
	@Autowired private ICommonCodeService commonCodeService;
	@Autowired private IAutoSeqService autoSeqService;
	@Autowired private IConfirmService confirmService;
	@Autowired private PlatformTransactionManager txManager;
	@Autowired private DefaultTransactionDefinition defaultTransactionDefinition;
	@Autowired private Gzip gzip;
	@Autowired private IJobDAO jobDAO;

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
			int j;
			int seq=0;
			int retCnt=0;
			String acptNo= null;
			String errStr;
			String retMsg;
			List itemidlist = new ArrayList();
			FileDataList fileDataList = ecamsmsg.getFiledatalist();
			
			HashMap sysinfoMap = systemService.getSysInfo_detail(ecamsmsg.getSysinfo().getSyscd());
			if(sysinfoMap==null){
				throw new Exception("시스템정보 오류입니다.");
			}
			HashMap fileinfo=null;
			HashMap rsrcinfo=null;
			ArrayList RsrccdList=null;
			Set addResources = new HashSet();
			datalength = fileDataList.getFiledatasCount();
			
			for (i=0;i<datalength;i++){
				itemidlist.add(fileDataList.getFiledatas(i).getItemid());
			}
			
			param = new HashMap();
			param.put("CM_SYSCD", ecamsmsg.getSysinfo().getSyscd());
			param.put("CM_USERID", ecamsmsg.getUserinfo().getId());
			param.put("ITEMIDLIST", itemidlist);
			List<Job> jobList = jobDAO.getJobCheck(param);
			retCnt = jobList.size();
			param = null;
			if (jobList == null || retCnt < 0){
				throw new Exception("권한이 없는 업무입니다.");
			}
			
			param = new HashMap();
			param.put("CM_SYSCD",ecamsmsg.getSysinfo().getSyscd());
			param.put("CM_REQCD",ecamsmsg.getRequestinfo().getQrycd());
			param.put("CM_USERID",ecamsmsg.getUserinfo().getId());
			param.put("rsrcList",RsrccdList);
			retMsg = confirmService.confselect(param);
			param = null;
			if (retMsg == "N"){
				retMsg="결재자정보가 등록되지 않았습니다.";
				throw new RollBackException(retMsg);
			}
			
			
			/* #################################################################
			 * 
			 * 특정일시배포 설정시간vs현재시간 체크 시작
			 * 
			   ################################################################# */
			if (ecamsmsg.getRequestinfo().getDeployGB().split(":")[0].equals("4")){
		        try{
		        	
		        	String sDate = ecamsmsg.getRequestinfo().getDeployGB().split(":")[2];//년월일시분
		    		SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMddHHmm",Locale.KOREA);
		    		Date dDay = new Date();
		    		String today = formatter.format(dDay);
	//	    		System.out.println("현재년월일시분today[0]:"+today);
	//	    		System.out.println("입력년월일시분sDate[0]:"+sDate);
	//	    		System.out.println("현재년월일today.substring(0,8)[1]:"+today.substring(0,8));
	//	    		System.out.println("입력년월일sDate.substring(0,8)[2]:"+sDate.substring(0,8));
		    		
		    		if (Integer.parseInt(today.substring(0,8)) > Integer.parseInt(sDate.substring(0,8))){//년월일 비교
						retMsg="배포지정일시가 현재일 이전입니다.[년월일 비교]";
						throw new RollBackException(retMsg);
		    		}else if(today.substring(8).equals(sDate.substring(8)) && 
		    				 Integer.parseInt(today.substring(8)) > Integer.parseInt(sDate.substring(8))){//년월일 동일 할때 시분 비교
						retMsg="배포지정일시가 현재일 이전입니다.[시분 비교]";
						throw new RollBackException(retMsg);
		    		}
		    		dDay = null;
		    		formatter = null;
		    		
		        }catch (Exception e) {
					retMsg="특정일시배포 설정시간체크 오류.["+e+"]";
					throw new RollBackException(retMsg);
		        }
			}
			/* #################################################################
			 * 
			 * 특정일시배포 설정시간vs현재시간 체크 시작
			 * 
			   ################################################################# */
			
			errStr = checkFilesStatus(fileDataList,sysinfoMap,ecamsmsg.getRequestinfo().getQrycd(),ecamsmsg.getUserinfo().getId(), ecamsmsg.getSrinfo().getCcSRId());
			if (errStr != null){
				throw new Exception(errStr);
			}
			
			String strTeam = userService.getUserInfo(ecamsmsg.getUserinfo().getId()).getCm_project();
			if (strTeam == null){
				throw new Exception("사용자 팀 정보 오류");
			}
			String strRequest = commonCodeService.getCode("REQUEST", ecamsmsg.getRequestinfo().getQrycd()).getCodename();
			if (strRequest == null){
				throw new Exception("신청구분 오류");
			}
	        int wkC = datalength/300;
	        int wkD = datalength%300;
	        if (wkD>0) wkC = wkC + 1;
	        
	        String svAcpt[] = null; 
	        svAcpt = new String [wkC];
			     
	        acptNo = autoSeqService.getAcptNo(ecamsmsg.getRequestinfo().getQrycd(), wkC);
	        svAcpt = acptNo.split(":");

	        for (i=0;i<wkC;i++){
	        	if(acptnoCheck(svAcpt[i])==false){
	        		throw new RollBackException("신청번호 오류");
	        	}
	        }
	        
			for(j=0;j<datalength;j++){
			
				param = new HashMap();
				param.put("SET_CR_STATUS","7");
				param.put("SET_CR_EDITOR",ecamsmsg.getUserinfo().getId());
    			param.put("SET_CR_ISRID", "");
				param.put("PARAM_CR_ITEMID",fileDataList.getFiledatas(j).getItemid());
			
				if(fileService.updateStatus(param)<=0){
					param=null;
					throw new RollBackException("cmr0020 update Error");
				}
				param=null;
			}
			
			boolean insSw = false;
			
			for (i=0;i<datalength;i++){
				insSw = false;
				wkC = i/300;
    			acptNo = svAcpt[wkC];
				
//				param = new HashMap();
//				param.put("CR_ACPTNO", acptNo);
//				param.put("CR_ITEMID", fileDataList.getFiledatas(i).getItemid());
//				
				//String confno = checkOutCnlRequestService.selectCmr1010_acptno(param);
			
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
//        				List checkinreallist = checkInRealDAO.CheckInReal_list_check(acptNo);
//        				if(checkinreallist == null){
//        					throw new RollBackException("CheckInReal List 오류");
//        				}
//        				
//        				for(j=0;j<checkinreallist.size();j++){
        					
//        					param = new HashMap();
//        					param.put("SET_CR_STATUS","A");
//        					param.put("SET_CR_EDITOR",ecamsmsg.getUserinfo().getId());
//        					param.put("PARAM_CR_ITEMID",fileDataList.getFiledatas(i).getItemid());
//        					
//        					if(fileService.updateStatus(param)<=0){
//        						throw new RollBackException("cmr0020 update Error");
//        	    			}
//        					
//        					param=null;
        					
    						param = new HashMap();
        					param.put("acptno",acptNo);
    						List result = updateConfCmr1010(param);
    						param=null;
    						if(result == null){
    							throw new RollBackException("updateCmr1010 오류");
    						}
    						result = null;
//        				}
        			}
//        			System.out.println("☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆");
//        			System.out.println(acptNo);
//        			System.out.println("☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆");
        			
        			param = new HashMap();
        			param.put("CR_ACPTNO", acptNo);
        			param.put("CR_SYSCD", ecamsmsg.getSysinfo().getSyscd());
        			param.put("CR_SYSGB", (String)sysinfoMap.get("cm_sysgb"));
        			param.put("CR_JOBCD", fileDataList.getFiledatas(i).getJobinfo().getJobcd());
        			param.put("CR_STATUS", "0");
        			param.put("CR_TEAMCD", strTeam);
        			param.put("CR_QRYCD", ecamsmsg.getRequestinfo().getQrycd());
        			param.put("CR_PASSOK", ecamsmsg.getRequestinfo().getDeployGB().split(":")[0]);
        			param.put("CR_PASSCD", strRequest);
        			param.put("CR_EMGCD", ecamsmsg.getRequestinfo().getReqgbn());
        			param.put("CR_PASSSUB", "0");
        			if(!ecamsmsg.getRequestinfo().getReqgbn().equals("11")){
        				param.put("CR_DOCNO", ecamsmsg.getRequestinfo().getGbnsayu());
        			}
        			param.put("CR_EDITOR", ecamsmsg.getUserinfo().getId());
        			param.put("CR_SAYU", ecamsmsg.getRequestinfo().getSayu());
        			param.put("CR_PRJNO", ecamsmsg.getPrjinfolist().getPrjinfo(0).getScsrno());
        			param.put("CR_PRJNAME", ecamsmsg.getPrjinfolist().getPrjinfo(0).getScsrtitle());
        			param.put("CR_BIZCODE", ecamsmsg.getPrjinfolist().getPrjinfo(0).getSbizcode());
        			param.put("CR_PRJSTATUS", ecamsmsg.getPrjinfolist().getPrjinfo(0).getSemerrequestyn());

        			// 운영체크인 추가입력 시작
        			if (ecamsmsg.getRequestinfo().getBefjobs().length()>0){
        				param.put("CR_BEFJOB", "Y");
        			} else {
        				param.put("CR_BEFJOB", "N");
        			}
        			if (ecamsmsg.getRequestinfo().getDeployGB().split(":")[0].equals("4")){
	        			//System.out.println("getDeployGB[2]:"+ecamsmsg.getRequestinfo().getDeployGB().split(":")[2]);
	        			param.put("CR_PRCREQ", ecamsmsg.getRequestinfo().getDeployGB().split(":")[2]+"00");
        			}else {
        				param.put("CR_PRCREQ", "");
        			}
        			param.put("CR_SVRYN", ecamsmsg.getRequestinfo().getSvrYN());
        			// 운영체크인 추가입력 끝 
        			
					if (insertCmr1000(param) <= 0){
						param = null;
        				throw new RollBackException("Cmr1000 Insert Error");
        			}
        			param = null;
        			
        			
        			/* #################################################################
        			 * 
        			 * 선행작업 등록 시작
        			 * 
        			   ################################################################# */
        			if (ecamsmsg.getRequestinfo().getBefjobs().indexOf(":")>=0){
        				System.out.println("getBefjobs().length[1]:"+ecamsmsg.getRequestinfo().getBefjobs().split(":").length);
	        			for (j=0 ; j<ecamsmsg.getRequestinfo().getBefjobs().split(":").length ; j++){
		        			param = new HashMap();
		        			System.out.println("getBefjobs()["+j+"]:"+ecamsmsg.getRequestinfo().getBefjobs().split(":")[j]);
		        			param.put("CR_ACPTNO", acptNo);
		        			param.put("CR_BEFACT", ecamsmsg.getRequestinfo().getBefjobs().split(":")[j]);
							if (checkInRealDAO.insertCmr1030(param) <= 0){
								param = null;
		        				throw new RollBackException("Cmr1030 Insert Error[0]");
		        			}
		        			param = null;
	        			}
        			} else if (ecamsmsg.getRequestinfo().getBefjobs().length()>0) {
	        			param = new HashMap();
	        			System.out.println("getBefjobs()[0]:"+ecamsmsg.getRequestinfo().getBefjobs());
	        			param.put("CR_ACPTNO", acptNo);
	        			param.put("CR_BEFACT", ecamsmsg.getRequestinfo().getBefjobs());
						if (checkInRealDAO.insertCmr1030(param) <= 0){
							param = null;
	        				throw new RollBackException("Cmr1030 Insert Error[1]");
	        			}
	        			param = null;
        			}
        			/* #################################################################
        			 * 
        			 * 선행작업 등록 끝
        			 * 
        			   ################################################################# */
        			
//        			if (!confirmService.request_confirm(acptNo, ecamsmsg.getSysinfo().getSyscd(), ecamsmsg.getRequestinfo().getQrycd(), ecamsmsg.getUserinfo().getId(), null)){
//						throw new RollBackException("결재정보등록 중 오류가 발생하였습니다. 관리자에게 연락하여 주시기 바랍니다.");
//					}
        			
        			seq = 0;
        		}
        		fileinfo = fileService.getFileInfo(fileDataList.getFiledatas(i).getItemid());
        		if (fileinfo == null){
        			throw new RollBackException("fileinfo get error - itemid=["+fileDataList.getFiledatas(i).getItemid()+"]");
        		}
        		
        		rsrcinfo = resourceTypeService.getRsrcInfo_detail(ecamsmsg.getSysinfo().getSyscd(),(String)fileinfo.get("CR_RSRCCD"));
        		if (rsrcinfo == null){
        			throw new RollBackException("rsrcinfo get error - itemid=["+fileDataList.getFiledatas(i).getItemid()+"]");
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
    			param.put("CR_JOBCD", (String) fileinfo.get("CR_JOBCD"));
    			param.put("CR_STATUS", "0");
    			param.put("CR_QRYCD", qrycd);
    			param.put("CR_RSRCCD", (String) fileinfo.get("CR_RSRCCD"));
    			param.put("CR_LANGCD", (String) fileinfo.get("CR_LANGCD"));
    			param.put("CR_DSNCD",  (String) fileinfo.get("CR_DSNCD"));
    			param.put("CR_RSRCNAME",  (String) fileinfo.get("CR_RSRCNAME"));
    			param.put("CR_RSRCNAM2",  (String) fileinfo.get("CR_RSRCNAME"));
    			
    			param.put("CR_CHGCD", "0");
    			param.put("CR_TSTCHG", "0");
    			
    			param.put("CR_SRCCHG", "1");
    			param.put("CR_SRCCMP", "Y");
    			
    			param.put("CR_PRIORITY", rsrcinfo.get("CM_STEPSTA"));
    			int vercnt=0;
    			if(Integer.parseInt(rsrcinfo.get("CM_VERCNT").toString())==0){
    				vercnt=9999;
    			}else{
    				vercnt=Integer.parseInt(rsrcinfo.get("CM_VERCNT").toString());
    			}
    			if(vercnt > Integer.parseInt(fileinfo.get("CR_LSTVER").toString())+1){
    				param.put("CR_VERSION", ((BigDecimal)fileinfo.get("CR_LSTVER")).intValue()+1);
    			}else{
    				param.put("CR_VERSION", 1);
    			}
    			
    			param.put("CR_BEFVER", ((BigDecimal)fileinfo.get("CR_LSTVER")).intValue());
    			param.put("CR_EDITOR", ecamsmsg.getUserinfo().getId());
    			//param.put("CR_BASENO", acptNo);
    			//param.put("CR_BASENO", confno);
    			
    			param.put("CR_BASEITEM", fileDataList.getFiledatas(i).getItemid());
    			param.put("CR_ITEMID", fileDataList.getFiledatas(i).getItemid());
    			param.put("CR_EDITCON", ecamsmsg.getRequestinfo().getSayu());
    			//param.put("CR_COACPT", acptNo);
    			//param.put("CR_COACPT", confno);
    			param.put("CR_PGMTYPE", fileinfo.get("CR_PGMTYPE"));
    			
    			//CR_APLYDATE 특정일시배포 년월일시분
    			if (ecamsmsg.getRequestinfo().getDeployGB().split(":")[0].equals("4")){
    				param.put("CR_APLYDATE", ecamsmsg.getRequestinfo().getDeployGB().split(":")[2]+"00");
    			}else{
    				param.put("CR_APLYDATE", "");
    			}
    			if (insertCmr1010(param)<=0){
    				param = null;
    				throw new RollBackException("Cmr1010 Insert Error");
    			}
    			param = null;
    			
//    			List checkinreallist2 = checkInRealDAO.CheckInReal_list_check(acptNo);
    			
//    			if (checkinreallist2 == null){
//    				throw new RollBackException("Cmr1010 Insert Error");
//    			}
//    			
//    			for(j=0;j<checkinreallist2.size();j++){
					
//					param = new HashMap();
//					param.put("SET_CR_STATUS","A");
//					param.put("SET_CR_EDITOR",ecamsmsg.getUserinfo().getId());
//					param.put("SET_CR_SYSDATE","sysdate");
//					param.put("PARAM_CR_ITEMID",fileDataList.getFiledatas(i).getItemid());
//					
//					if(fileService.updateStatus(param)<=0){
//						throw new RollBackException("cmr0020 update Error");
//	    			}
//					param=null;
					
//				}
    			
    			param = null;
    			
			}
			if (!confirmService.request_confirm(acptNo, ecamsmsg.getSysinfo().getSyscd(), ecamsmsg.getRequestinfo().getQrycd(), ecamsmsg.getUserinfo().getId(), null)){
				throw new RollBackException("결재정보등록 중 오류가 발생하였습니다. 관리자에게 연락하여 주시기 바랍니다.");
			}
			List result = null;
			param = new HashMap();
			param.put("acptno",acptNo);
			result = checkInRealDAO.updateCmr1010(param);
			param = null;
			if(result == null){
				throw new RollBackException("updateCmr1010 Error");
			}
			result = null;
			
			/*
			 *   자동체크인항목  조회해서 1010에 insert 함 시작
			 */
			if(checkInRealDAO.CheckInReal_InsertCmr1010(acptNo)<0){
				throw new RollBackException("CheckInReal Cmr1010 Insert Error");
			}
			/*
			 *   자동체크인항목  조회해서 1010에 insert 함  끝
			 */
			
			result = checkInRealDAO.CheckInReal_item_List_check(acptNo);
			if (result == null){
				throw new RollBackException("CheckInReal item_List_check Error");
			}
			for (j=0 ; j<result.size() ; j++){
				param = new HashMap();
				param.put("CR_ACPTNO",acptNo);
				param.put("CR_ITEMID",result.get(j));
				if (checkInRealDAO.CheckInReal_item_delete(param)<0){
					param = null;
					result = null;
					throw new RollBackException("CheckInReal item_delete Error");
				}
				param = null;
			}
			result = null;
			
			// TODO Auto-generated method stub
			returnmsg_builder.setReturnval(0);
			returnmsg_builder.setReturnStr(StringUtils.join(svAcpt,":"));
			if (fileDataList_builder.getFiledatasCount()>0){
				builder_msg.setFiledatalist(fileDataList_builder.build());
				returnmsg_builder.setEcamsmsg(builder_msg.build());
			}
			txManager.commit(status);
		}
		catch (RollBackException e){
			logger.error("RequestCheckInReal.request: "+ecamsmsg.getUserinfo().getId()+" "+e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getError());
			txManager.rollback(status);
		}
		catch (Exception e){
			logger.error("RequestCheckInReal.request: "+ecamsmsg.getUserinfo().getId()+" "+e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getMessage());
			txManager.rollback(status);
		}
		return returnmsg_builder.build();
	}
	
	public ReturnMsg transfile(EcamsMessage ecamsmsg) {
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		FileDataList.Builder fileDataList_builder = FileDataList.newBuilder();
		TransactionStatus status = txManager.getTransaction(defaultTransactionDefinition);
		int i,datalength;
		
		try{
			FileDataList fileDataList = ecamsmsg.getFiledatalist();
			datalength = fileDataList.getFiledatasCount();
			HashMap fileinfo=null;
			String acptNo = ecamsmsg.getRequestinfo().getAcptno();
			for (i=0;i<datalength;i++){
				
				fileinfo = fileService.getFileInfo(fileDataList.getFiledatas(i).getItemid());
        		if (fileinfo == null){
        			throw new RollBackException("fileinfo get error - itemid=["+fileDataList.getFiledatas(i).getItemid()+"]");
        		}
        		
	    		String md5sum = "";
	    		md5sum = CheckSum.MD5SumVal(fileDataList.getFiledatas(i).getFilebytes().toByteArray());
	    		byte[] tmpbyte = gzip.getCompressedByte(fileDataList.getFiledatas(i).getFilebytes().toByteArray());
	    		if(fileDataList.getFiledatas(i).getMd5Sum().equals(md5sum)){
	    			HashMap param = new HashMap();
	        		param.put("CR_ACPTNO",(String)acptNo);
	        		param.put("CR_ITEMID",(String)fileDataList.getFiledatas(i).getItemid());
	        		param.put("CR_FILE",(byte[])gzip.getCompressedByte(fileDataList.getFiledatas(i).getFilebytes().toByteArray()));
	        		param.put("CR_VER",(int)((BigDecimal)fileinfo.get("CR_LSTVER")).intValue()+1);
	        		param.put("CR_FILESIZE",(int)fileDataList.getFiledatas(i).getFilebytes().size());
	        		param.put("CR_FILEDATE",fileDataList.getFiledatas(i).getLstdate());
	        		param.put("CR_MD5SUM",(String)fileDataList.getFiledatas(i).getMd5Sum());
					
	        		int test = fileService.insertFileData(param);
	        		
					if (test <= 0){
						FileData.Builder fileData_builder = FileData.newBuilder();
		    			fileData_builder.setItemid(fileDataList.getFiledatas(i).getItemid());
						fileData_builder.setFilename(fileDataList.getFiledatas(i).getFilename());
						fileDataList_builder.addFiledatas(fileData_builder.build());
						EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
						builder_msg.setMsgtype("return");
						builder_msg.setFiledatalist(fileDataList_builder.build());
						returnmsg_builder.setReturnval(1);
						returnmsg_builder.setReturnStr("error");
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
//	    		++i;
//	    		if(i%300 == 0){
//	    			acptNo = acptNo.substring(13);
//	    		}
	    		
			}
			returnmsg_builder.setReturnval(0);
			returnmsg_builder.setReturnStr("정상");
			if (fileDataList_builder.getFiledatasCount()>0){
				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("return");
				builder_msg.setFiledatalist(fileDataList_builder.build());
				returnmsg_builder.setEcamsmsg(builder_msg.build());
			}
			txManager.commit(status);
		}
		catch (RollBackException e){
			logger.error("RequestCheckInReal.request: ",e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getError());
			txManager.rollback(status);
		}
		catch (Exception e){
			logger.error("RequestCheckInReal.request: ",e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getMessage());
			//txManager.rollback(status);
		}
		finally{
			return returnmsg_builder.build();
		}
	}
}
