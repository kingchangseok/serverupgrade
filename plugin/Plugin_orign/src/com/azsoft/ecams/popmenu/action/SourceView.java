package com.azsoft.ecams.popmenu.action;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.PathInfo;
import com.azsoft.ecams.util.checksum.CheckSum;
import com.azsoft.ecams.util.file.EFileToByteArray;
import com.google.protobuf.ByteString;
import com.azsoft.ecams.socket.EcamsClient;
import org.eclipse.core.runtime.Platform;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;

public class SourceView implements IObjectActionDelegate {
	private Shell shell;
	private String filename,itemid,userid;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		shell = targetPart.getSite().getShell();
	}
	
	public void run(IAction action) {
		List selectedList = new ArrayList();
		
		ObjectPluginAction myAction;
		ISelection mySelection;
		if (action instanceof ObjectPluginAction){
			myAction = (ObjectPluginAction) action;
		}
		else{
			return;
		}
		
		mySelection = myAction.getSelection();
		int cnt = 0;
		if (mySelection instanceof IStructuredSelection){
			for (Iterator it =((IStructuredSelection) mySelection).iterator();it.hasNext();){
				Object select_obj = it.next();
				if (select_obj != null && select_obj instanceof IResource){
					if(((IResource)select_obj).getType()==IResource.FILE){
						selectedList.add((IResource)select_obj);
					}
					if (((IResource)select_obj).getType()==IResource.FOLDER || ((IResource)select_obj).getType()==IResource.PROJECT){
						cnt++;
					}
				}
			}
		}
		
		if(cnt>0){
			MessageBox messageBox = new MessageBox(shell);
			messageBox.setMessage("\ud30c\uc77c\uc744 \uc120\ud0dd\ud558\uc138\uc694");
			messageBox.setText("\ud30c\uc77c\uc120\ud0dd");
			messageBox.open();
			return;
		}
		
		IResource[] selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
		
		IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]);
		
		if (resourceStatus == null){
			MessageBox messageBox = new MessageBox(shell);
			messageBox.setMessage("\ud788\uc2a4\ud1a0\ub9ac\uac00\uc5c6\uc2b5\ub2c8\ub2e4.");
			messageBox.setText("Source View");
			messageBox.open();
			return;
		}
		
		if(resourceStatus.getLastVer() == 0){
			MessageBox messageBox = new MessageBox(shell);
			messageBox.setMessage("\uc2e0\uaddc\ud30c\uc77c\uc785\ub2c8\ub2e4.");
			messageBox.setText("Source View");
			messageBox.open();
			return;
		}
		
		if(selectedList.size() == 1){
			filename = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]).getName();
			itemid = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]).getItemid().toString();
			userid = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
			
			byte[] testbyte = null;
			try {
				FileChannel inChannel = new FileInputStream(EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]).getFile() ).getChannel();
				int size = (int)inChannel.size();
				if(size<1){
					throw new IOException("Error");
				}
				testbyte = EFileToByteArray.FileToByteArray(EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]).getFile());
			} catch (IOException e) {
				MessageBox messageBox = new MessageBox(shell);
				messageBox.setMessage(filename+" \ud30c\uc77c \uc77d\uae30 \uc2e4\ud328(Size:0).");
				messageBox.setText("Source View");
				messageBox.open();
				return;
			}
			
			String md5sum = CheckSum.MD5SumVal(testbyte);
			String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
						
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();	
			FileData.Builder fileData_builder = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]).toFileData().toBuilder();
			fileData_builder.setMd5Sum(md5sum);
			fileData_builder.setItemid(itemid);
			fileData_builder.setFilename(userid + "_"+itemid);
			fileData_builder.setFilebytes(ByteString.copyFrom(testbyte));
			PathInfo.Builder pathinfo = PathInfo.newBuilder();
			pathinfo.setRelativitePath("99");
			fileData_builder.setPathinfo(pathinfo.build());
			
			builder_msg = EcamsMessage.newBuilder();
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null));
			userinfo_builder.setPasswd(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null));
			
			builder_msg.setMsgtype("FILETRANS_MAKE");
			builder_msg.setFiledata(fileData_builder.build());
			builder_msg.setUserinfo(userinfo_builder.build());
			EcamsClient ecamsclient = new EcamsClient(ip,port);
			ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
			
			if (returnMsg.getReturnval() != 0) {
				MessageBox messageBox = new MessageBox(shell);
				messageBox.setMessage("\uc18c\uc2a4\ubcf4\uae30\ub97c \uc704\ud55c \ud30c\uc77c\uc804\uc1a1\uc5d0 \uc2e4\ud328\ud558\uc600\uc2b5\ub2c8\ub2e4.");
				messageBox.setText("Source View");
				messageBox.open();
				return;
			}
			
			Program.launch("http://"+ip+":8080/ecams_win_flex/eCmr5300.jsp?UserId="+userid+"&RsrcName="+itemid+"&ReqNo=D");
			//Program.launch("http://10.253.20.63:4040/ecams_win_flex/eCmr5300.jsp?UserId="+userid+"&RsrcName="+itemid+"&ReqNo=D");
			
		}
	}


	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}



}