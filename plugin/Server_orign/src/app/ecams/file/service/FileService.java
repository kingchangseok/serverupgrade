package app.ecams.file.service;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.ibatis.session.RowBounds;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;


import com.google.protobuf.ByteString;

import app.core.exception.RollBackException;
import app.core.proto.ProtoEcams.EcamsMessage;
import app.core.proto.ProtoEcams.FileData;
import app.core.proto.ProtoEcams.FileDataList;
import app.core.proto.ProtoEcams.JobInfo;
import app.core.proto.ProtoEcams.PathInfo;
import app.core.proto.ProtoEcams.ReturnMsg;
import app.core.proto.ProtoEcams.RsrcInfo;
import app.core.proto.ProtoEcams.SRInfo;
import app.core.proto.ProtoEcams.SysInfo;
import app.ecams.file.dao.IFileDAO;
import app.ecams.file.model.FileInfo;
import app.ecams.path.dao.IPathDAO;
import app.ecams.path.service.IPathService;
import app.ecams.resourcetype.dao.IResourceTypeDAO;
import app.ecams.resourcetype.model.ResourceType;
import app.ecams.resourcetype.service.IResourceTypeService;
import app.ecams.user.dao.IUserInfoDAO;
import app.ecams.user.model.UserInfo;
import app.util.checksum.CheckSum;
import app.util.file.Gzip;

@Service
public class FileService implements IFileService {
	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired private IFileDAO fileDAO;	
	@Autowired private IPathService pathService;
	@Autowired private IPathDAO pathDAO;
	@Autowired private IResourceTypeDAO resourceTypeDAO;
	@Autowired private IUserInfoDAO userDAO;
	@Autowired private Gzip gzip;
	//@Autowired private PlatformTransactionManager txManager;
	//@Autowired private DefaultTransactionDefinition defaultTransactionDefinition;
	//@Autowired private IFileService fileService;
	
	@Transactional (readOnly = false, propagation = Propagation.REQUIRED, rollbackForClassName={"app.core.exception.RollBackException"})
	public ReturnMsg getFileList (EcamsMessage ecamsmsg){
		int i;
		int listsize;
		int returnval=0;
		String returnStr="";
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		HashMap<String, String> param = new HashMap<String, String>();
		
		FileDataList.Builder filedatalist_builder = FileDataList.newBuilder();
		
		if (ecamsmsg.hasFiledata()){
			if (ecamsmsg.getFiledata().hasItemid()){
				param.put("itemid",ecamsmsg.getFiledata().getItemid());
			}
			else{
				param.put("filename", ecamsmsg.getFiledata().getFilename());
				
				if (ecamsmsg.getFiledata().hasPathinfo()){
					param.put("filepath","/" + ecamsmsg.getFiledata().getPathinfo().getRelativitePath());
				}
			}
		} else if (ecamsmsg.getRequestinfo().getAcptno() != null && !ecamsmsg.getRequestinfo().getAcptno().equals("")) {   //\uc2e0\uccad\ubc88\ud638
			param.put("acptno", ecamsmsg.getRequestinfo().getAcptno());
		} else {
			if (ecamsmsg.hasPathinfo()){
				param.put("dirpath","/" + ecamsmsg.getPathinfo().getRelativitePath());
				
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				System.out.println(formatter.format(new Date())+" GETLIST "+ecamsmsg.getUserinfo().getId()+" "+ecamsmsg.getSysinfo().getSyscd()+" "+ecamsmsg.getPathinfo().getRelativitePath());
			}
		}
		
		if(ecamsmsg.hasSysinfo()){
			param.put("syscd",ecamsmsg.getSysinfo().getSyscd());
		}

		if(ecamsmsg.hasJobinfolist()){
			param.put("jobcd",ecamsmsg.getJobinfolist().getJobinfo(0).getJobcd());
		}
		
		
		UserInfo userInfo = (UserInfo)userDAO.getUserInfo(ecamsmsg.getUserinfo().getId());
		if("0".equals(userInfo.getCm_admin())){
			param.put("userid",ecamsmsg.getUserinfo().getId());
		}
		userInfo = null;
		
		EcamsMessage.Builder ecamsmsg_builder = EcamsMessage.newBuilder();
		
		try{
		
			List<FileInfo> tmpList = (List<FileInfo>) fileDAO.getFileList(param);
			
			if(tmpList == null){
				throw new RollBackException("getFileList Error");
			}
			
			param.clear();
			param = null;

			listsize = tmpList.size();
			String tmpPath = "";
			
			for (i=0;i<listsize;i++){
				FileData.Builder filedata = FileData.newBuilder();
				filedata.setFilename(tmpList.get(i).getRsrcname());
				if (tmpList.get(i).getMd5sum() != null){
					filedata.setMd5Sum(tmpList.get(i).getMd5sum());
				}
				
				if (tmpList.get(i).getTstmd5sum() != null){
					filedata.setTstmd5Sum(tmpList.get(i).getTstmd5sum());
				}
				
				int lstver = tmpList.get(i).getLstver().intValue();
				int tstver = tmpList.get(i).getTstver().intValue();
				
				filedata.setVersion(lstver);
				filedata.setTstver(tstver);
				filedata.setViewver(tmpList.get(i).getViewver());
				
				PathInfo.Builder pathinfo_builder = PathInfo.newBuilder();
				
				tmpPath = tmpList.get(i).getDirpath();
				
				param = new HashMap<String, String>();
				param.put("cm_syscd", tmpList.get(i).getCmSyscd());
				param.put("cm_rsrccd", tmpList.get(i).getResourceType());
				param.put("cm_jobcd", tmpList.get(i).getJobcd());
				String basepath = pathDAO.getBasePath(param);
				param = null;

//				System.out.println("===tmpPath==["+tmpPath+"]==basepath==["+basepath+"]");// 20201231 로그 찍는거 삭제해버리기
				if (null == basepath || "".equals(basepath)) {
					throw new Exception("체크아웃서버 프로그램종류 연결정보 등록확인 rsrccd["+tmpList.get(i).getResourceType()+"]");
				}
				
				if (tmpPath.equals(basepath)) {
					pathinfo_builder.setRelativitePath("");
				} else {
					pathinfo_builder.setRelativitePath(tmpPath.substring(basepath.length()));
					
				}
				
				filedata.setPathinfo(pathinfo_builder.build());
				pathinfo_builder.clear();
				pathinfo_builder = null;
				
				filedata.setEditor(tmpList.get(i).getEditorName()+":"+tmpList.get(i).getEditor());
				
				if (tmpList.get(i).getLstUser() != null){
					filedata.setLstUser(tmpList.get(i).getLstUserName()+":"+tmpList.get(i).getLstUser());
				}
				filedata.setItemid(tmpList.get(i).getItemid());
				filedata.setLstdate(tmpList.get(i).getLastdate());
				
				RsrcInfo.Builder rsrcinfo_builder = RsrcInfo.newBuilder();
				rsrcinfo_builder.setRsrccd(tmpList.get(i).getResourceType());
				rsrcinfo_builder.setRsrcmsg(tmpList.get(i).getResourceTypeName());
				rsrcinfo_builder.setCminfo(tmpList.get(i).getCm_info());
				filedata.setRsrcinfo(rsrcinfo_builder.build());
				
				JobInfo.Builder jobinfo_builder = JobInfo.newBuilder();
				jobinfo_builder.setJobcd(tmpList.get(i).getJobcd());
				jobinfo_builder.setJobname(tmpList.get(i).getJobName());
				filedata.setJobinfo(jobinfo_builder.build());
				filedata.setStatus(tmpList.get(i).getStatusName()+":"+tmpList.get(i).getStatus());
				
				SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
				sysinfo_builder.setSyscd(tmpList.get(i).getCmSyscd());
				sysinfo_builder.setSysmsg(tmpList.get(i).getCmSysmsg());
				filedata.setSysinfo(sysinfo_builder.build());
				
				SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
				String tmpSRID = tmpList.get(i).getCr_isrid();

				if( null == tmpSRID ) {
					tmpSRID = "";
				}

				srinfo_builder.setCcSRId(tmpSRID);
				filedata.setSrinfo(srinfo_builder.build());
				
				filedatalist_builder.addFiledatas(filedata.build());
				filedata.clear();
				filedata = null;
			}
			
			tmpList.clear();
			tmpList = null;
			if (listsize > 0){
				returnval = 0;
				returnStr = "\uc815\uc0c1";			
			}
			else{
				returnval = 1;
				returnStr = "\ucc98\ub9ac\uc5d0\ub7ec";			
			}
			ecamsmsg_builder.setFiledatalist(filedatalist_builder.build());
			ecamsmsg_builder.setMsgtype(ecamsmsg.getMsgtype());
			returnmsg_builder.setReturnStr(returnStr);
			returnmsg_builder.setReturnval(returnval);
			returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());	
			
		}
		catch(RollBackException e){
			e.printStackTrace();
			returnval = 1;
			returnStr = e.getMessage().toString();
			returnmsg_builder.setReturnStr(returnStr);
			returnmsg_builder.setReturnval(returnval);	
		}
		catch(Exception e){
			e.printStackTrace();
			returnval = 1;
			returnStr = e.getMessage().toString();
			returnmsg_builder.setReturnStr(returnStr);
			returnmsg_builder.setReturnval(returnval);	
			
		}
		return returnmsg_builder.build();
	}
	
	public ReturnMsg getNewFileList (EcamsMessage ecamsmsg){
		int i;
		int listsize;
		int returnval=0;
		String returnStr="";
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		HashMap<String, String> param = new HashMap<String, String>();
		
		FileDataList.Builder filedatalist_builder = FileDataList.newBuilder();
		
		param.put("syscd",ecamsmsg.getSysinfo().getSyscd());

		/*
		UserInfo userInfo = (UserInfo)userDAO.getUserInfo(ecamsmsg.getUserinfo().getId());
		if("0".equals(userInfo.getCm_admin())){
			param.put("userid",ecamsmsg.getUserinfo().getId());
		}
		userInfo = null;
		*/
		if(ecamsmsg.hasJobinfolist()){
			param.put("jobcd",ecamsmsg.getJobinfolist().getJobinfo(0).getJobcd());
		}
		
		param.put("start",Integer.toString(ecamsmsg.getTotpage()));
		param.put("end",Integer.toString(ecamsmsg.getPagenum()));
		
		EcamsMessage.Builder ecamsmsg_builder = EcamsMessage.newBuilder();
		
		try{
			List<FileInfo> tmpList = (List<FileInfo>) fileDAO.getNewFileList(param);
			
			if(tmpList == null){
				throw new Exception("getFileList Error");
			}
			
			param.clear();
			param = null;
			
			listsize = tmpList.size();
			String tmpPath = "";
			
			System.out.println(ecamsmsg.getSysinfo().getSyscd()+" : "+ecamsmsg.getUserinfo().getId()+" : "+Integer.toString(ecamsmsg.getTotpage())+"~"+Integer.toString(ecamsmsg.getPagenum()));
			
			for (i=0;i<listsize;i++){
				FileData.Builder filedata = FileData.newBuilder();
				filedata.setFilename(tmpList.get(i).getRsrcname());
				if (tmpList.get(i).getMd5sum() != null){
					filedata.setMd5Sum(tmpList.get(i).getMd5sum());
				}
				
				if (tmpList.get(i).getTstmd5sum() != null){
					filedata.setTstmd5Sum(tmpList.get(i).getTstmd5sum());
				}
				
				filedata.setVersion(tmpList.get(i).getLstver().intValue());
				filedata.setTstver(tmpList.get(i).getTstver().intValue());
				filedata.setViewver(tmpList.get(i).getViewver());
				PathInfo.Builder pathinfo_builder = PathInfo.newBuilder();
				
				tmpPath = tmpList.get(i).getDirpath();
				
				param = new HashMap<String, String>();
				param.put("cm_syscd", tmpList.get(i).getCmSyscd());
				param.put("cm_rsrccd", tmpList.get(i).getResourceType());
				param.put("cm_jobcd", tmpList.get(i).getJobcd());
				String basepath = pathDAO.getBasePath(param);
				param = null;

//				System.out.println("===tmpPath==["+tmpPath+"]==basepath==["+basepath+"]");// 20201231 로그 찍는거 삭제해버리기
				if (null == basepath || "".equals(basepath)) {
					throw new Exception("체크아웃서버 프로그램종류 연결정보 등록확인 rsrccd["+tmpList.get(i).getResourceType()+"]");
				}
				if (tmpPath.equals(basepath)) {
					pathinfo_builder.setRelativitePath("");
				} else {
					pathinfo_builder.setRelativitePath(tmpPath.substring(basepath.length()));
					
				}

				filedata.setPathinfo(pathinfo_builder.build());
				filedata.setEditor(tmpList.get(i).getEditorName()+":"+tmpList.get(i).getEditor());
				
				if (tmpList.get(i).getLstUser() != null){
					filedata.setLstUser(tmpList.get(i).getLstUserName()+":"+tmpList.get(i).getLstUser());
				}
				filedata.setItemid(tmpList.get(i).getItemid());
				filedata.setLstdate(tmpList.get(i).getLastdate());
				
				RsrcInfo.Builder rsrcinfo_builder = RsrcInfo.newBuilder();
				rsrcinfo_builder.setRsrccd(tmpList.get(i).getResourceType());
				rsrcinfo_builder.setRsrcmsg(tmpList.get(i).getResourceTypeName());
				rsrcinfo_builder.setCminfo(tmpList.get(i).getCm_info());
				filedata.setRsrcinfo(rsrcinfo_builder.build());
				
				JobInfo.Builder jobinfo_builder = JobInfo.newBuilder();
				jobinfo_builder.setJobcd(tmpList.get(i).getJobcd());
				jobinfo_builder.setJobname(tmpList.get(i).getJobName());
				filedata.setJobinfo(jobinfo_builder.build());
				filedata.setStatus(tmpList.get(i).getStatusName()+":"+tmpList.get(i).getStatus());
				
				SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
				sysinfo_builder.setSyscd(tmpList.get(i).getCmSyscd());
				sysinfo_builder.setSysmsg(tmpList.get(i).getCmSysmsg());
				filedata.setSysinfo(sysinfo_builder.build());

				if(!"9".equals(tmpList.get(i).getStatus()) && !"3".equals(tmpList.get(i).getStatus())){
					param = new HashMap<String, String>();
					if(tmpList.get(i).getEditor().equals(ecamsmsg.getUserinfo().getId())){
						//param.put("lstver", tmpList.get(i).getLstver().toString());
						param.put("devver", tmpList.get(i).getTstver().toString());
					}else{
						//param.put("lstver", tmpList.get(i).getLstver().toString());
						param.put("devver", "0");
					}
					param.put("lstver", tmpList.get(i).getViewver());

					param.put("itemid", tmpList.get(i).getItemid());
					FileInfo getFile = (FileInfo) fileDAO.getFileData(param);
					if (getFile != null){
						filedata.setFilebytes(ByteString.copyFrom(getFile.getFilebyte()));
					}
					param = null;
				}
				filedatalist_builder.addFiledatas(filedata.build());
				//logger.error(ecamsmsg.getTotpage()+i);
				filedata.clear();
				filedata = null;
			}
			
			tmpList.clear();
			tmpList = null;
			if (listsize > 0){
				returnval = 0;
				returnStr = "\uc815\uc0c1";			
			}
			else{
				returnval = 1;
				returnStr = "\ucc98\ub9ac\uc5d0\ub7ec";			
			}
			
			System.out.println(">>>>SIZE:"+filedatalist_builder.getFiledatasCount());
			ecamsmsg_builder.setFiledatalist(filedatalist_builder.build());
			ecamsmsg_builder.setMsgtype(ecamsmsg.getMsgtype());
			returnmsg_builder.setReturnStr(returnStr);
			returnmsg_builder.setReturnval(returnval);
			returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());	
			
		}
		catch(RollBackException e){
			e.printStackTrace();
			returnval = 1;
			returnStr = e.getMessage().toString();
			returnmsg_builder.setReturnStr(returnStr);
			returnmsg_builder.setReturnval(returnval);	
		}
		catch(Exception e){
			e.printStackTrace();
			returnval = 1;
			returnStr = e.getMessage().toString();
			returnmsg_builder.setReturnStr(returnStr);
			returnmsg_builder.setReturnval(returnval);	
			
		}
		return returnmsg_builder.build();
	}

	public ReturnMsg getFileListCount (EcamsMessage ecamsmsg){
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();

		HashMap<String, String> param = new HashMap<String, String>();
		param.put("syscd",ecamsmsg.getSysinfo().getSyscd());
		/*
		UserInfo userInfo = (UserInfo)userDAO.getUserInfo(ecamsmsg.getUserinfo().getId());
		if("0".equals(userInfo.getCm_admin())){
			param.put("userid",ecamsmsg.getUserinfo().getId());
		}
		userInfo = null;
		*/
		if(ecamsmsg.hasJobinfolist()){
			param.put("jobcd",ecamsmsg.getJobinfolist().getJobinfo(0).getJobcd());
		}
		int totpage = fileDAO.getFileListCount(param);
		param = null;
		
		if(totpage>0){
			ecamsmsg_builder.setMsgtype(ecamsmsg.getMsgtype());
			ecamsmsg_builder.setTotpage(totpage);
			returnmsg_builder.setReturnStr("\uc815\uc0c1");
			returnmsg_builder.setReturnval(0);
			returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
		}else{
			returnmsg_builder.setReturnStr("\ub3d9\uae30\ud654 \ubaa9\ub85d\uc5c6\uc74c");
			returnmsg_builder.setReturnval(1);
		}
		
		return returnmsg_builder.build();
	}
	
	public ReturnMsg getFileData (EcamsMessage ecamsmsg){
		int returnval=0;
		String returnStr="";		
		byte[] filebyte = null;
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();	

		EcamsMessage.Builder ecamsmsg_builder = EcamsMessage.newBuilder();
		FileData.Builder filedata_builder = FileData.newBuilder();
		
		
		try{
			HashMap param = new HashMap();
			param.put("itemid", ecamsmsg.getFiledata().getItemid());
			
			//param.put("lstver", ecamsmsg.getFiledata().getVersion());
			param.put("lstver", ecamsmsg.getFiledata().getViewver());

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println(formatter.format(new Date())+" "+ecamsmsg.getUserinfo().getId()+" "+ecamsmsg.getFiledata().getItemid()+" "+ecamsmsg.getFiledata().getFilename()+" "+param.get("lstver"));
			formatter = null;
			
			FileInfo getFile = (FileInfo) fileDAO.getFileData(param);
			param = null;
			

			if (getFile == null){
				returnStr = "\ud30c\uc77c \ubc84\uc804\uc815\ubcf4 \ubd88\uc77c\uce58";
				returnval = 1;
				returnmsg_builder.setReturnStr(returnStr);
				returnmsg_builder.setReturnval(returnval);
				return returnmsg_builder.build();
			}
			filedata_builder.setFilename(ecamsmsg.getFiledata().getFilename());

			filebyte = getFile.getFilebyte();
			filedata_builder.setFilebytes(ByteString.copyFrom(filebyte));
			
			returnStr = "\uc815\uc0c1";
			returnval = 0;
			ecamsmsg_builder.setMsgtype(ecamsmsg.getMsgtype());
			ecamsmsg_builder.setFiledata(filedata_builder.build());
			returnmsg_builder.setReturnStr(returnStr);
			returnmsg_builder.setReturnval(returnval);
			returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
						
		}
		catch(Exception e){
			returnval = 1;
			logger.error("getFileData309 UserId:"+ecamsmsg.getUserinfo().getId()+"Itemid:"+ecamsmsg.getFiledata().getItemid()+"ERROR:"+e.getMessage());
			returnmsg_builder.setReturnStr(e.getMessage());
			returnmsg_builder.setReturnval(returnval);
		}
		return returnmsg_builder.build();
	}
	
	public ReturnMsg getMergeFileData (EcamsMessage ecamsmsg){
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		//TransactionStatus status = txManager.getTransaction(defaultTransactionDefinition);
		
		final String	eCamsExe	= ".ecams";
		final String	mergeExe	= ".merge";
		
		String		strReturnMSg	= "";
		int			iReturnValue	= 0;
		
		String		strTmpPath		= "";
		String		strBinPath		= "";
		String		strTmpFileName	= "";
		String		strTmpFilePath	= "";
		
		String		cr_itemid		= "";
		int			cr_lstVer		= -1;
		
		String		shFileName		= "";
		OutputStreamWriter shWriter = null;
		File		shFile			= null;
		Runtime		run				= null;
		Process		p 				= null;
		
		String[] execArray;
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		FileData fileData = ecamsmsg.getFiledata();
		
		strTmpPath	= pathDAO.getTempPath("99"); //tmp \ub514\ub809\ud1a0\ub9ac
		strBinPath	= pathDAO.getTempPath("14"); //bin \ub514\ub809\ud1a0\ub9ac
			
		FileDataList.Builder	fileDataList_Builder	= FileDataList.newBuilder();
		FileData.Builder		fileData_Builder		= FileData.newBuilder();
		
		if( (null == strTmpPath)	|| "".equals(strTmpPath) ||
			(null == strBinPath)	|| "".equals(strBinPath) ){
			iReturnValue = 1;
			strReturnMSg = "\uc784\uc2dc\uc800\uc7a5\uacbd\ub85c \ub610\ub294 \uc2e4\ud589\uacbd\ub85c \uc5c6\uc74c";
		}else {
			strTmpPath = strTmpPath + "/";
			strBinPath = strBinPath + "/";
			
			cr_itemid	= fileData.getItemid();
			cr_lstVer	= Integer.parseInt( fileDAO.getLastFileVer(cr_itemid) );
			//cr_lstVer   = fileData.getVersion();
			
			strTmpFileName	= cr_itemid + "_" + ecamsmsg.getUserinfo().getId();
			strTmpFilePath	= strTmpPath + strTmpFileName;
			shFileName		= strTmpFilePath + ".sh";
			
    		FileOutputStream fos;
			try {
				/* \ub85c\uceec\ud30c\uc77c \uc218\uc2e0\ubd80 */
				//strHomePath = "C:\\";
				
				
				File mineFile = new File( strTmpFilePath );
				if( mineFile.exists() ) {
					mineFile.delete();
				}
				
				File mergeFile = new File(strTmpFilePath+mergeExe);
				if( mergeFile.exists() ) {
					mergeFile.delete();
				}				
				
				fos = new FileOutputStream( mineFile );
				FileChannel outChannel = fos.getChannel();
				java.nio.ByteBuffer buff = java.nio.ByteBuffer.wrap( gzip.getDecompressedByte(fileData.getFilebytes().toByteArray()) );
				outChannel.write(buff);
				
				outChannel.close();
	    		fos.close();
	    		
	    		Thread.sleep(500);
	    		
	    		/* \uba38\uc9c0 \uc791\uc5c5\ubd80 */
	    		shFile = new File( shFileName );
	    		if( !(shFile.isFile()) ) {
	    			shFile.createNewFile();
	    		}
	    		
	    		/* \uba38\uc9c0 \uc791\uc5c5 \uc258 \uc0dd\uc131 */
	    		String shParam = "ecams_merge " +	cr_itemid			+ " " +
	    											"LAST"				+ " " +
	    											strTmpFilePath		+ " " +
	    											strTmpFileName;
	    		
	    		shWriter = new OutputStreamWriter( new FileOutputStream(shFileName));
				shWriter.write("cd " + strBinPath	+ "\n");
				shWriter.write(shParam				+ "\n");
				shWriter.write("exit  $?  \n");
				shWriter.close();
				
				execArray = new String[3];
				execArray[0] = "chmod";
				execArray[1] = "777";
				execArray[2] = shFileName;
				
				run = Runtime.getRuntime();
				p = run.exec(execArray);
				execArray = null;
				p.waitFor();
				
				run = Runtime.getRuntime();
				execArray = new String[2];
				execArray[0] = "/bin/sh";
				execArray[1] = shFileName;
				p = run.exec(execArray);
				execArray = null;
				p.waitFor();
				
				if (p.exitValue() != 0) {
					strReturnMSg = "Merge \uc258 \uc2a4\ud06c\ub9bd\ud2b8 \uc2e4\ud589 \uc2e4\ud328";
					iReturnValue = 1;
				} else{
					shWriter = null;
					run = null;
					p = null;
					
					if (shFile != null) shFile.delete();
					
					/* \uba38\uc9c0\ud30c\uc77c \uc1a1\uc2e0 \uc900\ube44 */
					byte[] mergeFileByte = null;
					FileChannel inChannel = new FileInputStream(mergeFile).getChannel();
					
					int size = (int)inChannel.size();
					if(size<1){
						strReturnMSg = strTmpFilePath+mergeExe + " \ud30c\uc77c \uc77d\uae30 \uc2e4\ud328(Size:0).";
						iReturnValue = 1;
					} else {
						mergeFileByte = app.util.file.FileToByteArray.FileToByteArray(mergeFile);
						
						fileData_Builder.setFilename(fileData.getFilename());
						fileData_Builder.setVersion(cr_lstVer);
						fileData_Builder.setFilebytes( ByteString.copyFrom( gzip.getCompressedByte(mergeFileByte) ) );
						fileDataList_Builder.addFiledatas(fileData_Builder.build());
						
						/* \ud615\uc0c1\uad00\ub9ac \ucd5c\uc885\ubc84\uc83c \ud30c\uc77c \uc1a1\uc2e0 \uc900\ube44 */
						HashMap param = new HashMap();
						param.put("itemid", cr_itemid);
						param.put("lstver", cr_lstVer);
						
						FileInfo getFile = (FileInfo) fileDAO.getFileData(param);
						param = null;
						
						if (getFile == null){
							strReturnMSg = fileData.getFilename() + "\ud30c\uc77c \ubc84\uc804\uc815\ubcf4 \ubd88\uc77c\uce58";
							iReturnValue = 1;
						} else {
							fileData_Builder.setFilename(fileData.getFilename()+eCamsExe);
							
							byte[] lastVerFileByte = null;
							lastVerFileByte = getFile.getFilebyte();
							fileData_Builder.setFilebytes(ByteString.copyFrom(lastVerFileByte));
							fileDataList_Builder.addFiledatas(fileData_Builder.build());
							
							EcamsMessage.Builder ecamsMsg_Builder = EcamsMessage.newBuilder();
							ecamsMsg_Builder.setMsgtype(ecamsmsg.getMsgtype());
							ecamsMsg_Builder.setFiledatalist(fileDataList_Builder.build());
							
							strReturnMSg = "\uc815\uc0c1";
							iReturnValue = 0;
							returnmsg_builder.setEcamsmsg(ecamsMsg_Builder.build());
						}
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		
		returnmsg_builder.setReturnval(iReturnValue);
		returnmsg_builder.setReturnStr(strReturnMSg);
		
/*	tmp\ub514\ub809\ud1a0\ub9ac\uc758 \ud30c\uc77c\uc740 \uc8fc\uae30\uc801\uc73c\ub85c \uc0ad\uc81c\ud558\ubbc0\ub85c \ubcc4\ub3c4 \uc0ad\uc81c \uc548\ud558\ub3c4\ub85d \ubcc0\uacbd	
		File deleteFile = new File(strTmpFilePath);
		if( deleteFile.exists() ){
			deleteFile.delete();
		}
		
		deleteFile = new File(strTmpFilePath+mergeExe);
		if( deleteFile.exists() ){
			deleteFile.delete();
		}
*/		
		if( iReturnValue == 0) {
			System.out.println(formatter.format(new Date())+" [GETMERGEFILE] COMPLETE "+ecamsmsg.getUserinfo().getId()+" "+fileData.getItemid()+" "+fileData.getFilename()+" "+cr_lstVer);
		} else {
			System.out.println(formatter.format(new Date())+" FAIL>>> "+strReturnMSg+" "+ecamsmsg.getUserinfo().getId()+" "+fileData.getItemid()+" "+fileData.getFilename()+" "+cr_lstVer);
		}
		
//		deleteFile = null;
		formatter = null;
		
		return returnmsg_builder.build();
	}
	
	public ReturnMsg getFileTstData (EcamsMessage ecamsmsg){
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		FileData.Builder filedata_builder = FileData.newBuilder();
		try{
			
			//if(){
			FileInfo getFile = (FileInfo) fileDAO.getFileTstData(ecamsmsg.getFiledata().getItemid());
			//}else{
				//FileInfo getFile = (FileInfo) fileDAO.getFileDataTst(ecamsmsg.getFiledata().getItemid());
			//}
			
			if (getFile == null){
				returnmsg_builder.setReturnStr("\ud30c\uc77c \ubc84\uc804\uc815\ubcf4 \ubd88\uc77c\uce58");
				returnmsg_builder.setReturnval(1);
				return returnmsg_builder.build();
			}
			
			filedata_builder.setFilename(ecamsmsg.getFiledata().getFilename());
			
			filedata_builder.setFilebytes(ByteString.copyFrom(getFile.getFilebyte()));
			
			ecamsmsg_builder.setFiledata(filedata_builder.build());
			returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
		
			returnmsg_builder.setReturnStr("\uc815\uc0c1");
			returnmsg_builder.setReturnval(0);
			
						
		} catch(Exception e){
			returnmsg_builder.setReturnStr(e.getMessage());
			returnmsg_builder.setReturnval(1);
		}
		return returnmsg_builder.build();
	}
	
	public ReturnMsg getLastFileData (EcamsMessage ecamsmsg){
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
		FileData.Builder filedata_builder = FileData.newBuilder();
		
		HashMap<String, String> param=null;
		try{
			param = new HashMap<String, String>();
			param.put("CR_ITEMID" , ecamsmsg.getFiledata().getItemid().toString());
			param.put("CR_VER", ecamsmsg.getFiledata().getBasever());
			FileInfo getLastFile = null;
			
			getLastFile = (FileInfo) fileDAO.getLastFileData(param);
			
			if (getLastFile == null){
				returnmsg_builder.setReturnStr("\ud30c\uc77c \ubc84\uc804\uc815\ubcf4 \ubd88\uc77c\uce58");
				returnmsg_builder.setReturnval(1);
				return returnmsg_builder.build();
			}
			
			filedata_builder.setFilename(ecamsmsg.getFiledata().getFilename());
			
			filedata_builder.setFilebytes(ByteString.copyFrom(getLastFile.getFilebyte()));
		
			ecamsmsg_builder.setFiledata(filedata_builder.build());
			returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());
		
			returnmsg_builder.setReturnStr("\uc815\uc0c1");
			returnmsg_builder.setReturnval(0);
			
				
		}
		catch(Exception e){
			returnmsg_builder.setReturnStr(e.getMessage());
			returnmsg_builder.setReturnval(1);
		}
		return returnmsg_builder.build();
	}
	
	public ReturnMsg registFile (EcamsMessage ecamsmsg){
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		try{
			int i = 0;
			String itemid = null;
			
			for (i=0;i<ecamsmsg.getFiledatalist().getFiledatasCount();i++){
				itemid = ecamsmsg.getFiledatalist().getFiledatas(i).getItemid();
				if(itemid == null || itemid.equals("") || itemid.length()==0){
					HashMap<String, String> newList = new HashMap<String, String>();

					newList.put("dirpath","/"+ecamsmsg.getFiledatalist().getFiledatas(i).getPathinfo().getRelativitePath());
					newList.put("syscd", ecamsmsg.getSysinfo().getSyscd());
					//newList.put("jobcd", ecamsmsg.getSysinfo().getSyscd());
					newList.put("jobcd", ecamsmsg.getJobinfolist().getJobinfo(0).getJobcd());
					newList.put("userid", ecamsmsg.getUserinfo().getId());
					newList.put("rsrcname", ecamsmsg.getFiledatalist().getFiledatas(i).getFilename());
					//newList.put("sayu", ecamsmsg.getRequestinfo().getSayu());
					newList.put("sayu", ecamsmsg.getRequestinfo().getSayu());
					
					System.out.println("ecamsmsg.hasSrinfo():"+ecamsmsg.hasSrinfo());
					if(ecamsmsg.hasSrinfo()){
						newList.put("srid", ecamsmsg.getSrinfo().getCcSRId());
					}else{
						newList.put("srid", "");
					}
					
					System.out.println("fileDataList.getFiledatas(i).hasRsrcinfo():"+ecamsmsg.getFiledatalist().getFiledatas(i).hasRsrcinfo());
					if(ecamsmsg.getFiledatalist().getFiledatas(i).hasRsrcinfo()){
						newList.put("rsrccd", ecamsmsg.getFiledatalist().getFiledatas(i).getRsrcinfo().getRsrccd());
					}else{
						newList.put("rsrccd", "");
					}
					
					itemid = registCheckInFile(newList);
					newList = null;
					
					if(itemid == null || itemid.equals("") || itemid.length()==0){
						System.out.println(">>>FAIL "+ecamsmsg.getUserinfo().getId()+" "+ecamsmsg.getFiledatalist().getFiledatas(i).getFilename()
								+" "+ecamsmsg.getSysinfo().getSyscd()+" "+ecamsmsg.getJobinfolist().getJobinfo(0).getJobcd()+" "+
								ecamsmsg.getFiledatalist().getFiledatas(i).getPathinfo().getRelativitePath());
						
						throw new Exception("\uc2e0\uaddc\ub4f1\ub85d\uc5d0 \uc2e4\ud328\ud588\uc2b5\ub2c8\ub2e4. \ub2e4\uc2dc \uc9c4\ud589\ud574 \uc8fc\uc2dc\uae30 \ubc14\ub78d\ub2c8\ub2e4.");
					}
	        	}
			}
			
			returnmsg_builder.setReturnStr("\ud30c\uc77c\ub4f1\ub85d\uc644\ub8cc");
			returnmsg_builder.setReturnval(0);
		}
		catch (RollBackException e){
			logger.error("RequestCheckOut.request: ",e);
			returnmsg_builder.setReturnval(1);
			returnmsg_builder.setReturnStr("message:"+e.getError());
		}
		catch (Exception e){
			logger.error("RequestCheckOut.request: ",e);
			returnmsg_builder.setReturnStr("message:"+e.getMessage());
			returnmsg_builder.setReturnval(1);	
		}
		return returnmsg_builder.build();
	}
	
	public ReturnMsg getFileList_Data (EcamsMessage ecamsmsg){
		int i;
		int listsize;
		int returnval=0;
		String returnStr="";
		
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		HashMap<String, String> param = new HashMap<String, String>();
		
		FileDataList.Builder filedatalist_builder = FileDataList.newBuilder();
		param.put("filename", ecamsmsg.getFiledata().getFilename());
//		param.put("dirpath","/" + ecamsmsg.getPathinfo().getRelativitePath());

		String filePath = "/" + ecamsmsg.getFiledata().getPathinfo().getRelativitePath();
		
		while( filePath.indexOf("/") >= 0 ) {
			filePath = filePath.replace("/", "\\");
		}
		
		while( filePath.indexOf("\\\\") >= 0 ) {
			filePath = filePath.replace("\\\\", "\\");
		}
		
		while( filePath.indexOf("\\") >= 0 ) {
			filePath = filePath.replace("\\", "/");
		}
		
		//param.put("dirpath", filePath);
		param.put("filepath", filePath);
		
		param.put("syscd",ecamsmsg.getSysinfo().getSyscd());
		param.put("userid",ecamsmsg.getUserinfo().getId());
		
		EcamsMessage.Builder ecamsmsg_builder = EcamsMessage.newBuilder();
		
		try{
			List<FileInfo> tmpList = (List<FileInfo>) fileDAO.getFileList(param);
			
			if(tmpList == null){
				throw new RollBackException("getFileList Error");
			}
			
			param.clear();
			param = null;

			listsize = tmpList.size();
			String tmpPath = "";
			
			for (i=0;i<listsize;i++){
				FileData.Builder filedata = FileData.newBuilder();
				filedata.setFilename(tmpList.get(i).getRsrcname());
				if (tmpList.get(i).getMd5sum() != null){
					filedata.setMd5Sum(tmpList.get(i).getMd5sum());
				}
				if (tmpList.get(i).getTstmd5sum() != null){
					filedata.setTstmd5Sum(tmpList.get(i).getTstmd5sum());
				}
				
				int lstver = tmpList.get(i).getLstver().intValue();
				int tstver = tmpList.get(i).getTstver().intValue();
				
				filedata.setVersion(lstver);
				filedata.setTstver(tstver);
				filedata.setViewver(tmpList.get(i).getViewver());
				
				PathInfo.Builder pathinfo_builder = PathInfo.newBuilder();
				
				tmpPath = tmpList.get(i).getDirpath();
				
				param = new HashMap<String, String>();
				param.put("cm_syscd", tmpList.get(i).getCmSyscd());
				param.put("cm_rsrccd", tmpList.get(i).getResourceType());
				param.put("cm_jobcd", tmpList.get(i).getJobcd());
				String basepath = pathDAO.getBasePath(param);
				param = null;

//				System.out.println("===tmpPath==["+tmpPath+"]==basepath==["+basepath+"]");// 20201231 로그 찍는거 삭제해버리기
				if (null == basepath || "".equals(basepath)) {
					throw new Exception("체크아웃서버 프로그램종류 연결정보 등록확인 rsrccd["+tmpList.get(i).getResourceType()+"]");
				}
				if (tmpPath.equals(basepath)) {
					pathinfo_builder.setRelativitePath("");
				} else {
					pathinfo_builder.setRelativitePath(tmpPath.substring(basepath.length()));
					
				}
				
				filedata.setPathinfo(pathinfo_builder.build());
				pathinfo_builder.clear();
				pathinfo_builder = null;
				
				filedata.setEditor(tmpList.get(i).getEditorName()+":"+tmpList.get(i).getEditor());
				
				if (tmpList.get(i).getLstUser() != null){
					filedata.setLstUser(tmpList.get(i).getLstUserName()+":"+tmpList.get(i).getLstUser());
				}
				filedata.setItemid(tmpList.get(i).getItemid());
				filedata.setLstdate(tmpList.get(i).getLastdate());
				
				RsrcInfo.Builder rsrcinfo_builder = RsrcInfo.newBuilder();
				rsrcinfo_builder.setRsrccd(tmpList.get(i).getResourceType());
				rsrcinfo_builder.setRsrcmsg(tmpList.get(i).getResourceTypeName());
				rsrcinfo_builder.setCminfo(tmpList.get(i).getCm_info());
				filedata.setRsrcinfo(rsrcinfo_builder.build());
				
				JobInfo.Builder jobinfo_builder = JobInfo.newBuilder();
				jobinfo_builder.setJobcd(tmpList.get(i).getJobcd());
				jobinfo_builder.setJobname(tmpList.get(i).getJobName());
				filedata.setJobinfo(jobinfo_builder.build());
				filedata.setStatus(tmpList.get(i).getStatusName()+":"+tmpList.get(i).getStatus());
				
				SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
				sysinfo_builder.setSyscd(tmpList.get(i).getCmSyscd());
				sysinfo_builder.setSysmsg(tmpList.get(i).getCmSysmsg());
				filedata.setSysinfo(sysinfo_builder.build());
				
				
				param = new HashMap<String, String>();
				param.put("itemid", tmpList.get(i).getItemid());
				//param.put("lstver", Integer.toString(lstver));
				param.put("lstver", filedata.getViewver());


				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy/MM/dd HH:mm:ss
				
				System.out.println(formatter.format(new Date())+" GETFILE "+ecamsmsg.getUserinfo().getId()+" "+tmpList.get(i).getItemid()+" "+ecamsmsg.getFiledata().getFilename()+" "+Integer.toString(lstver)+"."+ Integer.toString(tstver));
				FileInfo getFile = (FileInfo) fileDAO.getFileData(param);
				param = null;
				
				if (getFile == null){
					returnStr = "\ud30c\uc77c \ubc84\uc804\uc815\ubcf4 \ubd88\uc77c\uce58";
					returnval = 1;
					returnmsg_builder.setReturnStr(returnStr);
					returnmsg_builder.setReturnval(returnval);
					return returnmsg_builder.build();
				}
				
				filedata.setFilebytes(ByteString.copyFrom(getFile.getFilebyte()));
				getFile = null;
				
				filedatalist_builder.addFiledatas(filedata.build());
				
				filedata.clear();
				filedata = null;
			}
			
			tmpList.clear();
			tmpList = null;
			
			if (listsize > 0){
				returnval = 0;
				returnStr = "\uc815\uc0c1";			
			}else{
				returnval = 1;
				returnStr = "\ucc98\ub9ac\uc5d0\ub7ec";			
			}
			ecamsmsg_builder.setFiledatalist(filedatalist_builder.build());
			ecamsmsg_builder.setMsgtype(ecamsmsg.getMsgtype());
			returnmsg_builder.setReturnStr(returnStr);
			returnmsg_builder.setReturnval(returnval);
			returnmsg_builder.setEcamsmsg(ecamsmsg_builder.build());	
			
		}
		catch(RollBackException e){
			e.printStackTrace();
			returnval = 1;
			returnStr = e.getMessage().toString();
			returnmsg_builder.setReturnStr(returnStr);
			returnmsg_builder.setReturnval(returnval);	
		}
		catch(Exception e){
			e.printStackTrace();
			returnval = 1;
			returnStr = e.getMessage().toString();
			returnmsg_builder.setReturnStr(returnStr);
			returnmsg_builder.setReturnval(returnval);	
			
		}
		return returnmsg_builder.build();
	}
	
	public ReturnMsg setDeleteStatus(EcamsMessage ecamsmsg){
		ReturnMsg.Builder returnmsg_builder = ReturnMsg.newBuilder();
		
		try{
			HashMap<String, String> param = new HashMap<String, String>();
			for(int i=0; i<ecamsmsg.getFiledatalist().getFiledatasCount(); i++){
				param = new HashMap<String, String>();
				param.put("SET_CR_STATUS", "9");
				param.put("SET_CR_EDITOR", ecamsmsg.getUserinfo().getId());
				param.put("SET_CR_SYSDATE","111");
				param.put("PARAM_CR_ITEMID", ecamsmsg.getFiledatalist().getFiledatas(i).getItemid());
				fileDAO.updateStatus(param);
			}
			param = null;

			returnmsg_builder.setReturnStr("\uc815\uc0c1");
			returnmsg_builder.setReturnval(0);
			
			return returnmsg_builder.build();				
		}
		catch(Exception e){
			logger.error(e.getMessage());
			returnmsg_builder.setReturnStr(e.getMessage());
			returnmsg_builder.setReturnval(1);
			return returnmsg_builder.build();
		}
	}

	public String registCheckInFile (HashMap<String, String> newList){
		HashMap<String, String> param = new HashMap<String, String>();
		String itemid = "";
		try{
			
			String rsrccd = null;
			
			if(!"".equals(newList.get("rsrccd"))){
				rsrccd = newList.get("rsrccd");
			}else{
				int j=0;
				int k=0;
	
				List<ResourceType> rsuList = (List<ResourceType>) resourceTypeDAO.getRsrcInfo(newList.get("syscd").toString());
				
				for (j=0; j<rsuList.size(); j++) {
					if(rsuList.get(j).getResourceExeName() != null) {
						String[] exename = rsuList.get(j).getResourceExeName().split(",");
						for (k=0; k<exename.length; k++) {  
							if(newList.get("rsrcname").toUpperCase().lastIndexOf(exename[k].toUpperCase()) > 0
									|| newList.get("rsrcname").toUpperCase().equals(exename[k].toUpperCase())) {
								rsrccd = rsuList.get(j).getResourceType();
								break;
							}
						}
					}
				}
				
				if(null == rsrccd) {
	//				for (j=0; j<rsuList.size(); j++) {
	//					if (rsuList.get(j).getResourceCmInfo().substring(40,41).equals("1")) {//\ud655\uc7a5\uc790 \uccb4\ud06c\uc548\ud568
	//						rsrccd = rsuList.get(j).getResourceType();
	//						break;
	//					}
	//				}
					rsrccd = "08"; //WEB\ud30c\uc77c
				}
				rsuList = null;
			}
			
			System.out.println(">>>registCheckInFile: rsrccd:"+rsrccd);
			System.out.println(">>>registCheckInFile: jobcd:"+newList.get("jobcd").toString());
			
			String dsncd = "";
			
			if(null != newList.get("dsncd")){
				dsncd = newList.get("dsncd");
			}else{
				param = new HashMap<String, String>();
				if (newList.get("dirpath") == null || "".equals(newList.get("dirpath"))){
					param.put("dirpath","/");
				}else{
					param.put("dirpath",newList.get("dirpath").toString());
				}
				
				param.put("cm_syscd", newList.get("syscd").toString());
				//param.put("cm_rsrccd", newList.get("rsrccd").toString());
				param.put("cm_rsrccd", rsrccd);
				param.put("cm_jobcd", newList.get("jobcd").toString());
				param.put("cm_userid", newList.get("userid").toString());
				
				dsncd = pathService.getPathCD(param);
			}
			
			if (dsncd == null){
				throw new RollBackException("\ub514\ub809\ud1a0\ub9ac \ucc98\ub9ac \uc911 \uc624\ub958\uac00 \ubc1c\uc0dd\ud558\uc600\uc2b5\ub2c8\ub2e4.");
			}
			
			if (dsncd.length() <= 0){
				throw new RollBackException("\ub514\ub809\ud1a0\ub9ac \ucc98\ub9ac \uc911 \uc624\ub958\uac00 \ubc1c\uc0dd\ud558\uc600\uc2b5\ub2c8\ub2e4.");
			}
			
			param = new HashMap<String, String>();
			param.put("cr_syscd", newList.get("syscd").toString());
			param.put("cr_dsncd", dsncd);
			param.put("cr_rsrcname", newList.get("rsrcname").toString().toUpperCase());
			
			itemid = "";
			itemid = fileDAO.getFileInfo_noitemid(param);
			param = null;
			/*if (itemid == null){
				param = new HashMap();
				param.put("CR_SYSCD", newList.get("syscd"));
				param.put("CR_DSNCD", dsncd);
				param.put("CR_RSRCNAME", newList.get("rsrcname"));
				param.put("CR_RSRCCD", newList.get("rsrccd"));
				param.put("CR_JOBCD", newList.get("jobcd"));
				param.put("CR_LANGCD", "01");
				param.put("CR_CREATOR", newList.get("userid"));
				param.put("CR_STORY", newList.get("sayu"));
				param.put("CR_EDITOR", newList.get("userid"));
				param.put("CR_PGMTYPE", "");
				param.put("CR_LSTUSR", newList.get("userid"));
				
				if (fileDAO.insertCmr0020(param) <= 0){
					param = null;
					throw new RollBackException("\ud30c\uc77c\ub4f1\ub85d \uc2e4\ud328 1");
				}else{
					param = new HashMap();
					param.put("cr_syscd", newList.get("syscd"));
					param.put("cr_dsncd", dsncd);
					param.put("cr_rsrcname", newList.get("rsrcname").toString().toUpperCase());
					itemid = fileDAO.getFileInfo_noitemid(param);
					if (itemid == null){
						throw new RollBackException("\ud30c\uc77c\ub4f1\ub85d \uc2e4\ud328 1");
					}
					param = null;
				}
			}else{
				throw new RollBackException("\uc774\ubbf8\ub4f1\ub85d \ub418\uc5b4\uc788\ub294 \ud30c\uc77c\uc774 \uc788\uc2b5\ub2c8\ub2e4 \ub3d9\uae30\ud654 \uc2e4\ud589\ud6c4 \ub2e4\uc2dc \uc2e0\uccad\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
			}*/
			
			boolean inflg = false;
			
			System.out.println("\uc2e0\uaddc\ub4f1\ub85d itemid: "+itemid);
			if (itemid != null){
				if(fileDAO.getFileInfo(itemid).get("CR_STATUS").equals("9")){
					//logger.error("Status:9["+itemid+"]");
					
					param = new HashMap<String, String>();
					param.put("item", itemid);
					fileDAO.delete_cmr0025(param);
					fileDAO.delete_cmr0021(param);
					
					fileDAO.delete_cmr1010(itemid);
					fileDAO.delete_cmr0022(itemid);
					fileDAO.delete_cmr0020(itemid);
				}else{
					inflg = true;
				}
			}
			if(!inflg){
				param = new HashMap<String, String>();
				param.put("CR_SYSCD", newList.get("syscd").toString());
				param.put("CR_DSNCD", dsncd);
				param.put("CR_RSRCNAME", newList.get("rsrcname").toString());
				//param.put("CR_RSRCCD", newList.get("rsrccd").toString());
				param.put("CR_RSRCCD", rsrccd);
				param.put("CR_JOBCD", newList.get("jobcd").toString());
				param.put("CR_LANGCD", "01");
				param.put("CR_CREATOR", newList.get("userid").toString());
				param.put("CR_STORY", newList.get("sayu").toString());
				param.put("CR_EDITOR", newList.get("userid").toString());
				param.put("CR_PGMTYPE", "");
				param.put("CR_LSTUSR", newList.get("userid").toString());
				param.put("CR_ISRID", newList.get("srid").toString());
				
				if (fileDAO.insertCmr0020(param) <= 0){
					param = null;
					throw new RollBackException("\ud30c\uc77c\ub4f1\ub85d \uc2e4\ud328 1");
				}else{
					param = new HashMap<String, String>();
					param.put("cr_syscd", newList.get("syscd").toString());
					param.put("cr_dsncd", dsncd);
					param.put("cr_rsrcname", newList.get("rsrcname").toString().toUpperCase());
					itemid = fileDAO.getFileInfo_noitemid(param);
					if (itemid == null){
						throw new RollBackException("\ud30c\uc77c\ub4f1\ub85d \uc2e4\ud328 1");
					}
					param = null;
				}
			}else{
				throw new RollBackException(newList.get("rsrcname").toString()+"\ud3d0\uae30\uc0c1\ud0dc\uac00 \uc544\ub2d9\ub2c8\ub2e4.");
			}
			return itemid;
		}catch (RollBackException e){
			e.printStackTrace();
			logger.error("FileService.rollback: ",e);
			return null;
		}
		catch (Exception e){
			e.printStackTrace();
			logger.error("FileService.exception: ",e);
			return null;
		}
		finally{
		}
	}

	public String registAllCheckInFile (HashMap<String, String> newList){
		HashMap<String, String> param = new HashMap<String, String>();
		String itemid = "";
		try{

			/*\uccb4\ud06c\uc778\uc2dc \ud655\uc7a5\uc790 \uc790\ub3d9\uccb4\ud06c\ud574\uc8fc\ub294 \ubd80\ubd84\uc744 \uc77c\uad04\ub4f1\ub85d\uc5d0\uc11c \uc0ac\uc6a9\uc790\uac00 \uc9c0\uc815\ud558\ub3c4\ub85d \ud588\uc73c\ubbc0\ub85c \uc6b0\uc120 \uc8fc\uc11d \ucc98\ub9ac \ud558\uace0, null\uc77c\ub54c\ub9cc \uc790\ub3d9 \ub4f1\ub85d\ub418\uac8c*/
//			String rsrccd = null;
//
//			int j=0;
//			int k=0;
//
//			List<ResourceType> rsuList = (List<ResourceType>) resourceTypeDAO.getRsrcInfo(newList.get("syscd").toString());
//			
//			for (j=0; j<rsuList.size(); j++) {
//				if(rsuList.get(j).getResourceExeName() != null) {
//					String[] exename = rsuList.get(j).getResourceExeName().split(",");
//					for (k=0; k<exename.length; k++) {  
//						if(newList.get("rsrcname").toUpperCase().lastIndexOf(exename[k].toUpperCase()) > 0
//								|| newList.get("rsrcname").toUpperCase().equals(exename[k].toUpperCase())) {
//							rsrccd = rsuList.get(j).getResourceType();
//							break;
//						}
//					}
//				}
//			}
//			
//			if(null == rsrccd) {
//				/*for (j=0; j<rsuList.size(); j++) {
//					if (rsuList.get(j).getResourceCmInfo().substring(40,41).equals("1")) {//\ud655\uc7a5\uc790 \uccb4\ud06c\uc548\ud568
//						rsrccd = rsuList.get(j).getResourceType();
//						break;
//					}
//				}*/
//				rsrccd = "08"; //WEB\ud30c\uc77c
//			}
//			
//			rsuList = null;
			String rsrccd = null;
			rsrccd = newList.get("rsrccd").toString();
			int j=0;
			int k=0;

			List<ResourceType> rsuList = (List<ResourceType>) resourceTypeDAO.getRsrcInfo(newList.get("syscd").toString());
			if(null == rsrccd) {
				for (j=0; j<rsuList.size(); j++) {
					if(rsuList.get(j).getResourceExeName() != null) {
						String[] exename = rsuList.get(j).getResourceExeName().split(",");
						for (k=0; k<exename.length; k++) {  
							if(newList.get("rsrcname").toUpperCase().lastIndexOf(exename[k].toUpperCase()) > 0
									|| newList.get("rsrcname").toUpperCase().equals(exename[k].toUpperCase())) {
								rsrccd = rsuList.get(j).getResourceType();
								break;
							}
						}
					}
				}
			}
			
			
			System.out.println(">>>registAllCheckInFile: rsrccd:"+rsrccd);
			System.out.println(">>>registAllCheckInFile: jobcd:"+newList.get("jobcd").toString());
			
			if (newList.get("dirpath") == null || "".equals(newList.get("dirpath"))){
				param.put("dirpath","/");
			}else{
				param.put("dirpath",newList.get("dirpath").toString());
			}
			
			param.put("cm_syscd", newList.get("syscd").toString());
			//param.put("cm_rsrccd", newList.get("rsrccd").toString());
			param.put("cm_rsrccd", rsrccd);
			param.put("cm_jobcd", newList.get("jobcd").toString());
			param.put("cm_userid", newList.get("userid").toString());
			
			String dsncd = pathService.getPathCD(param);
				
			if (dsncd == null){
				throw new RollBackException("\ub514\ub809\ud1a0\ub9ac \ucc98\ub9ac \uc911 \uc624\ub958\uac00 \ubc1c\uc0dd\ud558\uc600\uc2b5\ub2c8\ub2e4.");
			}
			
			if (dsncd.length() <= 0){
				throw new RollBackException("\ub514\ub809\ud1a0\ub9ac \ucc98\ub9ac \uc911 \uc624\ub958\uac00 \ubc1c\uc0dd\ud558\uc600\uc2b5\ub2c8\ub2e4.");
			}
			
			param = new HashMap<String, String>();
			param.put("cr_syscd", newList.get("syscd").toString());
			param.put("cr_dsncd", dsncd);
			param.put("cr_rsrcname", newList.get("rsrcname").toString().toUpperCase());
			
			itemid = "";
			itemid = fileDAO.getFileInfo_noitemid(param);
			param = null;
			boolean inflg = false;
			
			System.out.println("\uc2e0\uaddc\ub4f1\ub85d itemid: "+itemid);
			if (itemid != null){
				if(fileDAO.getFileInfo(itemid).get("CR_STATUS").equals("9")){
					//logger.error("Status:9["+itemid+"]");
					
//					param = new HashMap<String, String>();
//					param.put("item", itemid);
//					fileDAO.delete_cmr0025(param);
//					fileDAO.delete_cmr0021(param);
//					
//					fileDAO.delete_cmr1010(itemid);
//					fileDAO.delete_cmr0022(itemid);
//					fileDAO.delete_cmr0020(itemid);
					throw new RollBackException(newList.get("rsrcname").toString()+" \ud3d0\uae30\uc0c1\ud0dc\uc785\ub2c8\ub2e4. \uc2e0\uaddc\ub4f1\ub85d\uc774 \ubd88\uac00\ud569\ub2c8\ub2e4. \uad00\ub9ac\uc790\uc5d0\uac8c \ubb38\uc758\ud574\uc8fc\uc138\uc694.");
					
				}else{
					inflg = true;
				}
			}
			if(!inflg){
				param = new HashMap<String, String>();
				param.put("CR_SYSCD", newList.get("syscd").toString());
				param.put("CR_DSNCD", dsncd);
				param.put("CR_RSRCNAME", newList.get("rsrcname").toString());
				//param.put("CR_RSRCCD", newList.get("rsrccd").toString());
				param.put("CR_RSRCCD", rsrccd);
				param.put("CR_JOBCD", newList.get("jobcd").toString());
				param.put("CR_LANGCD", "01");
				param.put("CR_CREATOR", newList.get("userid").toString());
				param.put("CR_STORY", newList.get("sayu").toString());
				param.put("CR_EDITOR", newList.get("userid").toString());
				param.put("CR_PGMTYPE", "");
				param.put("CR_LSTUSR", newList.get("userid").toString());
				
				if( newList.get("srid") != null ) {
					param.put("CR_ISRID", newList.get("srid").toString());
				}else {
					param.put("CR_ISRID", "");
				}
				if (fileDAO.insertCmr0020(param) <= 0){
					param = null;
					throw new RollBackException("\ud30c\uc77c\ub4f1\ub85d \uc2e4\ud328 1");
				}else{
					param = new HashMap<String, String>();
					param.put("cr_syscd", newList.get("syscd").toString());
					param.put("cr_dsncd", dsncd);
					param.put("cr_rsrcname", newList.get("rsrcname").toString().toUpperCase());
					itemid = fileDAO.getFileInfo_noitemid(param);
					if (itemid == null){
						throw new RollBackException("\ud30c\uc77c\ub4f1\ub85d \uc2e4\ud328 1");
					}
					param = null;
				}
			}else{
				throw new RollBackException(newList.get("rsrcname").toString()+" \ud604\uc7ac \uc6b4\uc601\uc911\uc778 \ud504\ub85c\uadf8\ub7a8\uc785\ub2c8\ub2e4. \uc2e0\uaddc\ub4f1\ub85d\uc774 \ubd88\uac00\ud569\ub2c8\ub2e4. \uad00\ub9ac\uc790\uc5d0\uac8c \ubb38\uc758\ud574\uc8fc\uc138\uc694.");
			}
			return itemid;
		}catch (RollBackException e){
			e.printStackTrace();
			logger.error("FileService.rollback: ",e);
			return null;
		}
		catch (Exception e){
			e.printStackTrace();
			logger.error("FileService.exception: ",e);
			return null;
		}
		finally{
		}
	}	
	
	public HashMap<String, String> getFileStatus(String itemid, String qrycd) {
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("itemid",itemid);
		
		
		if (!qrycd.equals("01")){
			params.put("checkin",itemid);
		}
		
		HashMap<String, String> filestatus = fileDAO.getStatus(params);
		
		
		return filestatus;
	}
	
	public HashMap<String, String> getFileInfo(String itemid) {
		// TODO Auto-generated method stub
		return (HashMap<String, String>)fileDAO.getFileInfo(itemid);
	}

	public int updateStatus(HashMap<String, String> param) {
		// TODO Auto-generated method stub
		return fileDAO.updateStatus(param);
	}

	public int insertFileData(HashMap<String, String> param) {
		// TODO Auto-generated method stub
		return fileDAO.insertFileData(param);
	}
}
