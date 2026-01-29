package com.azsoft.ecams.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.azsoft.ecams.core.listeners.FileOpenCheckListener;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.properties.WorkspacePreferences;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.dialog.ConfirmToolDlg;
import com.azsoft.ecams.ui.view.ServiceRequestView;

public class EcamsEaryStarter implements IStartup {

	public void earlyStartup() {
		// TODO Auto-generated method stub
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				//Meta DB관리
				/*
				try {
					Connection conn = EcamsLocalDBConn.getConnection();
					if(conn == null){
						MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
						messageBox.setMessage("eClipse를 재기동 해주십시오.");
						messageBox.setText("DB Connection Failed");
						messageBox.open();
						return;
					}
					
					String errMsg = "";
					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
					for(int i=0; i<projects.length; i++){
						if(!EcamsDBInitialize.CreateTable(conn, projects[i])){
							errMsg = "eClipse를 재기동 해주십시오.";
							break;
						}
					}
					conn.close();
					conn = null;

					if(!"".equals(errMsg)){
						MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
						messageBox.setMessage(errMsg);
						messageBox.setText("DB Creation Failed");
						messageBox.open();
						return;
					}
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				*/
				//EcamsSyncMgr syncMgr = new EcamsSyncMgr();
				String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
				String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
				String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
				String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
				String tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
				
				// 20230405
//				if("".equals(tool) || null == tool){
//					ConfirmToolDlg cfmDlg = new ConfirmToolDlg(new Shell());
//					cfmDlg.open();
//					if (cfmDlg.getReturnCode() != ConfirmToolDlg.OK) {
//						WorkspacePreferences workPrefer = new WorkspacePreferences();
//						workPrefer.putString(IProperty.TOOL,"I");
//						workPrefer.save();
//						
//						MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
//						messageBox.setMessage("iStudio \ub85c \uae30\ubcf8\uc124\uc815 \ub418\uc5c8\uc2b5\ub2c8\ub2e4.\n" +
//								"[Window -> Preference -> eCAMS Plugin]\uc5d0\uc11c \ubcc0\uacbd\ud558\uc2e4 \uc218 \uc788\uc2b5\ub2c8\ub2e4.");
//						messageBox.setText("\ud655\uc778");
//						messageBox.open();
//					}
//					
//				}
//				tool = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.TOOL, "", null);
				tool = "E";
				
				if((null == ip || "".equals(ip)) || (null == port || "".equals(port))
						|| (null == id || "".equals(id)) || (null == passwd || "".equals(passwd))){
					java.lang.System.setProperty("isAdmin", "0");
				}else{
					EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
					builder_msg.setMsgtype("ADMIN");
	
					UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
					userinfo_builder.setId(id);
					userinfo_builder.setPasswd(passwd);
					
					builder_msg.setUserinfo(userinfo_builder.build());
	
					EcamsClient syncClient = new EcamsClient(ip,port);
					ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
					

					if (!returnMsg.getReturnStr().startsWith("SOCKERR")){
						if( 0 == returnMsg.getReturnval() ){
							if( "1".equals(returnMsg.getReturnStr()) ) {
								java.lang.System.setProperty("isAdmin", "1");
							} else {
								java.lang.System.setProperty("isAdmin", "0");
							}
						}else {
							java.lang.System.setProperty("isAdmin", "0");
						}
						
						if("I".equals(tool)){
							Map<String, String> parameter = new HashMap<String, String>(); 
							parameter.put("USER_ID", id);
							try {
								CommandExecuter.executeCommand("command.team.DoneLogin", parameter);
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} finally {
								parameter = null;
							}
						}
						
						FileOpenCheckListener fileOpenCheckListener = new FileOpenCheckListener();
						IWorkbenchPage aPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						
						if (aPage != null){
							aPage.addPartListener(fileOpenCheckListener);
							
							try{
								aPage.showView("com.azsoft.ecams.ui.view.servicerequestview",null,IWorkbenchPage.VIEW_ACTIVATE);
								
								IViewReference viewReference = aPage.findViewReference("com.azsoft.ecams.ui.view.servicerequestview");
								if (viewReference != null) {
									IViewPart view = viewReference.getView(true);
									ServiceRequestView showview = (ServiceRequestView) view;
									showview.setFocus();
								}
								
								//IViewReference viewReference2 = aPage.findViewReference("com.azsoft.ecams.ui.view.resourceview");
								//aPage.hideView(viewReference2);
							} catch (PartInitException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}else{
						java.lang.System.setProperty("isAdmin", "0");
						
						MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
						messageBox.setMessage("Connection refused: no Further information");
						messageBox.setText("ERROR");
						messageBox.open();
					}
				}
			}
		});	
	}

}
