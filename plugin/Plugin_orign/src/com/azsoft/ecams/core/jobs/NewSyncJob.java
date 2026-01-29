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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
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
import com.azsoft.ecams.core.resource.EcamsResourceStatus;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.JobInfo;
import com.azsoft.ecams.proto.ProtoEcams.JobInfoList;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.dialog.ErrorMessageDlg;
import com.azsoft.ecams.util.file.EGzip;
import com.ibm.icu.text.SimpleDateFormat;

public class NewSyncJob extends WorkspaceJob {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private List errList = new ArrayList();
	private List updatesTmp = new ArrayList();
	public List rsrcList = new ArrayList();
	public List jobList = new ArrayList();
	private IProject project = null;
	private IResource[] resources = null;
	private String errMsg = ""; 
	private String reqCd = "ALL";
	private String downDlgStr = "0";
	
	private File iMetaFile = null;
	
	public NewSyncJob(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	public NewSyncJob(String name, IResource[] resources) {
		// TODO Auto-generated constructor stub
		super(name);
		this.resources = resources;
		addJobChangeListener(new SyncJobChangeListener());
	}
	
	public IStatus runInWorkspace(IProgressMonitor monitor){
		//IProject myproject=null;
		IResource tmpResource = null;
		int i = 0;
		
		String tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
		
		Map<String, String> commandmapParam = new HashMap<String, String>();
		
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
			
			EcamsClient syncClient = new EcamsClient(ip,port);
			
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

			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			ReturnMsg returnMsg = null;
			if (project != null){
				builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("JOBLIST_GET");
				
				builder_msg.setSysinfo(sysinfo_builder.build());
				builder_msg.setUserinfo(userinfo_builder.build());				
				
				syncClient = new EcamsClient(ip,port);
				returnMsg = syncClient.sendMsg(builder_msg.build());
				builder_msg = null;
				
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
						
						List<IResource> projectList = new ArrayList<IResource>();
						projectList.add((IResource)project);
						EcamsProviderPlugin.broadcastModificationStateChanges((IResource[]) projectList.toArray(new IResource[projectList.size()]));
						projectList.clear();
						projectList = null;
					}
				}
				returnMsg = null;
			}
			
			builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("SYNC_PROJECT_GETCNT");
			builder_msg.setSysinfo(sysinfo_builder.build());							
			//builder_msg.setJobinfolist(jobinfo_builder.build());
    		builder_msg.setUserinfo(userinfo_builder.build());	
    		returnMsg = syncClient.sendMsg(builder_msg.build());
    		builder_msg = null;
    		
    		if(returnMsg.getReturnStr().startsWith("SOCKERR") || returnMsg.getReturnval()>0){
    			return Status.CANCEL_STATUS;
    		}
			
    		int totCnt = returnMsg.getEcamsmsg().getTotpage();
    		returnMsg = null;
    		
    		int limit = 50;
    		int totpage = 0;
    		
    		if ((totCnt%limit) == 0) {
				totpage =  (totCnt/limit);
			} else {
				totpage =  (totCnt/limit)+1;
			}

    		int start = 1;
    		int end = 50;
    		if(totpage==1){
    			end = totCnt;
    		}
    		
			//System.out.println(">>>>>>totpage:"+totpage);
			monitor.beginTask("P/"+project.getName()+" Synchronizing..", totCnt+totpage+50);

			builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("SYNC_PROJECT_NEW_GETLIST");
			builder_msg.setSysinfo(sysinfo_builder.build());							
			//builder_msg.setJobinfolist(jobinfo_builder.build());
    		builder_msg.setUserinfo(userinfo_builder.build());	


			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			FileData filedata = null;
			IEcamsStatus filestatus = null;
    		
			String projectPath = project.getLocation().toString();
			String filepath = "";
			File filez = null;
			File nfolder = null;
			IPath tmpPath = null;
			
			List syncLists = new ArrayList();
			
    		for(int page=0; page<totpage; page++){
				if (monitor.isCanceled()){
					syncLists.clear();
					syncLists = null;
		    		syncClient = null;
		    		returnMsg = null;
		    		builder_msg = null;
					return Status.CANCEL_STATUS;
				}

				monitor.subTask("\ubaa9\ub85d \uac00\uc838\uc624\ub294 \uc911..");
				monitor.worked(1);
				
	    		builder_msg.setTotpage(start);//start
	    		builder_msg.setPagenum(end);//end
	    		
	    		start = end + 1;
	    		end = end + 50;
	    		
				returnMsg = syncClient.sendMsg(builder_msg.build());
				
				if(returnMsg.getReturnStr().startsWith("SOCKERR")){
					errMsg = "SOCKET ERROR";
					syncLists.clear();
					syncLists = null;
					return Status.CANCEL_STATUS;
				}else if(returnMsg.getReturnval() == 0){
					syncLists = null;
					syncLists = new ArrayList();
	    			syncLists.addAll(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList());
		    		returnMsg = null;
		    		
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
	    					tmpResource = null;
	    					nfolder = null;
	    		    		filez = null;
	    					syncLists.clear();
	    					syncLists = null;
	    					return Status.CANCEL_STATUS;
	    				}
	        			
	        			filedata = (FileData) syncLists.get(i);

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
	    				
	        			if("I".equals(tool)){
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
	        			
	    				monitor.subTask(filepath+"/"+filedata.getFilename());
	    				monitor.worked(1);
	    				
	    				filez = new File(filepath+"/"+filedata.getFilename());
	    				nfolder = new File(filepath);

	    				tmpPath = new Path(filepath+"/"+filedata.getFilename());
	    				
	    				tmpResource = null;
	    				tmpResource = root.getFileForLocation(tmpPath);
	    				tmpPath = null;
	    				
	    				if((!filedata.getStatus().split(":")[1].equals("9") && !filedata.getStatus().split(":")[1].equals("3")) 
	    						&& filedata.getRsrcinfo().getCminfo().substring(2,3).equals("0") && filedata.getVersion() > 0){
	    					
	    		    		if (null == filedata.getFilebytes()){
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

	    					FileOutputStream fw = new FileOutputStream(filez);
	    		    		try{
	    						fw.write(EGzip.getDecompressedByte(filedata.getFilebytes().toByteArray()));
	    						fw.flush();
	    				    } catch (IOException e) {
	    						// TODO Auto-generated catch block
	    						e.printStackTrace();
	    						errMsg = e.getCause()+"\n"+e.getMessage()+"\n"+"\ud30c\uc77c\uc0dd\uc131\uc5d0 \uc2e4\ud328\ud558\uc600\uc2b5\ub2c8\ub2e4.";
	    						errList.add(tmpResource);
	    						//return Status.CANCEL_STATUS;
	    					} finally {
	    						fw.close();
	    						fw = null;
	    					}
	    				}
	    				
	    				if (filedata.getRsrcinfo().getCminfo().substring(2,3).equals("1") || filez.exists()){
	    					filestatus = new EcamsResourceStatus();
	    					filestatus.setStatus(filedata, project);
	    					
	    					if(!EcamsProviderPlugin.getPlugin().getXmlStatusMgr().new_updateStatus((EcamsResourceStatus)filestatus, project,"A")){
	    						errList.add(tmpResource);
	    					}
	    					filestatus = null;
	    				}
	    				syncLists.remove(i);
	    				i--;

	    				tmpResource = null;
	    				nfolder = null;
	    	    		filez = null;
	    	    		filedata = null;
	        		}
	    			iMetaStr = null;
	    			if(fos != null) try { fos.close(); }catch (Exception e){}
	    			
	    			syncLists.clear();
	    		}
	    		returnMsg = null;
    		}
    		syncClient = null;
    		returnMsg = null;
    		builder_msg = null;

    		tmpPath = null;
    		nfolder = null;
    		filez = null;
    		filepath = null;
    		projectPath = null;
    		filestatus = null;
    		filedata = null;
    		root = null;
			
			ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName()).refreshLocal(IResource.DEPTH_INFINITE, monitor);
    		
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
tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
    		
    		if("I".equals(tool)){
				Map<String, String> mapParam = new HashMap<String, String>();
				/*
				 * project checkout priority process
				 * command.team.BeforeProjectCheckOut
				 */
				monitor.subTask("\ud504\ub85c\uc81d\ud2b8 \uccb4\ud06c\uc544\uc6c3 \uc900\ube44 \uc911..");
				monitor.worked(1);
				
				try{
					mapParam = new HashMap<String, String>();
					mapParam.put("PROJECT_NAME", project.getName());
					CommandExecuter.executeCommand("command.team.BeforeProjectCheckOut", mapParam);
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					mapParam.clear();
					mapParam = null;
				}
				
				
				if(commandmapParam.size()>0){
					/*
					 * project checkout complete process
					 * command.team.DoneCheckInOut
					 */
					monitor.subTask("\uad00\ub828 \ub9ac\uc18c\uc2a4 \uc815\ubcf4 \ud638\ucd9c \uc911..");
					monitor.worked(1);
					
					try{
						Map<String, Map<String, String>> parameter = new HashMap<String, Map<String, String>>();
						parameter.put(project.getName(), commandmapParam);
						CommandExecuter.executeCommand("command.team.DoneCheckInOut", parameter);
						parameter.clear();
						parameter = null;
					}catch(Exception e){
						e.printStackTrace();
					}finally{
						commandmapParam.clear();
						commandmapParam = null;
					}
				}
				
				/*
				 * project checkout complete project process
				 * command.team.DoneProjectCheckOut
				 */
				monitor.subTask("\ud504\ub85c\uc81d\ud2b8 \uccb4\ud06c\uc544\uc6c3 \uc644\ub8cc \uc911..");
				monitor.worked(1);
				try{
					mapParam = new HashMap<String, String>();
					mapParam.put("PROJECT_NAME", project.getName());
					CommandExecuter.executeCommand("command.team.DoneProjectCheckOut", mapParam);
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					mapParam.clear();
					mapParam = null;
				}
			}
    		tool = null;
			
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
			result = null;
			
			List<IResource> labelRefreshList = new ArrayList<IResource>();
			labelRefreshList.add((IResource)project);
			EcamsProviderPlugin.broadcastModificationStateChanges((IResource[]) labelRefreshList.toArray(new IResource[labelRefreshList.size()]));
			labelRefreshList = null;
			
			if (errList.size() > 0){
				Display.getDefault().syncExec(new Runnable() {
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
				errList = null;
			}
		}
	}
}
