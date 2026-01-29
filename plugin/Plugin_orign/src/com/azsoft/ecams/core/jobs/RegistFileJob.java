package com.azsoft.ecams.core.jobs;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.CodeInfo;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.FileDataList;
import com.azsoft.ecams.proto.ProtoEcams.JobInfo;
import com.azsoft.ecams.proto.ProtoEcams.LangInfo;
import com.azsoft.ecams.proto.ProtoEcams.PathInfo;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.RsrcInfo;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;

public class RegistFileJob extends WorkspaceJob{
	private Logger logger = Logger.getLogger(this.getClass());
	
	private IResource[] resources;
	private String jobcd,gradecd,rsrccd,langcd,comment;
	private List errList = new ArrayList();

	private String errMsg=null;
	private int errVal;
	
	private List labelRefreshList = new ArrayList();
	
	public RegistFileJob(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public RegistFileJob(String name,IResource[] resources,String jobcd,
			String gradecd,String rsrccd,String langcd,String comment) {
		super(name);
		this.resources = resources;
		this.jobcd = jobcd;
		this.gradecd = gradecd;
		this.rsrccd = rsrccd;
		this.langcd = langcd;
		this.comment = comment;
		
		addJobChangeListener(new RegistFileJobChangeListener());
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		try{
			int i;
			//boolean rtFlag=false;
			//boolean downFlag=false;
			
			errList.clear();
			IProject project= resources[0].getProject();
			
			FileDataList.Builder fileDataList_builder = FileDataList.newBuilder();
			for(i=0;i<resources.length;i++){			
				FileData.Builder fileData_builder = FileData.newBuilder();
			
				RsrcInfo.Builder rsrcinfo_builder = RsrcInfo.newBuilder();
				rsrcinfo_builder.setRsrccd(rsrccd);
				
				JobInfo.Builder jobinfo_builder = JobInfo.newBuilder();
				jobinfo_builder.setJobcd(jobcd);
				
				LangInfo.Builder langinfo_builder = LangInfo.newBuilder();
				langinfo_builder.setLangcd("");//langcd);
			
				fileData_builder.setFilename(resources[i].getName());
				PathInfo.Builder pathinfo_builder = PathInfo.newBuilder();
				pathinfo_builder.setRelativitePath(((IResource)resources[i].getParent()).getProjectRelativePath().toString());
				fileData_builder.setPathinfo(pathinfo_builder.build());
				fileData_builder.setRsrcinfo(rsrcinfo_builder.build());
				fileData_builder.setJobinfo(jobinfo_builder.build());
				fileData_builder.setLanginfo(langinfo_builder.build());
				fileData_builder.setReqcd(comment);
				
				
			
				fileDataList_builder.addFiledatas(fileData_builder.build());
			}
			
			
			String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null));
			userinfo_builder.setPasswd(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null));
			
			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
			sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
			sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);
			
			CodeInfo.Builder codeinfo_builder = CodeInfo.newBuilder();
			codeinfo_builder.setMacode("PGMGRADE");
			codeinfo_builder.setMicode(gradecd);
			
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("REGISTFILE");
			
			builder_msg.setCodeinfo(codeinfo_builder.build());
			builder_msg.setFiledatalist(fileDataList_builder.build());
			builder_msg.setUserinfo(userinfo_builder.build());
			builder_msg.setSysinfo(sysinfo_builder.build());	
			
			EcamsClient ecamsclient = new EcamsClient(ip,port);
			ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
			
			if (returnMsg.getReturnval() != 0){
				errMsg = "\uc2e0\uaddc\ub4f1\ub85d \uc2e4\ud328 \ucc98\ub9ac\uacb0\uacfc["+returnMsg.getReturnStr()+"]";
				errVal = returnMsg.getReturnval();
				return Status.CANCEL_STATUS;
			}
			
			EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,"","NONE"));	
			
			return Status.OK_STATUS;
		}
		catch (CoreException e){
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}
	}
	/*
	final class RegistFileJobChangeListener extends JobChangeAdapter {

		public void done(IJobChangeEvent event) {
			IStatus result = event.getResult();
			if(result.getSeverity() == IStatus.CANCEL) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {		
						EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources));		
						MessageBox messageBox = new MessageBox(new Shell());
						messageBox.setMessage(errMsg);
						messageBox.setText("\uc2e0\uaddc\ub4f1\ub85d");
						messageBox.open();
					}
				});
				return;
			} else if(!result.isOK()) {
				// XXX report errors
				return;
			}
			
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {				
					//MessageBox messageBox = new MessageBox(new Shell());
					//messageBox.setMessage("\uc2e0\uaddc\ub4f1\ub85d \ucc98\ub9ac\uc644\ub8cc.");
					//messageBox.setText("\uc2e0\uaddc\ub4f1\ub85d");
					//messageBox.open();
				}
			});

		}
	}
	*/
	final class RegistFileJobChangeListener extends JobChangeAdapter {

		public void done(IJobChangeEvent event) {
			IStatus result = event.getResult();
			if(result.getSeverity() == IStatus.CANCEL) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,"","NONE"));
						MessageBox messageBox = new MessageBox(new Shell());
						messageBox.setMessage(errMsg);
						messageBox.setText("\uc2e0\uaddc\ub4f1\ub85d");
						messageBox.open();
					}
				});
			} else if(!result.isOK()) {
				// XXX report errors
			}
			return;
		}
	}

}
