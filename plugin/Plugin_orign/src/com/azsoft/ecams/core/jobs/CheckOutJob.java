package com.azsoft.ecams.core.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.azsoft.ecams.core.CommandExecuter;
import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.core.resource.EcamsResourceStatus;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.FileDataList;
import com.azsoft.ecams.proto.ProtoEcams.JobInfo;
import com.azsoft.ecams.proto.ProtoEcams.JobInfoList;
import com.azsoft.ecams.proto.ProtoEcams.RequestInfo;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.RsrcInfo;
import com.azsoft.ecams.proto.ProtoEcams.RsrcInfoList;
import com.azsoft.ecams.proto.ProtoEcams.SRInfo;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.dialog.ConfirmMergeMessageDlg;
import com.azsoft.ecams.ui.dialog.ConfirmMessage4Dlg;
import com.azsoft.ecams.ui.dialog.ErrorMessageDlg;
import com.azsoft.ecams.util.checksum.CheckSum;
import com.azsoft.ecams.util.file.EFileCopy;
import com.azsoft.ecams.util.file.EFileToByteArray;
import com.azsoft.ecams.util.file.EGzip;
import com.google.protobuf.ByteString;
import com.sun.corba.se.impl.ior.ByteBuffer;

public class CheckOutJob extends WorkspaceJob {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private IResource[] resources;
	private String sayu;
	private List errList = new ArrayList();
	
	//private String CSRNO=null;
	private String errMsg=null;
	//private String downDlgStr = "0";
	private int iMergeDlgStatus = ConfirmMergeMessageDlg.CONFIRM_INIT;
	
	private List labelRefreshList = new ArrayList();
	private String srId, srTitle;
	private boolean chkModify;
	
	final private int IS_BINARY	= 9; //cm_micode 10
	
	public CheckOutJob(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public CheckOutJob(String name,IResource[] resources,String sayu,String CSRNO, String srId, String srTitle, boolean chkModify) {
		super(name);
		this.resources = resources;
		//this.CSRNO = CSRNO;
		this.sayu = sayu;
		this.srId = srId;
		this.srTitle = srTitle;
		this.chkModify = chkModify;
		
		addJobChangeListener(new CheckOutJobChangeListener());
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		String tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
		Map<String, String> commandmapParam = new HashMap<String, String>();
		
		IProject project = null;

		int errcnt = 0;
		
		try{
			int i,j;
			//boolean rtFlag=false;
			boolean downFlag=false;
			boolean mergeFlag = false;
			
			errList.clear();
			
			IEcamsStatus[] filestatuses = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatuses(resources);
			if (filestatuses.length != resources.length){
				errMsg = "\uc0c1\ud0dc\uac12 \uc624\ub958.";
				//EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,""));
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

			project= resources[0].getProject();

			if(!project.isAccessible()){
				errMsg = "Poject is closed.";
				return Status.CANCEL_STATUS;
			}else{
				if(!EcamsRepositoryProvider.isManagedByEcams(project)){
					errMsg = "Is Not eCAMS Project.[ Connection Project -> Properties eCAMS PlugIn]";
					return Status.CANCEL_STATUS;
				}
			}
			
			FileDataList.Builder fileDataList_builder = FileDataList.newBuilder();
			
			Set<RsrcInfo> setrsrcinfo = new HashSet<RsrcInfo>();
			
			RsrcInfoList.Builder rsrcInfoList_builder = RsrcInfoList.newBuilder();
			RsrcInfo.Builder rsrcInfo_builder = null;
			JobInfoList.Builder jobinfo_builder = JobInfoList.newBuilder();
			
			for (i=0;i<filestatuses.length;i++){
				rsrcInfo_builder = RsrcInfo.newBuilder();
				rsrcInfo_builder.setRsrccd(filestatuses[i].getRsrccd());
				rsrcInfo_builder.setRsrcmsg(filestatuses[i].getRsrccodename());
				setrsrcinfo.add(rsrcInfo_builder.build());
				fileDataList_builder.addFiledatas(filestatuses[i].toFileData());
				
				JobInfo.Builder jobinfo = JobInfo.newBuilder();
				jobinfo.setJobcd(filestatuses[i].getJobcd());
				jobinfo.setJobname(filestatuses[i].getJobname());	
				jobinfo_builder.addJobinfo(jobinfo);
			}
			
			rsrcInfoList_builder.addAllRsrcinfo(setrsrcinfo);
			
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(id);
			userinfo_builder.setPasswd(passwd);
			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
			
			
			sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
			sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);

			RequestInfo.Builder requestinfo_builder = RequestInfo.newBuilder();
			requestinfo_builder.setQrycd("01");
			requestinfo_builder.setSayu(sayu);

			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			
			if(!chkModify){
				SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
				srinfo_builder.setCcEditor(id);
				srinfo_builder.setCcSRId(srId);
				srinfo_builder.setCcTitle(srTitle);	
				builder_msg.setSrinfo(srinfo_builder.build());
			}
			
			builder_msg.setMsgtype("CHECKOUT");
			//builder_msg.setPrjinfolist(prjinfolist_builder.build());
			builder_msg.setRequestinfo(requestinfo_builder.build());
			builder_msg.setFiledatalist(fileDataList_builder.build());
			sysinfo_builder.setRsrcinfolist(rsrcInfoList_builder.build());
			builder_msg.setUserinfo(userinfo_builder.build());
			builder_msg.setSysinfo(sysinfo_builder.build());	
			builder_msg.setJobinfolist(jobinfo_builder.build());
			
			EcamsClient ecamsclient = new EcamsClient(ip,port);
			ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
			
			if (returnMsg.getReturnStr().startsWith("SOCKERR")){
				errcnt = 1;
				errMsg = "SOCKET ERROR";
				return Status.CANCEL_STATUS;
			}else {
				String acptno = returnMsg.getReturnStr();
				
				if (returnMsg.getReturnval() == 0){
					builder_msg = EcamsMessage.newBuilder();
					RequestInfo.Builder requestinfo =  RequestInfo.newBuilder();
					requestinfo.setQrycd("01");
		    		requestinfo.setAcptno(acptno);
					builder_msg.setRequestinfo(requestinfo.build());
					builder_msg.setMsgtype("SYNC_PROJECT_GETLIST");
					builder_msg.setUserinfo(userinfo_builder.build());
					builder_msg.setSysinfo(sysinfo_builder.build());	
					builder_msg.setJobinfolist(jobinfo_builder.build());
					returnMsg = ecamsclient.sendMsg(builder_msg.build());
					
					
					if (returnMsg.getReturnval() == 0){
						monitor.beginTask("P/"+project.getName()+" \uccb4\ud06c\uc544\uc6c3 \uc911..", filestatuses.length+5);
						

						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
						
						fileDataList_builder = FileDataList.newBuilder();
						for (j=0;j<returnMsg.getEcamsmsg().getFiledatalist().getFiledatasCount();j++){				
							
							FileData filedata = returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(j);
							
							monitor.subTask(filedata.getFilename());
							monitor.worked(1);	
							
			    			String projectPath = project.getLocation().toOSString();
			    			
			    			//String filepath = (projectPath+"/"+filedata.getRsrcinfo().getRsrcmsg()+"/"+filedata.getPathinfo().getRelativitePath());
			    			String filepath = (projectPath+"/"+filedata.getPathinfo().getRelativitePath());
			    			while(filepath.indexOf("/") >=0){
			    				filepath = filepath.replace("/","\\");
			    			}
			    			
			    			while(filepath.indexOf("\\\\") >=0){
			    				filepath = filepath.replace("\\\\", "\\");
			    			}
			
			    			while(filepath.indexOf("\\") >=0){
			    				filepath = filepath.replace("\\", "/");
			    			}	
			    			
			    			String filename = filepath+"/"+filedata.getFilename();
			    			
							IPath tmpPath = new Path(filename);
							IResource tmpResource = root.getFileForLocation(tmpPath);
							
							File filez = new File(filename);
							
							downFlag = false;
							mergeFlag = false;
							
							boolean binarySW = false;
							if (filedata.getRsrcinfo().getCminfo().substring(2,3).equals("0")){ //\uccb4\ud06c\uc544\uc6c3(\ubb34)\uc81c\uc678
								if (!filez.exists()){
									downFlag = true;
								}else{
									if (!CheckSum.MD5SumVal(filename).equals(filedata.getTstmd5Sum())) {
										downFlag = true;
										
										IEcamsStatus tmpStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(tmpResource);
										String mergeEnable = "Y";
										binarySW = false;
										if( "1".equals(tmpStatus.getRsrcinfo().substring(IS_BINARY, IS_BINARY+1)) ) {
											 mergeEnable = "N";
											 binarySW = true;
										}
										
										if(tmpStatus.isChanged()){
											if( ConfirmMergeMessageDlg.CONFIRM_INIT == iMergeDlgStatus ) {
												final String dlgfilename = filename;
												final String tmpMergeEnable = mergeEnable;
												
												Display.getDefault().syncExec(new Runnable() {
													@Override
													public void run() {
														// TODO Auto-generated method stub
														ConfirmMergeMessageDlg confirmMergeMessageDlg = new ConfirmMergeMessageDlg(new Shell(), dlgfilename, tmpMergeEnable);
														confirmMergeMessageDlg.open();

														if (confirmMergeMessageDlg.getReturnCode() != ConfirmMergeMessageDlg.OK) {
															iMergeDlgStatus = ConfirmMergeMessageDlg.CONFIRM_NO_ACTION;
														}else{
															iMergeDlgStatus = confirmMergeMessageDlg.getChkAllways();
														}
													}
												});
												if( ConfirmMergeMessageDlg.CONFIRM_NO_ACTION == iMergeDlgStatus ){
													downFlag = false;
													iMergeDlgStatus = ConfirmMergeMessageDlg.CONFIRM_INIT;
												} else if( ConfirmMergeMessageDlg.CONFIRM_REPLACE == iMergeDlgStatus ){
													iMergeDlgStatus = ConfirmMergeMessageDlg.CONFIRM_INIT;
												} else if( ConfirmMergeMessageDlg.CONFIRM_MERGE == iMergeDlgStatus ){
													mergeFlag = true;
													iMergeDlgStatus = ConfirmMergeMessageDlg.CONFIRM_INIT;
												}
											}
											if( ConfirmMergeMessageDlg.CONFIRM_NO_ACTION_ALL == iMergeDlgStatus ){
												downFlag = false;
											} else if( ConfirmMergeMessageDlg.CONFIRM_MERGE_ALL == iMergeDlgStatus ){
												mergeFlag = true;
											}
										}
									}
								}
							}
		
							/* 바이너리파일이 머지로 신청되면 로컬소스 그대로 두도록 */
							if ( downFlag && (!mergeFlag || !binarySW) ){
								if("I".equals(tool)){
//									commandmapParam.put(project.getName()+filedata.getPathinfo().getRelativitePath()+"/"+filedata.getFilename(), Integer.toString(filedata.getVersion())+"."+filedata.getTstver());
									commandmapParam.put(project.getName()+filedata.getPathinfo().getRelativitePath()+"/"+filedata.getFilename(), filedata.getViewver());
								}
								
								/* 바이너리파일이 아닐때만 병합가능하도록 */
								if( mergeFlag && !binarySW ){
									
									Thread.sleep(500);
									IEcamsStatus tmpStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(tmpResource);
									List syncLists = new ArrayList();

									monitor.beginTask("P/"+project.getName()+" \uc11c\ubc84\ub85c \ud30c\uc77c \uc804\uc1a1 \uc911..", filestatuses.length+2);
									
									
									byte[] mineFilebyte = null;
										
									try {
										FileChannel inChannel = new FileInputStream(filez).getChannel();
										int size = (int)inChannel.size();
										if(size<1){
											throw new IOException("Error");
										}
										mineFilebyte = EFileToByteArray.FileToByteArray(filez);
									} catch (IOException e) {
										errcnt = 1;
										errMsg = filedata.getFilename()+" \ud30c\uc77c \uc77d\uae30 \uc2e4\ud328(Size:0).";
										//return Status.CANCEL_STATUS;
										break;
									}
										
									FileData.Builder fileData_builder = FileData.newBuilder();
									
									fileData_builder.setFilename(filedata.getFilename());
									//fileData_builder.setVersion(tmpStatus.getLastVer());
									fileData_builder.setItemid(filedata.getItemid());
									fileData_builder.setFilebytes(ByteString.copyFrom(EGzip.getCompressedByte(mineFilebyte)));
								
									
									builder_msg = EcamsMessage.newBuilder();
									builder_msg.setMsgtype("GETMERGEFILE");
									builder_msg.setFiledata(fileData_builder.build());
									builder_msg.setUserinfo(userinfo_builder.build());
									
									ReturnMsg returnMsg2  = ecamsclient.sendMsg(builder_msg.build());
							
									if(returnMsg2.getReturnval()>0){
										errcnt = 1;
										errMsg = "CheckOut ERROR [GETMERGEFILE] : "+returnMsg2.getReturnStr();
										System.out.println(errMsg);
									}else{
										try {
											EFileCopy.fileCopy( filez, new File(filename + ".mine") );
										
											syncLists.addAll(returnMsg2.getEcamsmsg().getFiledatalist().getFiledatasList());
																					
											monitor.beginTask("P/"+project.getName()+" \uc11c\ubc84\ub85c\ubd80\ud130 \ud30c\uc77c \uc218\uc2e0 \uc911..", syncLists.size()+20);
											FileOutputStream fos;
											
											for(i=0;i<syncLists.size();i++){
												if (monitor.isCanceled()){
													return Status.CANCEL_STATUS;
												}    		
												
												FileData mergeFileData = (FileData) syncLists.get(i);
								    			monitor.subTask(mergeFileData.getFilename());
												monitor.worked(1);		
												
												File mergeFile = new File(filepath + "/" + mergeFileData.getFilename());
												
												fos = new FileOutputStream(mergeFile);
												
												FileChannel outChannel = fos.getChannel();
												java.nio.ByteBuffer buf = java.nio.ByteBuffer.wrap( EGzip.getDecompressedByte(returnMsg2.getEcamsmsg().getFiledatalist().getFiledatas(i).getFilebytes().toByteArray()) );
												outChannel.write( buf );
												
												outChannel.close();
												fos.close();
											}
											syncLists.clear();
											syncLists = null;
										} catch (FileNotFoundException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									tmpResource.getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
								} else {
									Thread.sleep(500);
					    			builder_msg = EcamsMessage.newBuilder();
						    		builder_msg.setMsgtype("GETFILE");
			
						    		builder_msg.setFiledata(filedata);
						    		builder_msg.setUserinfo(userinfo_builder.build());
						    		ReturnMsg returnMsg2 = ecamsclient.sendMsg(builder_msg.build());
						    					    		
								    try{			
						    			File nfolder = new File(filepath);
						    						
										if (!nfolder.exists()){
											nfolder.mkdirs();
										}
				
						    			filez = new File(filename);
						    			
						    			if (!filez.exists() && !filedata.getStatus().split(":")[1].equals("9")){
						    				filez.createNewFile();
						    			}
						    			else{
						    				if(filedata.getStatus().split(":")[1].equals("9")){
						    					if(filez.exists()) filez.delete();
						    				}else{
							    				//filez.delete();
							    				filez.createNewFile();
						    				}
						    			}
						    			if(filez.exists()){
											FileOutputStream fw = new FileOutputStream(filez);
											
											fw.write(EGzip.getDecompressedByte(returnMsg2.getEcamsmsg().getFiledata().getFilebytes().toByteArray()));
											
											if(fw.getChannel().size() <1){
												throw new IOException("FILE SIZE ERROR");
											}
											//fw.write(EGzip.getDecompressedByte(returnMsg.getEcamsmsg().getFiledata().getFilebytes().toByteArray()));
											
											fw.flush();
											fw.close();
											//rtFlag=true;
											
						    			}
								    } catch (IOException e) {
										errMsg = e.toString();
										errcnt = 1;
										errList.add(tmpResource);
										continue;
										// TODO Auto-generated catch block
										//rtFlag=false;
										//return Status.CANCEL_STATUS;
									}
									//tmpResource.refreshLocal(IResource.DEPTH_ONE, monitor);
								}
							}
							fileDataList_builder.addFiledatas(filedata);
							
							labelRefreshList.add(tmpResource);
							
							tmpPath = null;
							tmpResource = null;
						}
					}
				} else {
					errMsg = "\uccb4\ud06c\uc544\uc6c3 \uc2e4\ud328 ["+returnMsg.getReturnStr()+"]";
					errcnt = 1;
					//EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,""));
					EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,"","NONE"));
					return Status.CANCEL_STATUS;
				}

				if(errcnt > 0) {
					builder_msg = EcamsMessage.newBuilder();
					builder_msg.setMsgtype("REQUEST_ALLCNCL");
					RequestInfo.Builder request_builder = RequestInfo.newBuilder();
					request_builder.setQrycd("01");
					request_builder.setAcptno(acptno);
					builder_msg.setRequestinfo(request_builder.build());
					builder_msg.setUserinfo(userinfo_builder.build());
					returnMsg = ecamsclient.sendMsg(builder_msg.build());
					
					if(errList.size()==0){
						EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,acptno,"NONE"));
					}
				} else {
					builder_msg = EcamsMessage.newBuilder();
					builder_msg.setMsgtype("REQUEST_COMPLETE");
					
					RequestInfo.Builder request_builder = RequestInfo.newBuilder();
					request_builder.setQrycd("01");
					request_builder.setAcptno(acptno);
					builder_msg.setRequestinfo(request_builder.build());
					builder_msg.setUserinfo(userinfo_builder.build());
					returnMsg = ecamsclient.sendMsg(builder_msg.build());
					
					builder_msg = EcamsMessage.newBuilder();
					builder_msg.setRequestinfo(request_builder.build());
					builder_msg.setMsgtype("SYNC_PROJECT_GETLIST");
					builder_msg.setUserinfo(userinfo_builder.build());
					builder_msg.setSysinfo(sysinfo_builder.build());	
					builder_msg.setJobinfolist(jobinfo_builder.build());
					returnMsg = ecamsclient.sendMsg(builder_msg.build());
					
					if (returnMsg.getReturnval() == 0){
						IEcamsStatus filestatus = new EcamsResourceStatus();
						for (j=0;j<returnMsg.getEcamsmsg().getFiledatalist().getFiledatasCount();j++){
							FileData filedata = returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(j);
							filestatus = new EcamsResourceStatus();
							filestatus.setStatus(filedata, project);
							EcamsProviderPlugin.getPlugin().getXmlStatusMgr().new_updateStatus((EcamsResourceStatus)filestatus, project,"A");
							filestatus = null;
							filedata = null;
						}
					}
				}
			}
			
			IResource[] syncResoruces = (IResource[])labelRefreshList.toArray(new IResource[labelRefreshList.size()]);
			for(i=0;i<syncResoruces.length;i++){
				((IResource) syncResoruces[i]).refreshLocal(IResource.DEPTH_ONE, monitor);
			}
			syncResoruces = null;
			
			return Status.OK_STATUS;
		}
		catch (CoreException e){
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}
		finally {
			if("I".equals(tool) && errcnt == 0){
				if(commandmapParam.size()>0){
					/*
					 * project checkout complete process
					 * command.team.DoneCheckInOut
					 */
					try{
						HashMap<String, Map<String, String>> parameter = new HashMap<String, Map<String, String>>();
						parameter.put(project.getName(), commandmapParam);
						CommandExecuter.executeCommand("command.team.DoneCheckInOut", parameter);
						parameter = null;
					}catch(Exception e){
						e.printStackTrace();
					}
					
					commandmapParam = null;
				}
			}
			monitor.done();
		}
	}
	
	final class CheckOutJobChangeListener extends JobChangeAdapter {

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
							messageBox.setText("CheckOut ERROR");
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
						ErrorMessageDlg errDlg = new ErrorMessageDlg(new Shell(),(IResource[])errList.toArray(new IResource[errList.size()]),"\uccb4\ud06c\uc544\uc6c3\uc624\ub958","\ub2e4\uc74c \ud30c\uc77c\ub4e4\uc740 \uccb4\ud06c\uc544\uc6c3\uc911 \uc624\ub958\uac00 \ubc1c\uc0dd\ud558\uc5ec \uc790\ub3d9\ubc18\ub824 \ub418\uc5c8\uc73c\ub2c8 \ub2e4\uc2dc \uccb4\ud06c\uc544\uc6c3 \ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.\n"+errMsg);
						errDlg.open();	
						errList.clear();
						errList = null;					
					}
				});
			} else if (labelRefreshList.size()>0){
				EcamsProviderPlugin.broadcastModificationStateChanges((IResource[]) labelRefreshList.toArray(new IResource[labelRefreshList.size()]));
			}
		}
	}

}
