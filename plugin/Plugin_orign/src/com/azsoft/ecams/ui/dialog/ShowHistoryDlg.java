package com.azsoft.ecams.ui.dialog;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;


public class ShowHistoryDlg extends Dialog {
	private IResource[] inputResource;
	private String filename,itemid;
	private Table svrlist;
	
	private String ip,port,id,passwd;
	private TableColumn column1;
	private TableColumn column2;
	private TableColumn column3;
	private TableColumn column4;
	private TableColumn column5;
	private TableColumn column6;
	private TableColumn column7;
	
	public ShowHistoryDlg(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @wbp.parser.constructor
	 */
	public ShowHistoryDlg(Shell parentShell, IResource[] inputResoruce, String file, String item) {
		super(parentShell);
		setInputResource(inputResoruce);
		this.filename = file;
		this.itemid = item;
		// TODO Auto-generated constructor stub
	}	
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("["+filename+"]\uc758 \ud788\uc2a4\ud1a0\ub9ac");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//svrlist.clearAll();
		createButton(parent, IDialogConstants.OK_ID,IDialogConstants.OK_LABEL, false);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		//createTableViewer(parent);
		Composite container = new Composite(parent, SWT.NULL);

		svrlist = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		svrlist.setBounds(10, 10, 1000, 500);
		svrlist.setHeaderVisible(true);
		svrlist.setLinesVisible(true);
		
		column1 = new TableColumn(svrlist, SWT.NONE);
        column1.setText("\uc2e0\uccad\ubc88\ud638"); 
        column1.setWidth(120);
        column1.setResizable(true);
        column1.setMoveable(true);
        column1.setAlignment(SWT.CENTER);
        
        column2 = new TableColumn(svrlist, SWT.NONE);
        column2.setText("\uc2e0\uccad\uc77c\uc2dc"); 
        column2.setWidth(150);
        column2.setResizable(true);
        column2.setMoveable(true);
        column2.setAlignment(SWT.CENTER);
        
        column3 = new TableColumn(svrlist, SWT.NONE);
        column3.setText("\uc2e0\uccad\uc790"); 
        column3.setWidth(100);
        column3.setResizable(true);
        column3.setMoveable(true);
        column3.setAlignment(SWT.CENTER);
        
        column4 = new TableColumn(svrlist, SWT.NONE);
        column4.setText("\uc2e0\uccad\uad6c\ubd84"); 
        column4.setWidth(120);
        column4.setResizable(true);
        column4.setMoveable(true);
        column4.setAlignment(SWT.CENTER);
        
        column5 = new TableColumn(svrlist, SWT.NONE);
        column5.setText("\uc644\ub8cc\uc77c\uc2dc"); 
        column5.setWidth(150);
        column5.setResizable(true);
        column5.setMoveable(true);
        column5.setAlignment(SWT.CENTER);
        
        column7 = new TableColumn(svrlist, SWT.NONE);
        column7.setText("\ubc84\uc804"); 
        column7.setWidth(80);
        column7.setResizable(true);
        column7.setMoveable(true);
        column7.setAlignment(SWT.CENTER);
        
        column6 = new TableColumn(svrlist, SWT.NONE);
        column6.setText("\ubcc0\uacbd\uc0ac\uc720"); 
        column6.setWidth(200);
        column6.setResizable(true);
        column6.setMoveable(true);
		
        setHistoryList();
		
		return container;
	}
	
	public void setHistoryList(){
		ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
		passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
		
		
		if(itemid.length() > 0){
			try{
				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("HISTORYLIST_GET");
				
				FileData.Builder filedata_builder = FileData.newBuilder();
				
				filedata_builder.setItemid(itemid);
				filedata_builder.setFilename(filename);
				
				builder_msg.setFiledata(filedata_builder.build());
				
				UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
		
				userinfo_builder.setId(id);
				userinfo_builder.setPasswd(passwd);
				
				builder_msg.setUserinfo(userinfo_builder.build());
			
				EcamsClient syncClient = new EcamsClient(ip,port);
				ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
				
				String acptno = "";
				if(returnMsg.getReturnval() == 0){
					for(int i=0;i<returnMsg.getEcamsmsg().getHistorylist().getHistorydataCount();i++){
						TableItem item = new TableItem(svrlist, SWT.NONE);
						int c = 0;
						acptno = returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrAcptno();
						item.setText(c++, acptno.substring(0,4)+"-"+acptno.substring(4,6)+"-"+acptno.substring(6));
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrAcptdate());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCmUsername());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCmCodename());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrPrcdate());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrVersion());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrEditcon());
					}
				}else{
					MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
					messageBox.setMessage("\ud788\uc2a4\ud1a0\ub9ac\uac00 \uc5c6\uc2b5\ub2c8\ub2e4");
					if (messageBox.open() == SWT.OK){
						this.close();
					}
					
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			return;
		}
	}

	public void setInputResource(IResource[] inputResource){
		this.inputResource = inputResource;
	}
}
