package com.azsoft.ecams.core.jobs;


import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.FileDataList;
import com.azsoft.ecams.proto.ProtoEcams.JobInfo;
import com.azsoft.ecams.proto.ProtoEcams.JobInfoList;
import com.azsoft.ecams.proto.ProtoEcams.PathInfo;
import com.azsoft.ecams.proto.ProtoEcams.RequestInfo;
import com.azsoft.ecams.proto.ProtoEcams.RequestInfoList;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.RsrcInfo;
import com.azsoft.ecams.proto.ProtoEcams.RsrcInfoList;
import com.azsoft.ecams.proto.ProtoEcams.SRInfo;
import com.azsoft.ecams.proto.ProtoEcams.SRInfoList;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.dialog.ErrorMessageDlg;
import com.azsoft.ecams.util.checksum.CheckSum;
import com.azsoft.ecams.util.file.EFileToByteArray;
import com.google.protobuf.ByteString;

public class RegistFileNewJob extends WorkspaceJob {
	//private Logger logger = Logger.getLogger(this.getClass());
	private Hashtable<Integer, Hashtable<String, Object> > resources;
	private String errMsg;
	private List errList = new ArrayList();
	private List labelRefreshList = new ArrayList();

	public RegistFileNewJob(String name) {
		super(name);
	}

	public RegistFileNewJob(String name, Hashtable<Integer, Hashtable<String, Object> > resources) {
		super(name);
		this.resources = resources;
		addJobChangeListener(new CheckInRegAllJobChangeListener());
	}
	
	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor)	throws CoreException {
		int i;
		
		try{
			errList.clear();
			
			/* 싱크시 필요한 IResource형태의 데이터 */
			IResource[] selectResources = new IResource[resources.size()];
			for( int j=0; j<resources.size(); j++ ) {
				selectResources[j] = (IResource)resources.get(j).get("resource");
			}
			
			IEcamsStatus[] filestatuses = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatuses_regist(selectResources);
			
			if (filestatuses.length != resources.size()){
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
			SRInfoList.Builder srInfoList_builder = SRInfoList.newBuilder();
			RequestInfoList.Builder requestInfoList_builder = RequestInfoList.newBuilder();
			JobInfoList.Builder jobinfo_builder = JobInfoList.newBuilder();
			
			IProject project= ((IResource)resources.get(0).get("resource")).getProject();

			if(!project.isAccessible()){
				errMsg = "Poject is closed.";
				return Status.CANCEL_STATUS;
			}else{
				if(!EcamsRepositoryProvider.isManagedByEcams(project)){
					errMsg = "Is Not eCAMS Project.[ Connection Project -> Properties eCAMS PlugIn]";
					return Status.CANCEL_STATUS;
				}
			}
			
			
			for (i=0;i<filestatuses.length;i++){
				FileData.Builder fileData_builder = FileData.newBuilder();
				RequestInfo.Builder requestinfo_builder = RequestInfo.newBuilder();
				RsrcInfo.Builder rsrcinfo_builder = RsrcInfo.newBuilder();
				JobInfo.Builder jobinfo = JobInfo.newBuilder();

				if(filestatuses[i] == null){
					fileData_builder.setItemid("");
					fileData_builder.setStatus("");
					fileData_builder.setReqcd("04");
				}else{
					fileData_builder.setItemid(filestatuses[i].getItemid());
					fileData_builder.setStatus(filestatuses[i].getFileStatus());
					if (filestatuses[i].getFileStatus().equals("3")) fileData_builder.setReqcd("03");
					else fileData_builder.setReqcd("04");
				}
				rsrcinfo_builder.setRsrccd(resources.get(i).get("rsrccd").toString());
				fileData_builder.setRsrcinfo(rsrcinfo_builder);
				
				requestinfo_builder.setQrycd("");
				requestinfo_builder.setSayu(resources.get(i).get("story").toString());
				requestinfo_builder.setSvrYN("N");
				requestinfo_builder.setVersion("Y");
				requestInfoList_builder.addRequestinfo(requestinfo_builder);
				requestinfo_builder = null;
				
				fileData_builder.setFilename(resources.get(i).get("rsrcname").toString());
				PathInfo.Builder pathinfo_builder = PathInfo.newBuilder();
				pathinfo_builder.setRelativitePath(resources.get(i).get("dirpath").toString());
				//pathinfo_builder.setRelativitePath(((IResource)resources[i]).getProjectRelativePath().toString());
				fileData_builder.setPathinfo(pathinfo_builder.build());
				fileDataList_builder.addFiledatas(fileData_builder.build());
				fileData_builder=null;
				
				
				jobinfo.setJobcd(resources.get(i).get("jobcd").toString());
				jobinfo.setJobname(resources.get(i).get("jobname").toString());
				jobinfo_builder.addJobinfo(jobinfo);
				jobinfo = null;
				
				SRInfo.Builder srinfo_builder = SRInfo.newBuilder();
				
				String ccEditor = "";
				String ccSRId = "";
				String ccTitle = "";
				
				if( (null != resources.get(i).get("srid")) && !"".equals(resources.get(i).get("srid").toString()) ) {
					ccEditor = id;
					ccSRId = resources.get(i).get("srid").toString();
					ccTitle = resources.get(i).get("srtitle").toString();
				}
				
				srinfo_builder.setCcEditor(ccEditor);
				srinfo_builder.setCcSRId(ccSRId);
				srinfo_builder.setCcTitle(ccTitle);
				srInfoList_builder.addSrinfo(srinfo_builder.build());
				srinfo_builder = null;
			}
			
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(id);
			userinfo_builder.setPasswd(passwd);
			
			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
			sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
			sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);

			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setSrinfolist(srInfoList_builder.build());	
			
			builder_msg.setMsgtype("REGISTFILENEW"); //신규등록
			builder_msg.setRequestinfolist(requestInfoList_builder.build());
			builder_msg.setFiledatalist(fileDataList_builder.build());
			builder_msg.setUserinfo(userinfo_builder.build());
			builder_msg.setSysinfo(sysinfo_builder.build());
			builder_msg.setJobinfolist(jobinfo_builder.build());
			
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
					EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",selectResources,"","NONE"));
					errcnt = 1;
					return Status.CANCEL_STATUS;			
				}
			}
			
			EcamsProviderPlugin.getPlugin().getJobManager().addJob(new UpdateStatusJob("Resource Status Updating..",selectResources,""));
			return Status.OK_STATUS;
			
		}catch (CoreException e){
			e.printStackTrace();
			return Status.CANCEL_STATUS;
		}finally{
			monitor.done();
		}
	}
	
	
	final class CheckInRegAllJobChangeListener extends JobChangeAdapter {

		public void done(IJobChangeEvent event) {
			IStatus result = event.getResult();
			if(result.getSeverity() == IStatus.CANCEL) {
				if(!"".equals(errMsg)){
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageBox messageBox = new MessageBox(new Shell());
							messageBox.setMessage(errMsg);
							messageBox.setText("CheckInRegAll ERROR");
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
