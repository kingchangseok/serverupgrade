package com.azsoft.ecams.core.jobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


import org.apache.commons.lang.StringUtils;
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
import com.azsoft.ecams.proto.ProtoEcams.PathInfo;
import com.azsoft.ecams.proto.ProtoEcams.RequestInfo;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.dialog.ConfirmMessage4Dlg;
import com.azsoft.ecams.ui.dialog.ErrorMessageDlg;
import com.azsoft.ecams.ui.dialog.SyncListDlg;
import com.azsoft.ecams.ui.wizard.SyncListWizard;
import com.azsoft.ecams.util.checksum.CheckSum;
import com.azsoft.ecams.util.file.EGzip;
import com.ibm.icu.text.SimpleDateFormat;

public class SyncJob extends WorkspaceJob {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private List errList = new ArrayList();
	private List updatesTmp = new ArrayList();
	private List overwriteList = new ArrayList();
	private List syncResource = new ArrayList();
	private List labelRefreshList = new ArrayList();
	public List rsrcList = new ArrayList();
	public List jobList = new ArrayList();
	
	private File iMetaFile = null;
	
	private IProject project = null;
	private IResource[] resources = null;
	private String acptno = "";
	private String errMsg = ""; 
	private String GBN = "NONE";
	private String reqCd = "ALL";
	private FileDataList.Builder filedatalist_builder = FileDataList.newBuilder();
	
	public SyncJob(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	public SyncJob(String name, IResource[] resources,String acptNo, String GBN) {
		// TODO Auto-generated constructor stub
		super(name);
		this.resources = resources;
		this.acptno = acptNo;
		this.GBN = GBN;
		addJobChangeListener(new SyncJobChangeListener());
	}
	
	public IStatus runInWorkspace(IProgressMonitor monitor){
		//IProject myproject=null;
		IResource tmpResource = null;
		List syncLists = new ArrayList();
		List projectList = new ArrayList();
		int i,j = 0;

		String tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
		
		final Map<String, String> commandmapParam = new HashMap<String, String>();
		Map<String, String> overwritemapParam = new HashMap<String, String>();
		
		try {

			if(resources == null){
				return Status.CANCEL_STATUS;
			}
			
			project = ((IResource[])resources)[0].getProject();
			
			//System.out.println("2"+project);
			
			if(project == null){
				errMsg = "Project is null";
				return Status.CANCEL_STATUS;
				//throw new Exception("Project is null");
			}
			
			if(!project.isAccessible()) {
				errMsg = "Project is closed";
				return Status.CANCEL_STATUS;
			}
			
			if(!EcamsRepositoryProvider.isManagedByEcams(project)){
				errMsg = "Is Not eCAMS Project.[ Connection Project -> Properties eCAMS PlugIn]";
				return Status.CANCEL_STATUS;
			}
			
			
			errList.clear();
			updatesTmp.clear();
			
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
			
			EcamsClient syncClient = null;
			EcamsMessage.Builder builder_msg = null;
			ReturnMsg returnMsg = null;
			
			//if(null == project.getPersistentProperty(new QualifiedName("Properties","syscd")) || project.getPersistentProperty(new QualifiedName("Properties","syscd")).equals("")){
			if(!"true".equals(project.getPersistentProperty(new QualifiedName("Properties","useyn")))){
				errMsg = "properties\uc5d0\uc11c \uc2dc\uc2a4\ud15c\uacfc \uc5c5\ubb34\ub97c \uc5f0\uacb0\ud574\uc8fc\uc2ed\uc2dc\uc624.";
				return Status.CANCEL_STATUS;
			}
			
			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
    		sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
    		sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);
    		/*
    		JobInfoList.Builder jobinfo_builder = JobInfoList.newBuilder();
    		JobInfo.Builder job_builder = JobInfo.newBuilder();
    		job_builder.setJobcd(project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[0]);
    		job_builder.setJobname(project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[1]);
    		jobinfo_builder.addJobinfo(job_builder.build());
    		*/
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(id);
			userinfo_builder.setPasswd(passwd);
			
			RequestInfo.Builder requestinfo =  RequestInfo.newBuilder();
			requestinfo.setQrycd("04");

			if (project != null){
				builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("JOBLIST_GET");
				
				builder_msg.setSysinfo(sysinfo_builder.build());
				builder_msg.setUserinfo(userinfo_builder.build());				
				
				syncClient = new EcamsClient(ip,port);
				returnMsg = syncClient.sendMsg(builder_msg.build());
				
				if (returnMsg.getReturnStr().startsWith("SOCKERR")){
					errMsg = "SOCKET ERROR";
					return Status.CANCEL_STATUS;
				}else{
					if(returnMsg.getReturnval() > 0){
						errMsg = "JOBLIST_GET ERROR";
						return Status.CANCEL_STATUS;
					}else{
						jobList.clear();
						rsrcList.clear();
						for (i=0;i<returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfoCount();i++){
							rsrcList.add(returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getRsrccd()+":"+returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getRsrcmsg()+":"+returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getExename());
						}

						for(i=0;i<returnMsg.getEcamsmsg().getJobinfolist().getJobinfoCount();i++){
							jobList.add(returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getJobcd()+":"+returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getJobname()+":"+returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getDeptcd());
						}
						
						project.setPersistentProperty(new QualifiedName("Properties","setrsrccd"), StringUtils.join(rsrcList.toArray(),"/"));
						project.setPersistentProperty(new QualifiedName("Properties","setjobcd"), StringUtils.join(jobList.toArray(),"/"));
						
						//System.out.println(">>>>>>>>>>>>jobList.toArray():\n"+StringUtils.join(jobList.toArray(),"/"));
						//projectList.add(project);
						
						projectList.add((IResource)project);
						EcamsProviderPlugin.broadcastModificationStateChanges((IResource[]) projectList.toArray(new IResource[projectList.size()]));
						projectList.clear();
						projectList = null;
					}
				}
			}
			builder_msg = null;
			syncClient = null;
			returnMsg = null;
			
			projectList = new ArrayList();
			for (i=0;i<resources.length;i++){
				if (resources[i].getType() == IResource.PROJECT && EcamsProviderPlugin.getPlugin().isManagedByEcams((IProject)resources[i])){
					projectList.add(resources[i]);
					//break;
				}
			}
			
			if(projectList.size()>1){
				errMsg = "Please Select One Project.";
				return Status.CANCEL_STATUS;
			}
			boolean sameProjectFlag = false;
			for (i=0;i<resources.length;i++){
				if (resources[i].getType() != IResource.PROJECT){
					sameProjectFlag = false;
					for(j=0;j<projectList.size();j++){
						if (resources[i].getProject() == projectList.get(j)){
							sameProjectFlag = true;
							break;
						}
					}
					if (!sameProjectFlag){
						if (resources[i].getType() != IResource.FOLDER){
							for (j=0;syncResource.size()>j;j++) {
								tmpResource = (IResource)syncResource.get(j);
								if(tmpResource.getType()==IResource.FOLDER){
									if (resources[i].getParent().getFullPath().equals(tmpResource.getFullPath())) {
										syncResource.remove(j);
										break;
									}
								}
							}
						}
						syncResource.add(resources[i]);
					}
				}						
			}
			if(acptno != null && !acptno.equals("")){
				reqCd = "SEL";
				builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("SYNC_PROJECT_GETLIST");
				
	    		requestinfo.setAcptno(acptno);
	    		
				builder_msg.setSysinfo(sysinfo_builder.build());
	    		builder_msg.setRequestinfo(requestinfo.build());
	    		builder_msg.setUserinfo(userinfo_builder.build());							
				//builder_msg.setJobinfolist(jobinfo_builder.build());
				syncClient = new EcamsClient(ip,port);
				returnMsg = syncClient.sendMsg(builder_msg.build());
				
	    		if(returnMsg.getReturnval() == 0){
	    			syncLists.addAll(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList());
	    		}
			}else{
				if (projectList.size() > 0){
					reqCd = "ALL";
					builder_msg = EcamsMessage.newBuilder();
					builder_msg.setMsgtype("SYNC_PROJECT_GETLIST");
					
		    		requestinfo.setAcptno("");
		    		
					builder_msg.setSysinfo(sysinfo_builder.build());	
		    		builder_msg.setRequestinfo(requestinfo.build());
		    		builder_msg.setUserinfo(userinfo_builder.build());						
					//builder_msg.setJobinfolist(jobinfo_builder.build());
					syncClient = new EcamsClient(ip,port);
					returnMsg = syncClient.sendMsg(builder_msg.build());
					
		    		if(returnMsg.getReturnval() == 0){
		    			syncLists.addAll(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList());
		    		}
				}else{
					for(i=0;i<syncResource.size();i++){
						if (monitor.isCanceled()){
							return Status.CANCEL_STATUS;
						}		
						//((IResource) syncResource.get(i)).getName().equals(".ecm") 
						//((IResource) syncResource.get(i)).getName().substring(0,1).equals(".") 
		    			if (((IResource) syncResource.get(i)).getParent().getName().equals(".deco") 
								|| ((IResource) syncResource.get(i)).getParent().getName().equals(".setting")
								|| ((IResource) syncResource.get(i)).getParent().getName().equals(".back")
								){
							continue;
		    			}
						builder_msg = EcamsMessage.newBuilder();
						builder_msg.setMsgtype("SYNC_PROJECT_GETLIST");
						
			    		requestinfo.setAcptno("");
			    		
						builder_msg.setUserinfo(userinfo_builder.build());
						
			    		if (((IResource) syncResource.get(i)).getType() == IResource.FOLDER){
							reqCd = "ALL";
				    		PathInfo.Builder pathInfo_builder = PathInfo.newBuilder();
				    		
				    		String prjname = project.getName();
				    		String relativePath = ((IResource)syncResource.get(i)).getProjectRelativePath().toString();
				    		
				    		if(relativePath.indexOf(".istudiometa")>-1){
				    			//pathInfo_builder.setRelativitePath(relativePath.substring(relativePath.indexOf(prjname)+prjname.length()+1));
				    			if (relativePath.indexOf(prjname) > -1) {
				    				pathInfo_builder.setRelativitePath(relativePath.substring(relativePath.indexOf(prjname)+prjname.length()+1));
				    			} else {
				    				pathInfo_builder.setRelativitePath(relativePath);
				    			}
				    		}else{
				    			pathInfo_builder.setRelativitePath(relativePath);
				    		}
				    		builder_msg.setPathinfo(pathInfo_builder.build());
			    		}else{
							reqCd = "SEL";
			    			FileData.Builder filedata_builder = FileData.newBuilder();
		    				filedata_builder.setFilename(((IResource) syncResource.get(i)).getName());
		    				PathInfo.Builder pathInfo_builder = PathInfo.newBuilder();
		    				pathInfo_builder.setRelativitePath(((IResource)syncResource.get(i)).getParent().getProjectRelativePath().toString());
		    				filedata_builder.setPathinfo(pathInfo_builder.build());
		    				builder_msg.setFiledata(filedata_builder.build());
			    		}
			    		
						//builder_msg.setJobinfolist(jobinfo_builder.build());
			    		builder_msg.setSysinfo(sysinfo_builder.build());
			    		builder_msg.setRequestinfo(requestinfo.build());
						syncClient = new EcamsClient(ip,port);
			    		returnMsg = syncClient.sendMsg(builder_msg.build());
			    		
			    		if(returnMsg.getReturnval() == 0){
			    			syncLists.addAll(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList());
			    		}
					}
				}
			}
			projectList = null;
			builder_msg = null;
			syncClient = null;
			returnMsg = null;
			
			String projectPath = "";
			String filepath = "";
			String filename = "";
			String decofilename = "";
			
			File filez = null;
			File decofile = null;
			File nfolder = null;

			monitor.beginTask("P/"+project.getName()+" Synchronizing..", syncLists.size()+20);
			
			FileData filedata = null;
			FileOutputStream fw = null;
			IEcamsStatus filestatus = null;

			boolean downFlag = false;

			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			String currentDate = formatter.format(new Date());
			
			IPath workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			
			String iMetaStr = workspacePath.uptoSegment(workspacePath.segmentCount() - 1).toString() + "/meta/" + project.getName();
			File iMetaFolder = new File(iMetaStr);
			if(!iMetaFolder.exists()){
				iMetaFolder.mkdirs(); 
			}
			iMetaStr = iMetaStr + "/" + currentDate + ".sync";
			
			iMetaFile = new File(iMetaStr);
			if(!iMetaFile.exists()){
				iMetaFile.createNewFile();
			}
			currentDate = null;
			formatter = null;
			formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			currentDate = formatter.format(new Date());
			
			FileOutputStream fos = new FileOutputStream(iMetaFile, true);
			fos.write(("\n#" + currentDate + "#").getBytes());
			fos.flush();
			
			currentDate = null;
			formatter = null;
			
			for(i=0;i<syncLists.size();i++){
				
				if (monitor.isCanceled()){
					return Status.CANCEL_STATUS;
				}    		
				
    			filedata = (FileData) syncLists.get(i);

    			projectPath = project.getLocation().toString();
    			
    			filepath = (projectPath+"/"+filedata.getPathinfo().getRelativitePath());
    			
    			while(filepath.indexOf("/") >=0){
    				filepath = filepath.replace("/","\\");
    			}
    			
    			while(filepath.indexOf("\\\\") >=0){
    				filepath = filepath.replace("\\\\", "\\");
    			}

    			while(filepath.indexOf("\\") >=0){
    				filepath = filepath.replace("\\", "/");
    			}	
    			
    			filename = filepath+"/"+filedata.getFilename();
    			decofilename = filepath+"/"+".deco/."+filedata.getFilename()+".ecm-meta";
    			
				monitor.subTask(filename);
				monitor.worked(1);		
				
				filez = new File(filename);
				nfolder = new File(filepath);

				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IPath tmpPath = new Path(filename);
				
				tmpResource = null;
				tmpResource = root.getFileForLocation(tmpPath);
				
				filestatus = new EcamsResourceStatus();
				filestatus.setStatus(filedata, project);

				downFlag = false;
				
				if(filedata.getStatus().split(":")[1].equals("9")) {
					downFlag = false;
					
					if(filez.exists()){
						//메타_iStudio 파일은 지우지않고 command호출되도록 수정 (command실행 후 istudio에서 삭제하도록 함)
						if(!"02".equals(filedata.getRsrcinfo().getRsrccd())){
							filez.delete();
							filez = null;
						}else{
							if("I".equals(tool)){
								commandmapParam.put(project.getName()+filedata.getPathinfo().getRelativitePath()+"/"+filedata.getFilename(), "");
								
								try{
									fos.write(("\n" + project.getName()+filedata.getPathinfo().getRelativitePath()+"/"+filedata.getFilename()+",-1").getBytes());
									fos.flush();
								}catch (FileNotFoundException ffe){
									ffe.printStackTrace();
								}catch (IOException ioe){
									ioe.printStackTrace();
								}
							}
						}
					}
					
					decofile = new File(decofilename);
					
					if(decofile.exists()){
						decofile.delete();
					}
					decofile = null;
				} else if(GBN.equals("DOWN")) {
					downFlag = true;
				} else {
					if (filedata.getRsrcinfo().getCminfo().substring(2,3).equals("0")){ // && !filez.exists()
						if(GBN.equals("GET")){
							if(filedata.getVersion() > 0){
								downFlag = true;
								downFlag = true;
								if("I".equals(tool)){
									overwritemapParam.put(project.getName()+filedata.getPathinfo().getRelativitePath()+"/"+filedata.getFilename(), filedata.getViewver());
								}
							}
						}else{
							if(filedata.getVersion() > 0){
								if (!filez.exists()){
									downFlag = true;
								}else{
									if(!filedata.getFilename().equals(".project") && !filedata.getFilename().equals(".classpath")){
										if(!CheckSum.MD5SumVal(filename).equals(filedata.getMd5Sum())){
											if(!filedata.getEditor().split(":")[1].equals(id)){
												downFlag = true; 
											}else{
												if(!filedata.getStatus().split(":")[1].equals("5") && !filedata.getStatus().split(":")[1].equals("8")
														&& !filedata.getStatus().split(":")[1].equals("B") && !filedata.getStatus().split(":")[1].equals("A")       // 체크인완료, 체크인요청
														&& !filedata.getStatus().split(":")[1].equals("7") && !filedata.getStatus().split(":")[1].equals("F")		// 운영적용요청,  테스트적용요청
														&& !filedata.getStatus().split(":")[1].equals("G")){
													downFlag = true;
												}
											}
										}
										
										if(downFlag && GBN.equals("NONE")){
											boolean qaFlag = false;
											
											if(null == acptno || "".equals(acptno)){
												IEcamsStatus tmpStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(tmpResource);
												//IEcamsStatus tmpStatus = EcamsProviderPlugin.getPlugin().getDBStatusMgr().getStatus(tmpResource);
												
												if(tmpStatus != null){
													if(!tmpStatus.isChanged()){
														qaFlag = true;
													}
												}
												tmpStatus = null;
											}
											
											if(!qaFlag){
												downFlag = false;
												if(!((filedata.getStatus().split(":")[1].equals("5") || filedata.getStatus().split(":")[1].equals("8"))
														&& filedata.getEditor().split(":")[1].equals(id))){
													overwriteList.add(tmpResource);
													//filedatalist_builder.addFiledatas(filedata);
													//if(EcamsProviderPlugin.getPlugin().getXmlStatusMgr().label_updateStatus((EcamsResourceStatus)filestatus, project)){
													if(EcamsProviderPlugin.getPlugin().getXmlStatusMgr().new_updateStatus((EcamsResourceStatus)filestatus, project,"N")){
													//if(EcamsProviderPlugin.getPlugin().getDBStatusMgr().new_updateStatus((EcamsResourceStatus)filestatus, project,"N")){
														labelRefreshList.add(tmpResource);
													}
													
													filestatus = null;
													returnMsg = null;
													filedata = null;
													nfolder = null;
										    		filez = null;
													
													syncLists.remove(i);
													i--;
													
													continue;
												}
											}
										}
									}
								}
							}
						}
					}
				}
				
				if (downFlag){
					//Thread.sleep(120);
					
					if("I".equals(tool)){
						if(!GBN.equals("GET")){
							commandmapParam.put(project.getName()+filedata.getPathinfo().getRelativitePath()+"/"+filedata.getFilename(), filedata.getViewver());
							
							try{
								fos.write(("\n" + project.getName()+filedata.getPathinfo().getRelativitePath()+"/"+filedata.getFilename()+","+filedata.getViewver()).getBytes());
								fos.flush();
							}catch (FileNotFoundException ffe){
								ffe.printStackTrace();
							}catch (IOException ioe){
								ioe.printStackTrace();
							}
						}
					}
					
					builder_msg = EcamsMessage.newBuilder();
			    	builder_msg.setMsgtype("GETFILE");
		    		builder_msg.setUserinfo(userinfo_builder.build());
		    		builder_msg.setFiledata(filedata);
		    		
					syncClient = new EcamsClient(ip,port);
		    		returnMsg = syncClient.sendMsg(builder_msg.build());
		    		builder_msg = null;
		    		syncClient = null;
		    		
		    		if (returnMsg.getReturnval() > 0){
		    			errList.add(tmpResource);
		    			continue;
		    		}

					if (!nfolder.exists()){
						nfolder.mkdirs();
					}
					if (!nfolder.exists()){
						errList.add(tmpResource);
		    			continue;
					}					
					filez.createNewFile();

		    		try{
						fw = new FileOutputStream(filez);
						fw.write(EGzip.getDecompressedByte(returnMsg.getEcamsmsg().getFiledata().getFilebytes().toByteArray()));
						fw.flush();
						fw.close();
				    } catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						errMsg = e.getCause()+"\n"+e.getMessage()+"\n"+"\ud30c\uc77c\uc0dd\uc131\uc5d0 \uc2e4\ud328\ud558\uc600\uc2b5\ub2c8\ub2e4.";
						errList.add(tmpResource);
						continue;
						//return Status.CANCEL_STATUS;
					}
					fw = null;
				} else {
					if("I".equals(tool)){
						if(!GBN.equals("GET")){
							if(!filedata.getStatus().split(":")[1].equals("9")) {
								String retStr = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getChanged((EcamsResourceStatus)filestatus, project);
								if(null != retStr){
									commandmapParam.put(project.getName()+filedata.getPathinfo().getRelativitePath()+"/"+filedata.getFilename(), filedata.getViewver());
									try{
										fos.write(("\n" + project.getName()+filedata.getPathinfo().getRelativitePath()+"/"+filedata.getFilename()+","+filedata.getViewver()).getBytes());
										fos.flush();
									}catch (FileNotFoundException ffe){
										ffe.printStackTrace();
									}catch (IOException ioe){
										ioe.printStackTrace();
									}
								}
							}
						}
					}
				}
				
				if ( (null != filez) && filez.exists() ){ //filedata.getRsrcinfo().getCminfo().substring(2,3).equals("1") || ( (null != filez) && 
					if(EcamsProviderPlugin.getPlugin().getXmlStatusMgr().new_updateStatus((EcamsResourceStatus)filestatus, project,"A")){
					//if(EcamsProviderPlugin.getPlugin().getDBStatusMgr().new_updateStatus((EcamsResourceStatus)filestatus, project,"A")){
						labelRefreshList.add(tmpResource);
					} else {
						errList.add(tmpResource);
					}
				}
				
				filestatus = null;
				returnMsg = null;
				filedata = null;
				nfolder = null;
	    		filez = null;
				
				syncLists.remove(i);
				i--;
    		}
			
			iMetaStr = null;
			if(fos != null) try { fos.close(); }catch (Exception e){}
    		
			syncLists.clear();
			syncLists = null;
			
			try{
	    		if(reqCd.equals("ALL")){
	    			ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName()).refreshLocal(IResource.DEPTH_INFINITE, monitor);
	    		}else{
	    			for(i=0;i<syncResource.size();i++){
						if (monitor.isCanceled()){
							return Status.CANCEL_STATUS;
						}
	
			    		if (((IResource) syncResource.get(i)).getType() == IResource.FOLDER){
			    			((IResource) syncResource.get(i)).refreshLocal(IResource.FOLDER, monitor);
			    		}else{
			    			((IResource) syncResource.get(i)).refreshLocal(IResource.DEPTH_ONE, monitor);
			    		}
					}	
	    		}
			}catch(Exception e){
				e.printStackTrace();
			}
			syncResource = null;
			
			
			return Status.OK_STATUS;
			
		}catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			errMsg = e.getCause().toString() + "\n" + e.getMessage().toString();
			return Status.CANCEL_STATUS;
		}catch (NullPointerException e){
			e.printStackTrace();
			errMsg = e.getCause().toString() + "\n" + e.getMessage().toString();
			return Status.CANCEL_STATUS;
		}catch (Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			errMsg = e.getCause().toString() + "\n" + e.getMessage().toString();
			return Status.CANCEL_STATUS;
		}finally{
			if("I".equals(tool)){
	    		Map<String, Map<String, String>> parameter = new HashMap<String, Map<String, String>>();
				/*Map<String, String> mapParam = new HashMap<String, String>();
				
				if(GBN.equals("NEW")){
					try{
						mapParam = new HashMap<String, String>();
						mapParam.put("PROJECT_NAME", project.getName());
						CommandExecuter.executeCommand("command.team.BeforeProjectCheckOut", mapParam);
						mapParam = null;
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				*/
				if(overwritemapParam.size()>0){
					monitor.subTask("command.team.DoneCheckInOutOverwrite");
					monitor.worked(1);	
					/*
					 * project overwrite complete process
					 * command.team.DoneCheckInOutOverwrite
					 */
					try{
						parameter = new HashMap<String, Map<String, String>>();
						parameter.put(project.getName(), overwritemapParam);
						CommandExecuter.executeCommand("command.team.DoneCheckInOutOverwrite", parameter);
						parameter = null;
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				/*
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageBox messageBox = new MessageBox(new Shell());
						messageBox.setMessage("command.team.DoneCheckInOut");
						messageBox.setText("size: "+commandmapParam.size());
						messageBox.open();
					}
				});
				*/
				if(commandmapParam.size()>0){
					monitor.subTask("command.team.DoneCheckInOut");
					monitor.worked(1);	
					/*
					 * project checkout complete process
					 * command.team.DoneCheckInOut
					 */
					try{
						parameter = new HashMap<String, Map<String, String>>();
						parameter.put(project.getName(), commandmapParam);
						CommandExecuter.executeCommand("command.team.DoneCheckInOut", parameter);
						parameter = null;
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				
				/*
				if(GBN.equals("NEW")){
					try{
						mapParam = new HashMap<String, String>();
						mapParam.put("PROJECT_NAME", project.getName());
						CommandExecuter.executeCommand("command.team.DoneProjectCheckOut", mapParam);
						mapParam = null;
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				*/
			}

			overwritemapParam = null;
			syncLists = null;
			tmpResource = null;
			
			monitor.done();
		}
	}
	
	final class SyncJobChangeListener extends JobChangeAdapter {
		public void done(IJobChangeEvent event) {
			IStatus result = event.getResult();
			if(result.getSeverity() == IStatus.CANCEL && errList.size() == 0) {
				if(!"".equals(errMsg)){
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageBox messageBox = new MessageBox(new Shell());
							messageBox.setMessage(errMsg);
							messageBox.setText("Sync ERROR");
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
						ErrorMessageDlg errDlg = null;
						if(errMsg != null && !errMsg.equals("")){
							errDlg = new ErrorMessageDlg(new Shell(),(IResource[])errList.toArray(new IResource[errList.size()]),"\ub3d9\uae30\ud654\uc624\ub958", errMsg);
						}else{
							errDlg = new ErrorMessageDlg(new Shell(),(IResource[])errList.toArray(new IResource[errList.size()]),"\ub3d9\uae30\ud654\uc624\ub958","\ub2e4\uc74c \ud30c\uc77c\ub4e4\uc740 \ub3d9\uae30\ud654\uc911 \uc624\ub958\uac00 \ubc1c\uc0dd\ud558\uc600\uc2b5\ub2c8\ub2e4.");
						}
						errDlg.open();
					}
				});	
				errList.clear();
				errList = null;
			}
			
			EcamsProviderPlugin.broadcastModificationStateChanges((IResource[])labelRefreshList.toArray(new IResource[labelRefreshList.size()]));
			labelRefreshList.clear();
			labelRefreshList = null;
			
			if(overwriteList.size()>0){
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						SyncListWizard wizard = new SyncListWizard((IResource[]) overwriteList.toArray(new IResource[overwriteList.size()]), project);
						SyncListDlg syncListDlg = new SyncListDlg(new Shell(),wizard);
						wizard.setParentDialog(syncListDlg);
						syncListDlg.open();
					}
				});
				
				project = null;
				overwriteList.clear();
				overwriteList = null;
			}
		}
	}
}
