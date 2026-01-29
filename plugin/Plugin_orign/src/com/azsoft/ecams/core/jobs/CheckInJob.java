package com.azsoft.ecams.core.jobs;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Display;


import com.azsoft.ecams.util.checksum.CheckSum;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.core.CommandExecuter;
import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.FileDataList;
import com.azsoft.ecams.proto.ProtoEcams.PathInfo;
import com.azsoft.ecams.proto.ProtoEcams.RequestInfo;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SRInfo;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.proto.ProtoEcams.RsrcInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.util.file.EFileToByteArray;
import com.azsoft.ecams.util.file.EFileToByteArray2;
import com.azsoft.ecams.util.file.HashData;

import com.azsoft.ecams.ui.dialog.ConfirmReqGbnDlg;
import com.azsoft.ecams.ui.dialog.ErrorMessageDlg;
import com.google.protobuf.ByteString;


public class CheckInJob extends WorkspaceJob {
	//private Logger logger = Logger.getLogger(this.getClass());
	
	private IResource[] resources;
	private String sayu,errMsg;
	
	private List errList = new ArrayList();
	private List labelRefreshList = new ArrayList();
	private ArrayList<HashMap<String, String>> ModList = new ArrayList<HashMap<String, String>>();
	private String ReqCd, srId, srTitle, ReqGbn;
	private boolean chkModify;

	public CheckInJob(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public CheckInJob(String name, IResource[] resources, ArrayList<HashMap<String, String>> ModList, 
			          String sayu, String ReqCd, String srId, String srTitle, String ReqGbn, boolean chkModify) {
		super(name);
		this.resources = resources;
		this.ModList = ModList;
		this.sayu = sayu;
		this.srId = srId;
		this.srTitle = srTitle;
		this.ReqCd = ReqCd;
		this.ReqGbn = ReqGbn;
		this.chkModify = chkModify;
		addJobChangeListener(new CheckInJobChangeListener());
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)	throws CoreException {
		// TODO Auto-generated method stub
		
		int i;
		
		try{
			/*if("A".equals(ReqGbn)){
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						ConfirmReqGbnDlg confirmReqGbnDlg = new ConfirmReqGbnDlg(new Shell().getShell());
						confirmReqGbnDlg.open();

						if(confirmReqGbnDlg.getReturnCode() == ConfirmReqGbnDlg.OK){
							ReqGbn = confirmReqGbnDlg.getReqGbn();
						}else{
							ReqGbn = "X";
						}
					}
				});
			}*/
			
			/*
			if("X".equals(ReqGbn)){
				errMsg = "\uccb4\ud06c\uc778\uc2e0\uccad\uc744 \ucde8\uc18c\ud558\uc168\uc2b5\ub2c8\ub2e4.";
				return Status.CANCEL_STATUS;
			}
			*/
			
			errList.clear();
			IEcamsStatus[] filestatuses = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatuses_regist(resources);
			
			if (filestatuses.length != resources.length){
				errMsg = "\uc0c1\ud0dc\uac12 \uc624\ub958.";
				return Status.CANCEL_STATUS;
			}

			String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
			String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);

			if(ip == null || ip.equals("")){
				errMsg = "Preferences\uc5d0\uc11c IP\ub97c \uc785\ub825\ud558\uc138\uc694.";
				return Status.CANCEL_STATUS;
			}
			if(port == null || port.equals("")){
				errMsg = "Preferences\uc5d0\uc11c PORT\ub97c \uc785\ub825\ud558\uc138\uc694.";
				return Status.CANCEL_STATUS;
			}
			if(id == null || id.equals("")){
				errMsg = "Preferences\uc5d0\uc11c ID\ub97c \uc785\ub825\ud558\uc138\uc694.";
				return Status.CANCEL_STATUS;
			}
			
			if(passwd == null || passwd.equals("")){
				errMsg = "Preferences\uc5d0\uc11c PASSWORD\ub97c \uc785\ub825\ud558\uc138\uc694.";
				return Status.CANCEL_STATUS;
			}
			
			FileDataList.Builder fileDataList_builder = FileDataList.newBuilder();

			IProject project= resources[0].getProject();

			if(!project.isAccessible()){
				errMsg = "Poject is closed.";
				return Status.CANCEL_STATUS;
			}else{
				if(!EcamsRepositoryProvider.isManagedByEcams(project)){
					errMsg = "Is Not eCAMS Project.[ Connection Project -> Properties eCAMS PlugIn]";
					return Status.CANCEL_STATUS;
				}
			}
			
			String tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
			if(null == tool || "".equals(tool)){
				errMsg = "Preferences\uc5d0\uc11c \uac1c\ubc1c \ud234\uc744 \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.";
				return Status.CANCEL_STATUS;
			}
			
			if(!"16".equals(ReqCd) && "I".equals(tool)){
				/*
				 * project checkin before process
				 * command.team.BeforeCheckIn
				 */
				try {
					Map<String, Map<String, String>> parameter = new HashMap<String, Map<String, String>>(); 
					Map<String, String> mapParam = new HashMap<String, String>();
					
					for (i=0;i<resources.length;i++){
						IEcamsStatus tmpStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(resources[i]);
						if(tmpStatus != null){
//							mapParam.put(resources[i].getProject().getName()+"/"+resources[i].getProjectRelativePath().toString(), Integer.toString(tmpStatus.getLastVer())+"."+tmpStatus.getTstVer());
							mapParam.put(resources[i].getProject().getName()+"/"+resources[i].getProjectRelativePath().toString(), tmpStatus.getViewver());
						}
					}
					
					parameter.put(project.getName(), mapParam);
					CommandExecuter.executeCommand("command.team.BeforeCheckIn", parameter);

					mapParam = null;
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					errMsg = e1.getMessage();
					return Status.CANCEL_STATUS;
				}
			}
			
			
			
			FileData.Builder fileData_builder = FileData.newBuilder();
			PathInfo.Builder pathinfo_builder = PathInfo.newBuilder();
			
			for (i=0;i<filestatuses.length;i++){
				fileData_builder = FileData.newBuilder();
				if(filestatuses[i] == null){
					fileData_builder.setItemid("");
					fileData_builder.setStatus("");
					fileData_builder.setReqcd("04");
				}else{
					fileData_builder.setItemid(filestatuses[i].getItemid());
					fileData_builder.setBaseitem(filestatuses[i].getItemid());
					fileData_builder.setStatus(filestatuses[i].getFileStatus());
					if (filestatuses[i].getFileStatus().equals("3")) fileData_builder.setReqcd("03");
					else fileData_builder.setReqcd("04");
				}
				fileData_builder.setMsguse("P");
				fileData_builder.setFilename(resources[i].getName());
				
				pathinfo_builder = PathInfo.newBuilder();
				pathinfo_builder.setRelativitePath(((IResource)resources[i].getParent()).getProjectRelativePath().toString());
				//pathinfo_builder.setRelativitePath(((IResource)resources[i]).getProjectRelativePath().toString());
				fileData_builder.setPathinfo(pathinfo_builder.build());
				fileDataList_builder.addFiledatas(fileData_builder.build());
				fileData_builder=null;
			}
			
			if(null != ModList){
				for (i=0;i<ModList.size();i++){
					fileData_builder = FileData.newBuilder();
					fileData_builder.setItemid(ModList.get(i).get("itemid"));
					fileData_builder.setBaseitem(ModList.get(i).get("baseitem"));
					fileData_builder.setReqcd("04");
					
					fileData_builder.setMsguse("M");
					fileData_builder.setFilename(ModList.get(i).get("rsrcname"));
					
					pathinfo_builder = PathInfo.newBuilder();
					pathinfo_builder.setRelativitePath(ModList.get(i).get("dirpath"));
					
					RsrcInfo.Builder rsrcinfo_builder = RsrcInfo.newBuilder();
					rsrcinfo_builder.setRsrccd("N");
					rsrcinfo_builder.setCminfo(ModList.get(i).get("cminfo"));
					fileData_builder.setRsrcinfo(rsrcinfo_builder.build());
					rsrcinfo_builder = null;
					
					fileData_builder.setPathinfo(pathinfo_builder.build());
					fileDataList_builder.addFiledatas(fileData_builder.build());
					fileData_builder=null;
				}
			}
			
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(id);
			userinfo_builder.setPasswd(passwd);
			
			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
			sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
			sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);

			RequestInfo.Builder requestinfo_builder = RequestInfo.newBuilder();
			//requestinfo_builder.setQrycd("04");
			requestinfo_builder.setQrycd(ReqCd);
			requestinfo_builder.setSayu(sayu);

//			requestinfo_builder.setSvrYN("Y");
			requestinfo_builder.setSvrYN("N");
			requestinfo_builder.setVersion("Y");
//			if(null != ReqGbn && !"".equals(ReqGbn)){
//				if("V".equals(ReqGbn)){
//					requestinfo_builder.setSvrYN("N");
//					requestinfo_builder.setVersion("Y");
//				}else if("D".equals(ReqGbn)){
//					requestinfo_builder.setSvrYN("Y");
//					requestinfo_builder.setVersion("N");
//				}
//			}

			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			
			if(!chkModify){
				SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
				srinfo_builder.setCcEditor(id);
				srinfo_builder.setCcSRId(srId);
				srinfo_builder.setCcTitle(srTitle);
				builder_msg.setSrinfo(srinfo_builder.build());	
			}
			
			builder_msg.setMsgtype("CHECKIN");
			builder_msg.setRequestinfo(requestinfo_builder.build());
			builder_msg.setFiledatalist(fileDataList_builder.build());
			builder_msg.setUserinfo(userinfo_builder.build());
			builder_msg.setSysinfo(sysinfo_builder.build());
			builder_msg.setTooltype(tool); 

			EcamsClient ecamsclient = new EcamsClient(ip,port);
			ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
			

			int errcnt=0;
			
			if (returnMsg.getReturnStr().startsWith("SOCKERR")){
				errMsg = "SOCKET ERROR";
				errcnt = 2;
				return Status.CANCEL_STATUS;
			}else{
				if (returnMsg.getReturnval() != 0){
					errMsg = "\uccb4\ud06c\uc778 \uc2e4\ud328 \ucc98\ub9ac\uacb0\uacfc["+returnMsg.getReturnStr()+"]";
					EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,"","NONE"));
					errcnt = 1;
					return Status.CANCEL_STATUS;			
				}
			}
			
			String acptno = returnMsg.getReturnStr();
			
			FileDataList filedatalist = returnMsg.getEcamsmsg().getFiledatalist();
			fileDataList_builder = FileDataList.newBuilder();
			

			monitor.beginTask("P/"+project.getName()+" \uc11c\ubc84\ub85c \ud30c\uc77c \uc804\uc1a1 \uc911..", filestatuses.length+2);
			
			File filez = null;
			String filepath = "";

			//for (i=0;i<filestatuses.length;i++){
			for(i=0; i<filedatalist.getFiledatasCount(); i++){
				//\ube4c\ub4dc\uc11c\ubc84\uc5d0\uc11c \uccb4\ud06c\uc778
				if (filedatalist.getFiledatas(i).getMsguse() != null && filedatalist.getFiledatas(i).getMsguse().equals("M")) {
					if("1".equals(filedatalist.getFiledatas(i).getRsrcinfo().getCminfo().substring(24,25))){
						continue;
					}
				}
    			
				//monitor.subTask(filestatuses[i].getName());
				monitor.subTask(filedatalist.getFiledatas(i).getFilename());
				monitor.worked(1);		
    			
    			filepath = (project.getLocation().toString()+"/"+filedatalist.getFiledatas(i).getPathinfo().getRelativitePath());
    			
    			while(filepath.indexOf("/") >=0){
    				filepath = filepath.replace("/","\\");
    			}
    			
    			while(filepath.indexOf("\\\\") >=0){
    				filepath = filepath.replace("\\\\", "\\");
    			}

    			while(filepath.indexOf("\\") >=0){
    				filepath = filepath.replace("\\", "/");
    			}	
    			
    			
				filez = new File(filepath+"/"+filedatalist.getFiledatas(i).getFilename());
				
				if(!filez.exists()){
					errcnt = 1;
					errMsg = "\ud30c\uc77c\uc774 \uc874\uc7ac\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4. \n("+filepath+"/"+filedatalist.getFiledatas(i).getFilename()+")";
					//return Status.CANCEL_STATUS;
					break;
				}
				
				requestinfo_builder = RequestInfo.newBuilder();
				builder_msg = EcamsMessage.newBuilder();
				String md5sum="";
				requestinfo_builder.setAcptno(acptno);
				requestinfo_builder.setQrycd(ReqCd);
				
				int lasthou = 0;
				int lastmin = 0;
				int lastsec = 0;
				String lastdat = "";
				if(i < resources.length) {
					lastdat = (new java.sql.Date(resources[i].getLocalTimeStamp())).toString().replaceAll("-", "");
					lasthou = new Date(resources[i].getLocalTimeStamp()).getHours();
					lastmin = new Date(resources[i].getLocalTimeStamp()).getMinutes();
					lastsec = new Date(resources[i].getLocalTimeStamp()).getSeconds();
				}
				if(lasthou<10){
					lastdat=lastdat+"0"+lasthou;
				}else{
					lastdat=lastdat+lasthou;
				}
				if(lastmin<10){
					lastdat=lastdat+"0"+lastmin;
				}else{
					lastdat=lastdat+lastmin;
				}
				if(lastsec<10){
					lastdat=lastdat+"0"+lastsec;
				}else{
					lastdat=lastdat+lastsec;
				}
				
				//List<byte[]> testbyte = null;
				byte[] testbyte = null;
				try {
					//FileChannel inChannel = new FileInputStream(filestatuses[i].getFile()).getChannel();
					FileChannel inChannel = new FileInputStream(filez).getChannel();
					int size = (int)inChannel.size();
					if(size<1){
						throw new IOException("Error");
					}

				    System.out.println("+++size ["+size+"]");
				    
					if (size>=1024*1024*10){//10mb\ubcf4\ub2e4 \ud06c\uba74
						try{
							ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
							InputStream in = null;
							BufferedInputStream bis = null;
							
							int maxFileSize = 1024*1024*10;//10mb
						    byte[] newBytes = new byte[maxFileSize];
						    if (size < maxFileSize) {
						    	newBytes = new byte[size];
						    }
						    
						    in=new FileInputStream(filez);
						    bis = new BufferedInputStream(in);
						    
						    int nRead;
						    int page1 = size/maxFileSize;//\ubaab
						    int page2 = size%maxFileSize;//\ub098\uba38\uc9c0
						    if (page2>0) page1 = page1 + 1;

					    	System.out.println("page1:"+page1);
					    	System.out.println("page2:"+page2);
					    	
						    int sendCnt=0;
						    while ((nRead = bis.read(newBytes)) >= 0) {
						    	byteBuffer = new ByteArrayOutputStream();
						    	byteBuffer.write(newBytes, 0, nRead);

						    	System.out.println("byteBuffer size:"+byteBuffer.size());
						    	System.out.println("nRead:"+nRead);
						    	
						    	System.out.println("sendCnt:"+sendCnt);

								fileData_builder = FileData.newBuilder();
								fileData_builder.setLstdate(lastdat);
								fileData_builder.setFilename(filedatalist.getFiledatas(i).getFilename());
								fileData_builder.setItemid(filedatalist.getFiledatas(i).getItemid());
					    		fileData_builder.setFilebytes(ByteString.copyFrom(byteBuffer.toByteArray()));
					    		md5sum = CheckSum.MD5SumVal(filez);
					    		fileData_builder.setMd5Sum(md5sum);
						    	System.out.println("md5sum:"+md5sum);

					    		builder_msg = EcamsMessage.newBuilder();
								builder_msg.setMsgtype("NEW_FILETRANS");
								builder_msg.setPagenum(++sendCnt);//\ud604\uc7ac\uac04\uac70
								builder_msg.setTotpage(page1);//\uc804\uccb4
								builder_msg.setFiledata(fileData_builder.build());
								builder_msg.setRequestinfo(requestinfo_builder.build());
								builder_msg.setUserinfo(userinfo_builder.build());
								
								returnMsg = ecamsclient.sendMsg(builder_msg.build());
					
								if(returnMsg.getReturnval()>0){
									errcnt = 1;
									errMsg = "CheckIn ERROR [FILETRANS] : "+returnMsg.getReturnStr();
									break;
								}
						    	byteBuffer = null;
						    }
							
						    in.close();
						    in = null;
						    bis.close();
						    bis = null;
						    
						    continue;
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (Exception e){
							e.printStackTrace();
							System.out.println(e);
						}
					} else {
						testbyte = EFileToByteArray.FileToByteArray(filez);
						md5sum = CheckSum.MD5SumVal(testbyte);
						
						if (filez.length() < 1 || md5sum == null || "".equals(md5sum)) {
							errcnt++;
							errMsg = "FILE SIZE ZERO. ["+filedatalist.getFiledatas(i).getFilename()+"]";
						} else {
							fileData_builder = FileData.newBuilder();
							fileData_builder.setLstdate(lastdat);
							fileData_builder.setMd5Sum(md5sum);
							fileData_builder.setFilebytes(ByteString.copyFrom(testbyte));
							fileData_builder.setFilename(filedatalist.getFiledatas(i).getFilename());
							fileData_builder.setItemid(filedatalist.getFiledatas(i).getItemid());
							fileDataList_builder.addFiledatas(fileData_builder.build());
							fileData_builder = null;
						}
					}
				} catch (IOException e) {
					errcnt = 1;
					errMsg = filedatalist.getFiledatas(i).getFilename()+" \ud30c\uc77c \uc77d\uae30 \uc2e4\ud328(Size:0).";
					//return Status.CANCEL_STATUS;
					break;
				} finally {
					testbyte = null;
				}
			}
			
			if(fileDataList_builder.getFiledatasCount() > 0){
				builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("FILETRANS");
				builder_msg.setFiledatalist(fileDataList_builder.build());
				builder_msg.setRequestinfo(requestinfo_builder.build());
				builder_msg.setUserinfo(userinfo_builder.build());
				
				returnMsg = ecamsclient.sendMsg(builder_msg.build());
	
				if(returnMsg.getReturnval()>0){
					errcnt = 1;
					errMsg = "CheckIn ERROR [FILETRANS] : "+returnMsg.getReturnStr();
				}
			}
			
			if(errcnt>0) {
				builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("REQUEST_ALLCNCL");
				RequestInfo.Builder request_builder = RequestInfo.newBuilder();
				//request_builder.setQrycd(ReqCd);
				request_builder.setQrycd("07");
				request_builder.setAcptno(acptno);
				builder_msg.setRequestinfo(request_builder.build());
				builder_msg.setUserinfo(userinfo_builder.build());
				returnMsg = ecamsclient.sendMsg(builder_msg.build());
				//final String errStr = returnMsg.getReturnStr()+"\n"+"\ud615\uc0c1\uad00\ub9ac \ub2f4\ub2f9\uc790\uc5d0\uac8c \uc5f0\ub77d\ud574\uc8fc\uc2dc\uae30 \ubc14\ub78d\ub2c8\ub2e4.";
				final String errStr = returnMsg.getReturnStr();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						//MessageDialog.openError(new Shell(),errMsg,errStr);
						MessageDialog.openError(new Shell(),errStr,errMsg);
					}
				});

				Thread.sleep(1000);
				EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,acptno,"NONE"));
			} else {
				builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("REQUEST_COMPLETE");
				RequestInfo.Builder request_builder = RequestInfo.newBuilder();
				//request_builder.setQrycd(ReqCd);
				request_builder.setQrycd("07");
				
				request_builder.setAcptno(acptno);
				builder_msg.setRequestinfo(request_builder.build());
				builder_msg.setUserinfo(userinfo_builder.build());
				returnMsg = ecamsclient.sendMsg(builder_msg.build());
				final String errStr = returnMsg.getReturnStr()+"\n"+"\uccb4\ud06c\uc778\uc624\ub958\ubc1c\uc0dd\uc73c\ub85c \uc644\ub8cc\ub418\uc9c0 \ubabb\ud588\uc2b5\ub2c8\ub2e4. \ud615\uc0c1\uad00\ub9ac \ub2f4\ub2f9\uc790\uc5d0\uac8c \uc5f0\ub77d\ud574\uc8fc\uc2dc\uae30 \ubc14\ub78d\ub2c8\ub2e4.";
				if(returnMsg.getReturnval()>0){
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(new Shell(),"CheckIn ERROR",errStr);
						}
					});
					//return Status.CANCEL_STATUS;
				}
				
				Thread.sleep(1000);
				EcamsProviderPlugin.getPlugin().getJobManager().addJob(new UpdateStatusJob("Resource Status Updating..",resources,acptno));
			}
			
			/*if(errcnt == 1){
				EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,"","NONE"));
			}else if(errcnt == 0){
				EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,acptno,"NONE"));
			}*/
			
			
			return Status.OK_STATUS;
			
		}catch (CoreException e){
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}finally{
			monitor.done();
		}
	}
	
	
	final class CheckInJobChangeListener extends JobChangeAdapter {

		public void done(IJobChangeEvent event) {
			IStatus result = event.getResult();
			if(result.getSeverity() == IStatus.CANCEL) {
				if(!"".equals(errMsg)){
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							/*
							final int option = JOptionPane.showConfirmDialog(null, errMsg+"\n"+"\ub3d9\uae30\ud654\ub97c \ud558\uc2dc\uaca0\uc2b5\ub2c8\uae4c?", "CheckIn ERROR", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
							if(option == JOptionPane.YES_OPTION){
								EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,""));
							}
							*/
							MessageBox messageBox = new MessageBox(new Shell());
							messageBox.setMessage(errMsg);
							messageBox.setText("CheckIn ERROR");
							messageBox.open();
						}
					});
				}
				return;
			} else if(!result.isOK()) {
				// XXX report errors
				return;
			}
			
			if (errList.size() > 0){
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						ErrorMessageDlg errDlg = new ErrorMessageDlg(new Shell(),(IResource[])errList.toArray(new IResource[errList.size()]),"\uccb4\ud06c\uc778\uc624\ub958","\ub2e4\uc74c \ud30c\uc77c\ub4e4\uc740 \uccb4\ud06c\uc778\uc911 \uc624\ub958\uac00 \ubc1c\uc0dd\ud558\uc5ec \uc790\ub3d9\ubc18\ub824 \ub418\uc5c8\uc73c\ub2c8 \ub2e4\uc2dc \uccb4\ud06c\uc778 \ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
						errDlg.open();
					}
				});
			}
			else{
				if (labelRefreshList.size()>0){
					EcamsProviderPlugin.broadcastModificationStateChanges((IResource[]) labelRefreshList.toArray(new IResource[labelRefreshList.size()]));
				}
			}

		}
	}
}
