package com.azsoft.ecams.core.jobs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.azsoft.ecams.core.CommandExecuter;
import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
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
import com.azsoft.ecams.ui.dialog.ErrorMessageDlg;

public class CheckOutCnlJob extends WorkspaceJob {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private IResource[] resources;
	private List errList = new ArrayList();
	private String errMsg=null;
	private int errVal;
	
	private List labelRefreshList = new ArrayList();	
	private String srId, srTitle;
	private boolean chkModify;
	String acptno = "";
	
	public CheckOutCnlJob(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	public CheckOutCnlJob(String name,IResource[] resources, String srId, String srTitle, boolean chkModify) {
		super(name);
		this.resources = resources;
		this.srId = srId;
		this.srTitle = srTitle;
		this.chkModify = chkModify;
		addJobChangeListener(new CheckOutCnlJobChangeListener());
	}
	
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor){
		try{
			// TODO Auto-generated method stub
			int i;
			
			errList.clear();
			
			IEcamsStatus[] filestatuses = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatuses(resources);
			if (filestatuses.length != resources.length){
				errMsg = "\uc0c1\ud0dc\uac12 \uc624\ub958.";
				errVal = 1;
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
			
			/*
			JobInfoList.Builder jobinfo_builder = JobInfoList.newBuilder();
			JobInfo.Builder jobinfo = JobInfo.newBuilder();
			jobinfo.setJobcd(project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[0]);
			jobinfo.setJobname(project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[1]);
			jobinfo_builder.addJobinfo(jobinfo);
			*/
			
			RequestInfo.Builder requestinfo_builder = RequestInfo.newBuilder();
			requestinfo_builder.setQrycd("11");

			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			
			if(!chkModify){
				SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
				srinfo_builder.setCcEditor(id);
				srinfo_builder.setCcSRId(srId);
				srinfo_builder.setCcTitle(srTitle);	
				builder_msg.setSrinfo(srinfo_builder.build());
			}
			
			builder_msg.setMsgtype("CHECKOUTCNL");
			builder_msg.setRequestinfo(requestinfo_builder.build());
			builder_msg.setFiledatalist(fileDataList_builder.build());
			sysinfo_builder.setRsrcinfolist(rsrcInfoList_builder.build());
			builder_msg.setUserinfo(userinfo_builder.build());
			builder_msg.setSysinfo(sysinfo_builder.build());
			builder_msg.setJobinfolist(jobinfo_builder.build());
			
			EcamsClient ecamsclient = new EcamsClient(ip,port);
			ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
			
			if (returnMsg.getReturnStr().startsWith("SOCKERR")){
				errVal = 1;
				errMsg = "SOCKET ERROR";
				return Status.CANCEL_STATUS;
			}else{
				if (returnMsg.getReturnval() != 0){
					errMsg = "\uccb4\ud06c\uc544\uc6c3\ucde8\uc18c \uc2e4\ud328 \ucc98\ub9ac\uacb0\uacfc["+returnMsg.getReturnStr()+"]";
					errVal = returnMsg.getReturnval();
					//EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources, ""));
					EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,"","NONE"));
					return Status.CANCEL_STATUS;
				}
			}

			acptno = returnMsg.getReturnStr();
			
			builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("REQUEST_COMPLETE");

			RequestInfo.Builder request_builder = RequestInfo.newBuilder();
			request_builder.setQrycd("11");
			request_builder.setAcptno(acptno);
			builder_msg.setRequestinfo(request_builder.build());
			builder_msg.setUserinfo(userinfo_builder.build());
			returnMsg = ecamsclient.sendMsg(builder_msg.build());

			String tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
			if("I".equals(tool)){
				/*
				 * project checkout cancel process
				 * command.team.ResourceItemDeleted
				 */
	
				Map<String, Map<String, String>> parameter = new HashMap<String, Map<String, String>>(); 
				Map<String, String> mapParam = new HashMap<String, String>();
				try {
					for (i=0;i<resources.length;i++){
						IEcamsStatus tmpStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(resources[i]);
//						mapParam.put(resources[i].getProject().getName()+"/"+resources[i].getProjectRelativePath().toString(), Integer.toString(tmpStatus.getLastVer())+"."+tmpStatus.getTstVer());
						mapParam.put(resources[i].getProject().getName()+"/"+resources[i].getProjectRelativePath().toString(), tmpStatus.getViewver());
					}
					
					parameter.put(project.getName(), mapParam);
					CommandExecuter.executeCommand("command.team.ResourceItemDeleted", parameter);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					errMsg = e1.getMessage();
					return Status.CANCEL_STATUS;
				} finally {
					mapParam = null;
					parameter = null;
				}
			}
			
			try {
				Thread.sleep(1000);
				EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,acptno,"NONE"));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			/*
			try {
				Thread.sleep(1000);
				EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,acptno));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			return Status.OK_STATUS;
		}
		catch (CoreException e){
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}	
		
	}
	
	
	final class CheckOutCnlJobChangeListener extends JobChangeAdapter {

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
							messageBox.setText("CheckOutCnl ERROR");
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
						ErrorMessageDlg errDlg = new ErrorMessageDlg(new Shell(),(IResource[])errList.toArray(new IResource[errList.size()]),"\uccb4\ud06c\uc544\uc6c3\ucde8\uc18c\uc624\ub958","\ub2e4\uc74c \ud30c\uc77c\ub4e4\uc740 \uccb4\ud06c\uc544\uc6c3\ucde8\uc18c\uc911 \uc624\ub958\uac00 \ubc1c\uc0dd\ud558\uc5ec \uc790\ub3d9\ubc18\ub824 \ub418\uc5c8\uc73c\ub2c8 \ub2e4\uc2dc \uccb4\ud06c\uc544\uc6c3\ucde8\uc18c \ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
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
