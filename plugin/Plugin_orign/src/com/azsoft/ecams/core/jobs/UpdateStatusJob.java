package com.azsoft.ecams.core.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.azsoft.ecams.core.CommandExecuter;
import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.core.resource.EcamsResourceStatus;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.JobInfo;
import com.azsoft.ecams.proto.ProtoEcams.JobInfoList;
import com.azsoft.ecams.proto.ProtoEcams.PathInfo;
import com.azsoft.ecams.proto.ProtoEcams.RequestInfo;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;

public class UpdateStatusJob extends WorkspaceJob {
	private Logger logger = Logger.getLogger(this.getClass());

	Map<String, String> commandmapParam = new HashMap<String, String>();
	private IResource[] resources;
	private String acptno = "";
	public List<IResource> labelRefreshList = new ArrayList<IResource>();
	
	public UpdateStatusJob(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public UpdateStatusJob(String name, IResource[] resources, String acptno) {
		// TODO Auto-generated constructor stub
		super(name);
		this.acptno = acptno;
		this.resources = resources;
		addJobChangeListener(new LabelChangeListener());
	}
	
	public IStatus runInWorkspace(IProgressMonitor monitor){
		String tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
		IProject project = resources[0].getProject();
		
		try{
			String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
			String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);

			if((null == ip || "".equals(ip)) || (null == port || "".equals(port))
					|| (null == id || "".equals(id)) || (null == passwd || "".equals(passwd))){
				return Status.CANCEL_STATUS;
			}
			
			if(resources.length==0) return Status.CANCEL_STATUS;
			
			int i = 0;
			List syncLists = new ArrayList();
			

			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("SYNC_PROJECT_GETLIST");

			RequestInfo.Builder requestinfo =  RequestInfo.newBuilder();
			requestinfo.setQrycd("04");
			
    		UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(id);
			userinfo_builder.setPasswd(passwd);
			builder_msg.setUserinfo(userinfo_builder.build());
			
			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
    		sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
    		sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);
    		builder_msg.setSysinfo(sysinfo_builder.build());
/*
    		JobInfoList.Builder jobinfo_builder = JobInfoList.newBuilder();
    		JobInfo.Builder job_builder = JobInfo.newBuilder();
    		job_builder.setJobcd(project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[0]);
    		job_builder.setJobname(project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[1]);
    		jobinfo_builder.addJobinfo(job_builder.build());
    		builder_msg.setJobinfolist(jobinfo_builder.build());
    		*/
			if(null != acptno && !"".equals(acptno)){
				requestinfo.setAcptno(acptno);
	    		builder_msg.setRequestinfo(requestinfo.build());
				
	    		EcamsClient syncClient = new EcamsClient(ip,port);
				syncClient = new EcamsClient(ip,port);
	    		ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
	    		
	    		if (returnMsg.getReturnStr().startsWith("SOCKERR")){
					return Status.CANCEL_STATUS;
				}else{
		    		if(returnMsg.getReturnval() == 0){
		    			syncLists.addAll(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList());
		    		}
				}
			}else{
				for(i=0;i<resources.length;i++){
					requestinfo.setAcptno("");
		    		builder_msg.setRequestinfo(requestinfo.build());
		    		
		    		if (((IResource) resources[i]).getType() == IResource.FOLDER){
			    		PathInfo.Builder pathInfo_builder = PathInfo.newBuilder();
			    		pathInfo_builder.setRelativitePath(((IResource)resources[i]).getProjectRelativePath().toString());
			    		builder_msg.setPathinfo(pathInfo_builder.build());
		    		}else{
		    			FileData.Builder filedata_builder = FileData.newBuilder();
	    				filedata_builder.setFilename(((IResource)resources[i]).getName());
	    				PathInfo.Builder pathInfo_builder = PathInfo.newBuilder();
	    				pathInfo_builder.setRelativitePath(((IResource)resources[i]).getParent().getProjectRelativePath().toString());
	    				filedata_builder.setPathinfo(pathInfo_builder.build());
	    				builder_msg.setFiledata(filedata_builder.build());
		    		}
		    		
		    		EcamsClient syncClient = new EcamsClient(ip,port);
					syncClient = new EcamsClient(ip,port);
		    		ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
		    		
		    		if (returnMsg.getReturnStr().startsWith("SOCKERR")){
						return Status.CANCEL_STATUS;
					}else{
			    		if(returnMsg.getReturnval() == 0){
			    			syncLists.addAll(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList());
			    		}
					}
				}
			}

			String filepath = "";
			String filename = "";
			File filez = null;

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			monitor.beginTask("P/"+project.getName()+" Synchronizing..", syncLists.size()+20);
			
			for(i=0;i<syncLists.size();i++){
				if (monitor.isCanceled()){
					return Status.CANCEL_STATUS;
				}
				FileData filedata = (FileData) syncLists.get(i);
				
				IEcamsStatus filestatus = new EcamsResourceStatus();
				filestatus.setStatus(filedata, project);
				
				filepath = project.getLocation().toString()+"/"+filedata.getPathinfo().getRelativitePath();
				
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

				IPath tmpPath = new Path(filename);
				IResource tmpResource = root.getFileForLocation(tmpPath);
				
				filez = new File(filename);
				
				if (filedata.getRsrcinfo().getCminfo().substring(2,3).equals("1") || filez.exists()){
					if(null != acptno && !"".equals(acptno)){
						EcamsProviderPlugin.getPlugin().getXmlStatusMgr().new_updateStatus((EcamsResourceStatus)filestatus, project,"A");
						if("I".equals(tool)){
							//int tstver = Integer.getInteger(filedata.getViewver().substring(filedata.getViewver().lastIndexOf("."))+1) + 1;	//20201222 그냥 viewver로 보내기
							commandmapParam.put(project.getName()+filedata.getPathinfo().getRelativitePath()+"/"+filedata.getFilename(), filedata.getViewver());
						}
					}else{
						EcamsProviderPlugin.getPlugin().getXmlStatusMgr().new_updateStatus((EcamsResourceStatus)filestatus, project,"N");
						//EcamsProviderPlugin.getPlugin().getXmlStatusMgr().label_updateStatus((EcamsResourceStatus)filestatus, project);
					}
					labelRefreshList.add(tmpResource);
				}
			}
			
			IResource[] syncResoruces = (IResource[])labelRefreshList.toArray(new IResource[labelRefreshList.size()]);
			syncLists = null;
			for(i=0;i<syncResoruces.length;i++){
				((IResource) syncResoruces[i]).refreshLocal(IResource.DEPTH_ONE, monitor);
			}
			
			return Status.OK_STATUS;
		}catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}catch (NullPointerException e){
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}catch (Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}finally{
			if("I".equals(tool)){
				if(null != acptno && !"".equals(acptno)){
					/*
					 * project checkout complete process
					 * command.team.DoneCheckInOut
					 */
					try{
//						logger.info(">>>>>>>> command.team.DoneCheckInOut		start");
						
						HashMap<String, Map<String, String>> parameter = new HashMap<String, Map<String, String>>();
						parameter.put(project.getName(), commandmapParam);
						CommandExecuter.executeCommand("command.team.DoneCheckInOut", parameter);
						parameter = null;
					}catch(Exception e){
						e.printStackTrace();
//						logger.info(">>>>>>>> command.team.DoneCheckInOut		exception"+e.getMessage()+"\n"+e.getCause());
					}
//					logger.info(">>>>>>>> command.team.DoneCheckInOut		end");
				}
				
				commandmapParam = null;
			}
			
			monitor.done();
		}
	}
	

	final class LabelChangeListener extends JobChangeAdapter {
		public void done(IJobChangeEvent event) {
			IStatus result = event.getResult();
			if(result.getSeverity() == IStatus.CANCEL) {
				return;
			} else if(!result.isOK()) {
				// XXX report errors
				return;
			}
			
			EcamsProviderPlugin.broadcastModificationStateChanges((IResource[]) labelRefreshList.toArray(new IResource[labelRefreshList.size()]));
			labelRefreshList.clear();
			labelRefreshList = null;
		}
	}
}
