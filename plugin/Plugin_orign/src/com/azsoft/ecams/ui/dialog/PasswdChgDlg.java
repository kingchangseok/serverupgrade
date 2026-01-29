package com.azsoft.ecams.ui.dialog;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;

public class PasswdChgDlg extends Dialog {
	private String ip,port,id;
	
	private Text txtpass;
	private Text txtnewpass;
	private Text txtpassok;
	
	public PasswdChgDlg(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @wbp.parser.constructor
	 */
	public PasswdChgDlg(Shell parentShell, String ip, String port, String id) {
		super(parentShell);
		this.ip = ip;
		this.port = port;
		this.id = id;
		// TODO Auto-generated constructor stub
	}	
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("\ube44\ubc00\ubc88\ud638 \ubcc0\uacbd");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//svrlist.clearAll();
		Button OKButton = super.createButton(parent, IDialogConstants.OK_ID,IDialogConstants.OK_LABEL, false);
		
		OKButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				//parentShell.dispose();
				
				if(txtnewpass.getText().length() < 8 || txtnewpass.getText().length() > 12){
					MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
					messageBox.setMessage("\ube44\ubc00\ubc88\ud638\ub294 8-12\uc790\ub9ac\ub85c \uc124\uc815\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
					messageBox.setText("ERROR");
					messageBox.open();
				}
				
				String pattern1 = "0123456789";
				String pattern2 = "abcdefghijklmnopqrstuvwxyz";
				String pattern3 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
				//String pattern4 = "-_=+\|()*&^%$#@!~`?></;,.:'";
				String pattern4 = "!\"#[$]%&\\(\\)\\{\\}@`[*]:[+];-.<>,\\^~|'\\[\\]";
				
				int encnt = 0;
				int numcnt = 0;
				int spcnt = 0;
				
				for (int i=0; i<txtnewpass.getText().length(); i++){
					//System.out.println(i+" "+txtnewpass.getText().substring(i,i+1));
					if (pattern1.indexOf(txtnewpass.getText().substring(i,i+1)) >= 0){
						numcnt++;
					}
					if (pattern2.indexOf(txtnewpass.getText().substring(i,i+1)) >= 0 || pattern3.indexOf(txtnewpass.getText().substring(i,i+1)) >= 0){
						encnt++;
					}
					if (pattern4.indexOf(txtnewpass.getText().substring(i,i+1)) >= 0){
						spcnt++;
					}
				}
				
				if(encnt == 0 || numcnt == 0 || spcnt == 0){
					MessageBox messageBox = new MessageBox(new Shell(), SWT.OK);
					messageBox.setMessage("\uc601\ubb38/\uc22b\uc790/\ud2b9\uc218\ubb38\uc790 \uc870\ud569\uc73c\ub85c \ub4f1\ub85d\uc774 \uac00\ub2a5\ud569\ub2c8\ub2e4.");
					messageBox.setText("ERROR");
					messageBox.open();
				}else{
					setHistoryList();
				}
			}
		});
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		//createTableViewer(parent);
		Composite container = new Composite(parent, SWT.NULL);

		Group group = new Group(container, SWT.NONE);
		group.setBounds(0, 0, 328, 205);
		
		Label lblId = new Label(group, SWT.NONE);
		lblId.setBounds(10, 40, 100, 16);
		lblId.setText("\uc0ac\ubc88");
		
		Text txtId = new Text(group, SWT.BORDER);
		txtId.setBounds(120, 40, 190, 19);
		txtId.setText(id);
		txtId.setEnabled(false);
		
		Label lblpass = new Label(group, SWT.NONE);
		lblpass.setBounds(10, 70, 100, 16);
		lblpass.setText("\uae30\uc874\ube44\ubc00\ubc88\ud638");
		
		txtpass = new Text(group, SWT.BORDER | SWT.PASSWORD);
		txtpass.setBounds(120, 70, 190, 19);
		
		Label lblhint = new Label(group, SWT.NONE);
		lblhint.setBounds(10, 100, 300, 16);
		lblhint.setText("\ucd08\uae30 \ube44\ubc00\ubc88\ud638\ub294 '1234'\uc785\ub2c8\ub2e4.");
		
		Label lblnewpass = new Label(group, SWT.NONE);
		lblnewpass.setBounds(10, 126, 100, 16);
		lblnewpass.setText("\uc0c8\ub85c\uc6b4\ube44\ubc00\ubc88\ud638");
		
		txtnewpass = new Text(group, SWT.BORDER | SWT.PASSWORD);
		txtnewpass.setBounds(120, 126, 190, 19);
		
		Label lblpassok = new Label(group, SWT.NONE);
		lblpassok.setBounds(10, 156, 100, 16);
		lblpassok.setText("\ube44\ubc00\ubc88\ud638\ud655\uc778");
		
		txtpassok = new Text(group, SWT.BORDER | SWT.PASSWORD);
		txtpassok.setBounds(120, 156, 190, 19);
		
		return container;
	}
	
	public void setHistoryList(){
		try{
			if(txtnewpass.getText() == null || txtnewpass.getText().equals("") ||
					txtpassok.getText() == null || txtpassok.getText().equals("") ||
					txtpass.getText() == null || txtpass.getText().equals("")){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\ube44\ubc00\ubc88\ud638\ub97c \uc785\ub825\ud574 \uc8fc\uc2ed\uc2dc\uc624.");
				messageBox.open();
			}else if(txtnewpass.getText().equals(txtpass.getText())){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\uae30\uc874\ube44\ubc00\ubc88\ud638\uc640 \uc0c8\ub85c\uc6b4 \ube44\ubc00\ubc88\ud638\uac00 \uac19\uc2b5\ub2c8\ub2e4.");
				messageBox.open();
			}else if(!txtnewpass.getText().equals(txtpassok.getText())){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\ubcc0\uacbd\ube44\ubc00\ubc88\ud638\uac00 \uc77c\uce58\ud558\uc9c0\uc54a\uc2b5\ub2c8\ub2e4.");
				messageBox.open();
			}else{
				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("PASSWD_CHECK");
				UserInfo.Builder userInfo_builder = UserInfo.newBuilder();
				userInfo_builder.setId(id);
				userInfo_builder.setPasswd(txtpass.getText());
				userInfo_builder.setNewPasswd(txtnewpass.getText());
				builder_msg.setUserinfo(userInfo_builder.build());	
			
				EcamsClient syncClient = new EcamsClient(ip,port);
				ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
				
				if(returnMsg.getReturnval() == 0){
					MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
					messageBox.setMessage("\ube44\ubc00\ubc88\ud638\uac00 \ubcc0\uacbd\ub418\uc5c8\uc2b5\ub2c8\ub2e4. \n\uc0c8\ub85c\uc6b4 \ube44\ubc00\ubc88\ud638\ub85c \ub85c\uadf8\uc778\ud14c\uc2a4\ud2b8\ub97c \uc9c4\ud589\ud574 \uc8fc\uc2ed\uc2dc\uc624.");
					if (messageBox.open() == SWT.OK){
						close();
					}
				}else{
					MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
					messageBox.setMessage(returnMsg.getReturnStr());
					messageBox.open();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}