package com.azsoft.ecams.core.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.FileDataList;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;

public class DeleteJob extends WorkspaceJob {
private Logger logger = Logger.getLogger(this.getClass());
	
	private IResource[] resources;
	private IResource[] delresources;
	private String errMsg;
	private IProject myproject;

	public DeleteJob(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public DeleteJob(String name,IResource[] resources) {
		super(name);
		this.resources = resources;
		addJobChangeListener(new DeleteJobChangeListener());
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)	throws CoreException {
		// TODO Auto-generated method stub
		
		IEcamsStatus[] filestatuses = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatuses_regist(resources);
		
		if (filestatuses.length != resources.length){
			errMsg = "\uc0c1\ud0dc\uac12 \uc624\ub958.";
			EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,"","NONE"));
			return Status.CANCEL_STATUS;
		}
		
		FileDataList.Builder fileDataList_builder = FileDataList.newBuilder();
		List delList = new ArrayList();	
		
		for (int i=0;i<filestatuses.length;i++){
			if(filestatuses[i].getItemid() != null && !filestatuses[i].getItemid().equals("")){ //\uc6b4\uc601\uc911
				FileData.Builder fileData_builder = FileData.newBuilder();
				fileData_builder.setItemid(filestatuses[i].getItemid());
				fileData_builder.setFilename(resources[i].getName());
				fileDataList_builder.addFiledatas(fileData_builder.build());
				fileData_builder=null;
				
				delList.add(resources[i]);
				delresources = (IResource[])delList.toArray(new IResource[delList.size()]);
			} 
			myproject = ((IResource[])resources)[i].getProject();
			String projectPath = myproject.getLocation().toOSString();
			//String filepath = projectPath+"/"+((IResource)resources[i]).getProjectRelativePath().toString();
			while(projectPath.indexOf("/") >=0){
				projectPath =projectPath.replace("/","\\");
			}
			while(projectPath.indexOf("\\\\") >=0){
				projectPath = projectPath.replace("\\\\", "\\");
			}
			while(projectPath.indexOf("\\") >=0){
				projectPath = projectPath.replace("\\", "/");
			}
			String filename = projectPath + "/" + resources[i].getName(); 
			String deconame = projectPath+"/.deco/."+resources[i].getName()+".ecm-meta";
			
			logger.error("Delete filepath+filename : " + filename);
			File nfile = new File(filename);
			
			if (nfile.exists()) {
				nfile.delete();
			}
			
			File decofile = new File(filename);
			decofile = new File(deconame);
			if(decofile.exists()){
				decofile.delete();
			}
		}
		
		if(fileDataList_builder.getFiledatasCount() > 0){
			String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null));
			userinfo_builder.setPasswd(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null));
			
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("DELETE");
			builder_msg.setFiledatalist(fileDataList_builder.build());
			builder_msg.setUserinfo(userinfo_builder.build());
			
			EcamsClient ecamsclient = new EcamsClient(ip,port);
			ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
			
			if (returnMsg.getReturnval() != 0){
				errMsg = "DELETE FAIL";
				return Status.CANCEL_STATUS;
			}
			
			EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",delresources,"","NONE"));
		}//else{
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		//}
		
		return Status.OK_STATUS;
	}
	
	final class DeleteJobChangeListener extends JobChangeAdapter {

		public void done(IJobChangeEvent event) {
			IStatus result = event.getResult();
			if(result.getSeverity() == IStatus.CANCEL) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,"","NONE"));
						MessageBox messageBox = new MessageBox(new Shell());
						messageBox.setMessage(errMsg);
						messageBox.setText("\ud30c\uc77c\uc0ad\uc81c");
						messageBox.open();
					}
				});
				return;
			} else if(!result.isOK()) {
				// XXX report errors
				return;
			}
		}
	}
}