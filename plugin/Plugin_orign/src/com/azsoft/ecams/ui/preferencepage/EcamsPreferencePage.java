
package com.azsoft.ecams.ui.preferencepage;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.properties.WorkspacePreferences;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.dialog.PasswdChgDlg;
import com.swtdesigner.SWTResourceManager;

public class EcamsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private Logger logger = Logger.getLogger(this.getClass());
	
	private Text ip_txt;
	private Text port_txt;
	private Text id_txt;
	private Text passwd_txt;	
//	private CCombo tool_combo;	
	
	private Listener m_modifyListener;
	private Listener connectTest_Listener;
	private Listener loginTest_Listener;
	private Listener pass_Listener;
	
	protected WorkspacePreferences m_store;

	
	/**
	 * Create the preference page.
	 */
	public EcamsPreferencePage() {
		setDescription("\ud615\uc0c1\uad00\ub9ac\uc124\uc815");
		setTitle("eCAMS PlugIn");		
	}
	
	
	public void init(IWorkbench workbench) {
		// Initialize the preference page
	}	

	/**
	 * Create contents of the preference page.
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
		m_store = new WorkspacePreferences();
		
		setupCallbacks();
		
		Composite container = new Composite(parent, SWT.NULL);
		
		// 20230405
//		Label tool_lb = new Label(container, SWT.NONE);
//		tool_lb.setBounds(10, 13, 90, 16);
//		tool_lb.setText("Tool \uad6c\ubd84");
//
//		tool_combo = new CCombo(container, SWT.BORDER);
//		tool_combo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
//		tool_combo.setBounds(106, 10, 210, 23);
//		tool_combo.setEnabled(true);
//		tool_combo.setEditable(false);
//		tool_combo.add("iStudio");
//		tool_combo.add("eClipse");
		
		Label ip_lb = new Label(container, SWT.NONE);
		ip_lb.setBounds(10, 13, 90, 16);
		ip_lb.setText("\ud615\uc0c1\uad00\ub9ac\uc544\uc774\ud53c");

		ip_txt = new Text(container, SWT.BORDER);
		ip_txt.setBounds(106, 10, 210, 23);
		ip_txt.addListener(SWT.Modify, m_modifyListener);
		
		Label port_lb = new Label(container, SWT.NONE);
		port_lb.setBounds(10, 45, 90, 16);
		port_lb.setText("\ud615\uc0c1\uad00\ub9ac\ud3ec\ud2b8");
		
	
		port_txt = new Text(container, SWT.BORDER);
		port_txt.setBounds(106, 42, 110, 19);
		port_txt.addListener(SWT.Modify, m_modifyListener);
		
		
		Button con_bt = new Button(container, SWT.NONE);
		con_bt.setBounds(232, 45, 84, 23);
		con_bt.setText("\uc811\uc18d\ud655\uc778");
		con_bt.addListener(SWT.MouseDown,connectTest_Listener);
		
		Label label = new Label(container, SWT.NONE);
		label.setText("\uc544\uc774\ub514(\uc0ac\ubc88)");
		label.setBounds(10, 74, 90, 16);
		
		id_txt = new Text(container, SWT.BORDER);
		id_txt.setText("");
		id_txt.setBounds(106, 75, 110, 19);
		
		passwd_txt = new Text(container, SWT.BORDER | SWT.PASSWORD);
		passwd_txt.setText("");
		passwd_txt.setBounds(106, 106, 110, 19);
		
		Label label_1 = new Label(container, SWT.NONE);
		label_1.setText("\ube44\ubc00\ubc88\ud638");
		label_1.setBounds(10, 109, 90, 16);
		
		Button login_bt = new Button(container, SWT.NONE);
		login_bt.setText("\ub85c\uadf8\uc778\ud14c\uc2a4\ud2b8");
		login_bt.setBounds(232, 109, 84, 23);
		login_bt.addListener(SWT.MouseDown,loginTest_Listener);
		
		Button pass_bt = new Button(container, SWT.NONE);
		pass_bt.setText("\ube44\ubc00\ubc88\ud638\ubcc0\uacbd");
		pass_bt.setBounds(232, 135, 84, 23);
		pass_bt.addListener(SWT.MouseDown,pass_Listener);
		//pass_bt.setVisible(false);
		updateInterface();
		
		return container;
	}

	public boolean performOk()
	{
		if(passwd_txt.getText() == null || passwd_txt.getText().equals("") ||
			id_txt.getText() == null || id_txt.getText().equals("") || 
			ip_txt.getText() == null || ip_txt.getText().equals("") || 
			port_txt.getText() == null || port_txt.getText().equals("")){
			MessageBox messageBox =  new MessageBox(this.getShell(), SWT.OK);
			messageBox.setMessage("IP, PORT, \uc544\uc774\ub514(\uc0ac\ubc88), \ube44\ubc00\ubc88\ud638\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			
			return false;
		}
		
		if(passwd_txt.getText().equals("1234")){
			MessageBox messageBox =  new MessageBox(this.getShell(), SWT.OK);
			messageBox.setMessage("\ube44\ubc00\ubc88\ud638\ub97c \ubcc0\uacbd\ud558\uc2dc\uae30 \ubc14\ub78d\ub2c8\ub2e4.");
			messageBox.open();
			
			return false;
		}
		
		if(passwd_txt.getText().length() < 8 || passwd_txt.getText().length() > 12){
			MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
			messageBox.setMessage("\ube44\ubc00\ubc88\ud638\ub294 8-12\uc790\ub9ac\ub85c \uc124\uc815\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
			messageBox.setText("ERROR");
			messageBox.open();
			return false;
		}
		
		//	store all the preferences
		EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
		builder_msg.setMsgtype("GET_USERNAME");
		UserInfo.Builder userInfo_builder = UserInfo.newBuilder();
		userInfo_builder.setId(id_txt.getText());
		userInfo_builder.setPasswd(passwd_txt.getText());
		builder_msg.setUserinfo(userInfo_builder.build());			
		
		EcamsClient ecamsclient = new EcamsClient(ip_txt.getText(),port_txt.getText());
		ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
		
		if (!returnMsg.getReturnStr().startsWith("SOCKERR")){
			if (returnMsg.getReturnval() == 0){
				m_store.putString(IProperty.NAME,returnMsg.getReturnStr());
				close();
			}else{
				return false;
			}
		}else{
			MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
			messageBox.setMessage("Connection refused: no Further information");
			messageBox.setText("ERROR");
			messageBox.open();
			return false;
		}
		/*
		if(close() == false){
		}
		*/
		return super.performOk();		
	}
	
	public boolean close(){
		//boolean modify = compare();
		
		//if(modify == false) 
		updateStore();
		
		return true;
	}	
	
	
	public void updateStore()
	{
		logger.debug("UI::updateStore()");
		try{
			m_store.putString(IProperty.IP,ip_txt.getText());
			m_store.putString(IProperty.PORT,port_txt.getText());
			m_store.putString(IProperty.ID,id_txt.getText());
			m_store.putString(IProperty.PASSWD,passwd_txt.getText());
//			if(tool_combo.getSelectionIndex() == 0){
//				m_store.putString(IProperty.TOOL,"I");
//			}else{
//				m_store.putString(IProperty.TOOL,"E");
//			}
			m_store.putString(IProperty.TOOL,"E"); // 20230405
			m_store.save();
			
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("ADMIN");

			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(id_txt.getText());
			userinfo_builder.setPasswd(passwd_txt.getText());
			
			builder_msg.setUserinfo(userinfo_builder.build());

			EcamsClient syncClient = new EcamsClient(ip_txt.getText(), port_txt.getText());
			ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
			
			if( 0 == returnMsg.getReturnval() ){
				if( "1".equals(returnMsg.getReturnStr()) ) {
					java.lang.System.setProperty("isAdmin", "1");
				} else {
					java.lang.System.setProperty("isAdmin", "0");
				}
			}else {
				java.lang.System.setProperty("isAdmin", "0");
			}
			//System.out.println("settings " + java.lang.System.getProperty("isAdmin"));
			
		}catch(NullPointerException e){
			logger.error("Caught exception saving preferences, this really shouldnt happen");
			logger.error("Exception was from here: "+e.getMessage());
			logger.error("The cause was: "+e.getCause());
		} //	Maybe one of these values is NULL??
	}

	public void performDefaults()
	{
		m_store.restoreDefaults();
		updateInterface();
	}

	public void updateInterface(){
		if(compare() == false){
			ip_txt.setText(m_store.getString(IProperty.IP));
			port_txt.setText(m_store.getString(IProperty.PORT));
			id_txt.setText(m_store.getString(IProperty.ID));
			passwd_txt.setText(m_store.getString(IProperty.PASSWD));
//			if(m_store.getString(IProperty.TOOL).equals("I")){ // 20230405
//				tool_combo.select(0);
//			}else{
//				tool_combo.select(1);
//			}
			m_store.save();
		}else logger.error("Compare(), returned true");
	}

	public void setupCallbacks()
	{
		m_modifyListener = new Listener(){
			public void handleEvent(Event e){
				if(e.type == SWT.Modify){
					if((m_store.getBoolean(IProperty.VERIFIED) == true)  && (compare() == false)){
						//logger.error("Widget was modified");
						m_store.putBoolean(IProperty.VERIFIED, false);
					}
				}
			}
		};
		
		connectTest_Listener = new Listener(){
			public void handleEvent(Event e){
				if(e.type == SWT.MouseDown){
					try{
						EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
						builder_msg.setMsgtype("CONNECT_TEST");
						EcamsClient ecamsclient = new EcamsClient(ip_txt.getText(),port_txt.getText());
						ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
						
						MessageBox messageBox =  new MessageBox(e.display.getActiveShell(), SWT.OK); 
						messageBox.setMessage(returnMsg.getReturnStr());
						messageBox.setText("\ub85c\uadf8\uc778\ud14c\uc2a4\ud2b8");
						messageBox.open();
					}catch(Exception exception){
						logger.error(exception.getMessage());
					}
				}
			}
		};
		
		loginTest_Listener = new Listener(){
			public void handleEvent(Event e){
				if(e.type == SWT.MouseDown){
					try{
						EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
						builder_msg.setMsgtype("LOGIN_CHECK");
						UserInfo.Builder userInfo_builder = UserInfo.newBuilder();
						userInfo_builder.setId(id_txt.getText());
						userInfo_builder.setPasswd(passwd_txt.getText());
						builder_msg.setUserinfo(userInfo_builder.build());						
						EcamsClient ecamsclient = new EcamsClient(ip_txt.getText(),port_txt.getText());
						ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
						
						MessageBox messageBox =  new MessageBox(e.display.getActiveShell(), SWT.OK); 
						messageBox.setMessage(returnMsg.getReturnStr());
						messageBox.setText("\ub85c\uadf8\uc778\ud14c\uc2a4\ud2b8");
						messageBox.open();
					}catch(Exception exception){
						logger.error(exception.getMessage());
					}
				}
			}
		};
		
		pass_Listener = new Listener(){
			public void handleEvent(Event e) {
				if(e.type == SWT.MouseDown){
					try{
						if(id_txt.getText() == null || id_txt.getText().equals("") || 
								ip_txt.getText() == null || ip_txt.getText().equals("") || 
								port_txt.getText() == null || port_txt.getText().equals("")){
							MessageBox messageBox =  new MessageBox(e.display.getActiveShell(), SWT.OK);
							messageBox.setMessage("IP, PORT, \uc0ac\ubc88\uc744 \uc785\ub825\ud558\uc138\uc694");
							messageBox.open();
						}else{
							PasswdChgDlg passwdchgdlg = new PasswdChgDlg(e.display.getActiveShell(),ip_txt.getText(),port_txt.getText(),id_txt.getText());
							passwdchgdlg.open();
							
							//System.out.println("getReturnCode:"+passwdchgdlg.getReturnCode());
							//System.out.println("OK:"+passwdchgdlg.OK);
							//System.out.println("CANCEL:"+passwdchgdlg.CANCEL);
							if(passwdchgdlg.getReturnCode() == PasswdChgDlg.OK){
								passwd_txt.setText("");
							}
						}
					}catch(Exception exception){
						logger.error(exception.getMessage());
					}
				}
			}
		};
	}

	public boolean compare(){
		try{
			if(!m_store.getString(IProperty.IP).equals(ip_txt.getText()) ) return false;
			if(!m_store.getString(IProperty.PORT).equals( port_txt.getText()) ) return false;
			if(!m_store.getString(IProperty.ID).equals( id_txt.getText()) ) return false;
			if(!m_store.getString(IProperty.PASSWD).equals(passwd_txt.getText())) return false;
			// 20230405
//			if(m_store.getString(IProperty.TOOL).equals("I") && tool_combo.getSelectionIndex() != 0) return false;
//			if(m_store.getString(IProperty.TOOL).equals("E") && tool_combo.getSelectionIndex() != 1) return false;
		}catch(NullPointerException e){
			logger.error("Caught exception comparing preferences, this really shouldnt happen");
			logger.error("Exception was from here: "+e.getMessage());
			logger.error("The cause was: "+e.getCause());
			return false;
		}
		//logger.error("UserInterface::compare(), nothing changed");
	
		return true;
	}
}
