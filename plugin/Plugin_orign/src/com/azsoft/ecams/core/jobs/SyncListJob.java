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
import java.util.List;
import java.util.Map;

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
import com.azsoft.ecams.proto.ProtoEcams.RsrcInfo;
import com.azsoft.ecams.proto.ProtoEcams.RsrcInfoList;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.dialog.ErrorMessageDlg;
import com.azsoft.ecams.util.checksum.CheckSum;
import com.azsoft.ecams.util.file.EGzip;
import com.ibm.icu.text.SimpleDateFormat;

public class SyncListJob extends WorkspaceJob {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private IResource[] resources = null;
	private List errList = new ArrayList();
	private IProject project = null;
	private String errMsg = "";
	private File iMetaFile = null;
	//public List labelRefreshList = new ArrayList();
	
	
	public SyncListJob(String name) {
		// TODO Auto-generated constructor stub
		super(name);
	}
	public SyncListJob(String name, IResource[] resources, IProject project) {
		// TODO Auto-generated constructor stub
		super(name);
		this.resources = resources;
		this.project = project;
		addJobChangeListener(new SyncJobChangeListener());
	}
	
	public IStatus runInWorkspace(IProgressMonitor monitor){
		int i = 0;
		String tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);

		Map<String, String> commandmapParam = new HashMap<String, String>();
		try {

			String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
			String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
			
			List<IResource> tmpResource = new ArrayList<IResource>();
			tmpResource = new ArrayList<IResource>();
			for(int rscCnt=0; rscCnt<resources.length; rscCnt++){
				if(project.equals(resources[rscCnt].getProject())){
					tmpResource.add(resources[rscCnt]);
				}
			}
			
			if(tmpResource.size()>0){
				monitor.beginTask("P/"+project.getName()+" Synchronizing..", tmpResource.size()+30);
				
				FileData filedata = null;
				FileOutputStream fw = null;
				IEcamsStatus filestatus = null;
	
				String projectPath = "";
				String filepath = "";
				String filename = "";
				String filename_back = "";
				String filename_back_dir = "";
				File filez = null;
				File nfolder = null;
				
				EcamsClient syncClient = null;
				EcamsMessage.Builder builder_msg = null;
				ReturnMsg returnMsg = null;
				
				SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
	    		sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
	    		sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);
	    		
				UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
				userinfo_builder.setId(id);
				userinfo_builder.setPasswd(passwd);
/*
	    		JobInfoList.Builder jobinfo_builder = JobInfoList.newBuilder();
	    		JobInfo.Builder job_builder = JobInfo.newBuilder();
	    		job_builder.setJobcd(project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[0]);
	    		job_builder.setJobname(project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[1]);
	    		jobinfo_builder.addJobinfo(job_builder.build());
	    		*/
				RequestInfo.Builder requestinfo =  RequestInfo.newBuilder();
				requestinfo.setQrycd("04");
	    		requestinfo.setAcptno("");
	    		
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
	    		
	    		
				for(i=0; i<tmpResource.size(); i++){
				
					builder_msg = EcamsMessage.newBuilder();
					builder_msg.setMsgtype("SYNC_FILEDATA");
					
					builder_msg.setUserinfo(userinfo_builder.build());
					FileData.Builder filedata_builder = FileData.newBuilder();
					filedata_builder.setFilename(tmpResource.get(i).getName());
					
					PathInfo.Builder pathInfo_builder = PathInfo.newBuilder();
					pathInfo_builder.setRelativitePath(tmpResource.get(i).getParent().getProjectRelativePath().toOSString());
					filedata_builder.setPathinfo(pathInfo_builder.build());
					
					builder_msg.setFiledata(filedata_builder.build());
	
					builder_msg.setSysinfo(sysinfo_builder.build());						
					//builder_msg.setJobinfolist(jobinfo_builder.build());
		    		builder_msg.setRequestinfo(requestinfo.build());
					syncClient = new EcamsClient(ip,port);
		    		returnMsg = syncClient.sendMsg(builder_msg.build());
		    		builder_msg = null;
		    		syncClient = null;
		    		
		    		if(returnMsg.getReturnval() == 0){
		    			filedata = (FileData) returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList().get(0);
	
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
		    			
						monitor.subTask(filename);
						monitor.worked(1);		
						
						filename_back_dir = filepath+"/.back";
		    			filename_back_dir = filename_back_dir.replace("TFSKR/iStudio", "TFSKR/ecamsback");
		    			filename_back = filename_back_dir+"/"+filedata.getFilename()+".back";
						monitor.subTask(filename);
						monitor.worked(1);		
						
						filestatus = new EcamsResourceStatus();
						filestatus.setStatus(filedata, project);
						
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
						
						filez = new File(filename);
						nfolder = new File(filepath);
		
						if (!nfolder.exists()){
							nfolder.mkdirs();
						}
						filez.createNewFile();
						
						// 소스 백업 ----------------------------------------------------------------
						File nfolder1 = new File(filename_back_dir);
						if(!nfolder1.exists()){
							nfolder1.mkdirs();
						} 
						File sourceFile = new File(filename);
						FileInputStream input = null;
						FileOutputStream output = null;
						FileChannel fcin = null;
						FileChannel fcout = null;
						
						input = new FileInputStream(sourceFile);
						output = new FileOutputStream(filename_back);
						
						fcin = input.getChannel();
						fcout = output.getChannel();
						
						long size = fcin.size();
						fcin.transferTo(0,size,fcout);
						
						fcin.close();
						fcout.close();
						//-------------------------------------------------------------------------
						
			    		try{
							fw = new FileOutputStream(filez);
							fw.write(EGzip.getDecompressedByte(filedata.getFilebytes().toByteArray()));
							fw.flush();
							fw.close();
					    } catch (IOException e) {
							// TODO Auto-generated catch block
					    	errMsg = e.toString();
					    	errList.add(tmpResource.get(i));
							//e.printStackTrace();
							continue;
							//return Status.CANCEL_STATUS;
						}
						fw = null;
		
						EcamsProviderPlugin.getPlugin().getXmlStatusMgr().new_updateStatus((EcamsResourceStatus)filestatus, project,"A");
						
						
						filestatus = null;
						returnMsg = null;
						filedata = null;
						nfolder = null;
			    		filez = null;
		    		}
				}
				
				iMetaStr = null;
				if(fos != null) try { fos.close(); }catch (Exception e){}
				
				for(i=0;i<resources.length;i++){
					if (monitor.isCanceled()){
						return Status.CANCEL_STATUS;
					}

		    		if ((resources[i]).getType() == IResource.FOLDER){
		    			(resources[i]).refreshLocal(IResource.FOLDER, monitor);
		    		}else{
		    			(resources[i]).refreshLocal(IResource.DEPTH_ONE, monitor);
		    		}
				}
				
				tmpResource = null;
			}
			
			return Status.OK_STATUS;
			
		}catch (NullPointerException e){
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}catch (Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}finally{
			if("I".equals(tool)){
	    		Map<String, Map<String, String>> parameter = new HashMap<String, Map<String, String>>();
				
				if(commandmapParam.size()>0){
					monitor.subTask("command.team.DoneCheckInOut");
					monitor.worked(1);	
					
					try{
						parameter = new HashMap<String, Map<String, String>>();
						parameter.put(project.getName(), commandmapParam);
						CommandExecuter.executeCommand("command.team.DoneCheckInOut", parameter);
						parameter = null;
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}

			commandmapParam = null;
			
			monitor.done();
		}
	}
	
	final class SyncJobChangeListener extends JobChangeAdapter {
		public void done(IJobChangeEvent event) {
			IStatus result = event.getResult();
			if(result.getSeverity() == IStatus.CANCEL) {
				return;
			} else if(!result.isOK()) {
				// XXX report errors
				return;
			}
			
			if (errList.size() > 0){
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						ErrorMessageDlg errDlg = new ErrorMessageDlg(new Shell(),(IResource[])errList.toArray(new IResource[errList.size()]),"Sync Error","\ub2e4\uc74c \ud30c\uc77c\ub4e4\uc740 \ub3d9\uae30\ud654\uc911 \uc624\ub958\uac00 \ubc1c\uc0dd\ud558\uc600\uc2b5\ub2c8\ub2e4.\n\ud504\ub85c\uadf8\ub7a8\uc774 \uc5f4\ub824\uc788\ub294 \uacbd\uc6b0 \ub2eb\uace0 \ub2e4\uc2dc \uc2dc\ub3c4\ud574 \ubcf4\uc2dc\uae30 \ubc14\ub78d\ub2c8\ub2e4.\n"+errMsg);
						errDlg.open();				
					}
				});
			}
			
			if(resources.length>0){
				EcamsProviderPlugin.broadcastModificationStateChanges(resources);
			}
		}
	}

}
