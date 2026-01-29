package com.azsoft.ecams.core.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
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

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.core.resource.EcamsResourceStatus;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.PathInfo;
import com.azsoft.ecams.proto.ProtoEcams.RequestInfo;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SRInfo;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;

public class CompleteSRJob extends WorkspaceJob {
	
	private String SysCd, SrId = "";
	private String errMsg = "";
	private List<IResource> labelRefreshList = new ArrayList<IResource>();
	
	public CompleteSRJob(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public CompleteSRJob(String name, String SysCd, String SrId) {
		// TODO Auto-generated constructor stub
		super(name);
		this.SysCd = SysCd;
		this.SrId = SrId;
		addJobChangeListener(new LabelChangeListener());
	}
	
	public IStatus runInWorkspace(IProgressMonitor monitor){
		try{
			String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
			String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);

			if((null == ip || "".equals(ip)) || (null == port || "".equals(port))
					|| (null == id || "".equals(id)) || (null == passwd || "".equals(passwd))){
				errMsg = "[Window->Preferences]\uc5d0\uc11c IP, PORT, ID, PASSWORD\ub97c \uc785\ub825\ud558\uc138\uc694.";
				return Status.CANCEL_STATUS;
			}
			
			//\ub2f4\ub2f9\uc790\uc758 SR\uc0c1\ud0dc \ud655\uc778(\ubaa8\ub4e0 \ud504\ub85c\uadf8\ub7a8\uc758 \uc0c1\ud0dc\uac00 8\uc778\uc9c0) count(editor, srid, status <> 8)
			//count=0, status=8\uc778 \ub9ac\uc18c\uc2a4 \ubaa9\ub85d\uc744 \ubc1b\uc544\uc11c \ub9ac\uc18c\uc2a4\uc0c1\ud0dc G\ub85c \uc0c1\ud0dc\ubcc0\uacbd & CMC0020 \ub2f4\ub2f9\uc790 \uc0c1\ud0dc\ubcc0\uacbd
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("DEVCOMPLETE");
			
    		UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(id);
			userinfo_builder.setPasswd(passwd);
			builder_msg.setUserinfo(userinfo_builder.build());

			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
    		sysinfo_builder.setSyscd(SysCd);
    		builder_msg.setSysinfo(sysinfo_builder.build());

			SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
    		srinfo_builder.setCcSRId(SrId);
    		builder_msg.setSrinfo(srinfo_builder.build());
    		
    		EcamsClient syncClient = new EcamsClient(ip,port);
    		ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
    		
    		if (returnMsg.getReturnStr().startsWith("SOCKERR")){
				errMsg = "SOCKET ERROR";
				return Status.CANCEL_STATUS;
			}else if (returnMsg.getReturnval() == 1) {
    			errMsg = returnMsg.getReturnStr();
    			return Status.CANCEL_STATUS;
			}

    		List updateList = new ArrayList();
    		updateList.addAll(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList());
			
			if(updateList.size()>0){
				IPath tmpPath = null;
				IResource tmpResource = null;
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				
				IProject project = null;
				File filez = null;

	    		monitor.beginTask("Information Updating..", updateList.size()+20);
	    		
				for(int i=0; i<updateList.size(); i++){
					FileData filedata = (FileData) updateList.get(i);
					
					IProject findProject = EcamsRepositoryProvider.findProject(filedata.getSysinfo().getSyscd());
					
					if(findProject == null){
						continue;
					}
					
					String projectPath = findProject.getLocation().toString();
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
					tmpPath = new Path(filename);
					
					tmpResource = root.getFileForLocation(tmpPath);
					tmpPath = null;
					

					monitor.subTask(filename);
					monitor.worked(1);		
					
					filez = new File(filename);
					
					if (filez.exists()){
						project = tmpResource.getProject();
						
						IEcamsStatus filestatus = new EcamsResourceStatus();
						filestatus.setStatus(filedata, project);
						
						EcamsProviderPlugin.getPlugin().getXmlStatusMgr().new_updateStatus((EcamsResourceStatus)filestatus, project,"A");
						labelRefreshList.add(tmpResource);
					}
					
					tmpResource = null;
				}
				updateList = null;
				
				if(labelRefreshList.size()>0){
					IResource[] syncResoruces = (IResource[])labelRefreshList.toArray(new IResource[labelRefreshList.size()]);

					for(int i=0;i<syncResoruces.length;i++){
		    			((IResource) syncResoruces[i]).refreshLocal(IResource.DEPTH_ONE, monitor);
					}
				}
				
			}
			return Status.OK_STATUS;
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
			monitor.done();
		}
	}
	

	final class LabelChangeListener extends JobChangeAdapter {
		public void done(IJobChangeEvent event) {
			IStatus result = event.getResult();
			if(result.getSeverity() == IStatus.CANCEL) {
				if(!"".equals(errMsg)){
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageBox messageBox = new MessageBox(new Shell());
							messageBox.setMessage(errMsg);
							messageBox.setText("ERROR");
							messageBox.open();
						}
					});
				}
				return;
			} else if(result.getMessage().equals("OK")){
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						MessageBox messageBox = new MessageBox(new Shell());
						messageBox.setMessage("\uc815\uc0c1\uc801\uc73c\ub85c \uac1c\ubc1c\uc644\ub8cc \ub418\uc5c8\uc2b5\ub2c8\ub2e4.\n" +
								"\uc77c\ubc18: \uac1c\ubc1c\ucc45\uc784\uc790\uc5d0\uac8c \uc801\uc6a9\uc2e0\uccad\uc744 \uc694\uccad\ud558\uc2ed\uc2dc\uc624.\n" +
								"\ucc28\uc138\ub300: \ubcf8\uc778\uc774 \uc801\uc6a9\uc2e0\uccad \uac00\ub2a5\ud569\ub2c8\ub2e4.");
						messageBox.setText(SrId);
						messageBox.open();
					}
				});
			}
			
			EcamsProviderPlugin.broadcastModificationStateChanges((IResource[]) labelRefreshList.toArray(new IResource[labelRefreshList.size()]));
		}
	}
}