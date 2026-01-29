package com.azsoft.ecams.ui.wizard.page;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;

public class BefJobSelectPage extends WizardPage  {

	private Object[] selectedResources;
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
	
	public BefJobSelectPage() {
		super("wizardPage");
		setTitle("\uc6b4\uc601\uccb4\ud06c\uc778 \uc815\ubcf4\uc785\ub825");
		setDescription("\uc2e0\uccad\uc11c\ub97c \uc791\uc131\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
	}
	/** 앞페이지에서 이어지는 곳
	 * @param selectedResource : 선택한 항목들
	 */
	public void setSelectedResources(Object[] selectedResource) {
		// TODO Auto-generated method stub
		this.selectedResources = selectedResources;
		setTitle("\uc6b4\uc601\uccb4\ud06c\uc778 \uc815\ubcf4\uc785\ub825");
		setDescription("\uc2e0\uccad\uc11c\ub97c \uc791\uc131\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
	}
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		Composite container = new Composite(parent, SWT.NULL);

		svrlist = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		svrlist.setBounds(10, 10, 700, 170);
		svrlist.setHeaderVisible(true);
		svrlist.setLinesVisible(true);
		
		column1 = new TableColumn(svrlist, SWT.NONE);
        column1.setText("\uc2e0\uccad\ubc88\ud638"); 
        column1.setWidth(120);
        column1.setResizable(true);
        column1.setMoveable(true);
        
        column2 = new TableColumn(svrlist, SWT.NONE);
        column2.setText("\uc2e0\uccad\uc77c\uc2dc"); 
        column2.setWidth(150);
        column2.setResizable(true);
        column2.setMoveable(true);
        
        column3 = new TableColumn(svrlist, SWT.NONE);
        column3.setText("\uc2e0\uccad\uc790"); 
        column3.setWidth(120);
        column3.setResizable(true);
        column3.setMoveable(true);
        
        column4 = new TableColumn(svrlist, SWT.NONE);
        column4.setText("\uc2e0\uccad\uad6c\ubd84"); 
        column4.setWidth(80);
        column4.setResizable(true);
        column4.setMoveable(true);
        
        column5 = new TableColumn(svrlist, SWT.NONE);
        column5.setText("\uc644\ub8cc\uc77c\uc2dc"); 
        column5.setWidth(150);
        column5.setResizable(true);
        column5.setMoveable(true);
        
        column6 = new TableColumn(svrlist, SWT.NONE);
        column6.setText("\ubcc0\uacbd\uc0ac\uc720"); 
        column6.setWidth(100);
        column6.setResizable(true);
        column6.setMoveable(true);
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
				
				if(returnMsg.getReturnval() == 0){
					for(int i=0;i<returnMsg.getEcamsmsg().getHistorylist().getHistorydataCount();i++){
						TableItem item = new TableItem(svrlist, SWT.NONE);
						int c = 0;
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrAcptno());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrAcptdate());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCmUsername());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCmCodename());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrPrcdate());
						item.setText(c++, returnMsg.getEcamsmsg().getHistorylist().getHistorydata(i).getCrEditcon());
					}
				}else{
					MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
					messageBox.setMessage("\ud788\uc2a4\ud1a0\ub9ac\uac00 \uc5c6\uc2b5\ub2c8\ub2e4");
					if (messageBox.open() == SWT.OK){
						//this.close();
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
