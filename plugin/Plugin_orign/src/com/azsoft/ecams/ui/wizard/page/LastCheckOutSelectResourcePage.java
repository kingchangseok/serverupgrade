package com.azsoft.ecams.ui.wizard.page;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.FileDataList;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;
import com.swtdesigner.SWTResourceManager;

import org.eclipse.swt.widgets.Label;

public class LastCheckOutSelectResourcePage extends WizardPage{
	private Logger logger = Logger.getLogger(this.getClass());
	private IResource[] resources;
	ReturnMsg returnMsg;
	public FileDataList cboVersionSel = null;
	
	private IDialogSettings settings;
	private ResourceSelectionTree resourceSelectionTree;
	private Object[] selectedResources;
	private String itemid, filename, ip, port, id, passwd;
	private Table svrlist;
	private TableColumn column1;
	private TableColumn column2;
	private TableColumn column3;
	private TableColumn column4;
	
	private StyledText text;
	/**
	 * Create the wizard.
	 */
	public LastCheckOutSelectResourcePage(IResource[] resources, String itemid, String filename, String ip, String port, String id, String passwd) {
		super("wizardPage");
		this.resources = resources;
		this.itemid = itemid;
		this.filename = filename;
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.passwd = passwd;
		settings = EcamsProviderPlugin.getPlugin().getDialogSettings();
		setPageComplete(false);
		setTitle("\ud30c\uc77c\uc120\ud0dd");
		setDescription("\uccb4\ud06c\uc544\uc6c3\ud558\uc2e4 \ud30c\uc77c\uc744 \uc120\ud0dd\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		//setupCallbacks();
		
		Composite container = new Composite(parent, SWT.NULL);

		resourceSelectionTree = new ResourceSelectionTree(container, SWT.NONE, resources, true);
		resourceSelectionTree.setLocation(10, 245);
		resourceSelectionTree.setSize(558, 65);
		resourceSelectionTree.setEnabled(false);

		Label label_2 = new Label(container, SWT.NONE);
		label_2.setText("\uc2e0\uccad\uc0ac\uc720:");
		label_2.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label_2.setBounds(10, 201, 61, 12);
		
		text = new StyledText(container, SWT.BORDER);
		text.setBounds(77, 200, 489, 39);
		
		resourceSelectionTree.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selectedResources = resourceSelectionTree.getSelectedResources();
			}
		});
		
		((CheckboxTreeViewer)resourceSelectionTree.getTreeViewer()).addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				selectedResources = resourceSelectionTree.getSelectedResources();
			}
		});
	

		selectedResources = resourceSelectionTree.getSelectedResources();
		setPageComplete(true);
		
		svrlist = new Table(container, SWT.BORDER | SWT.FULL_SELECTION);
		svrlist.setBounds(7, 10, 561, 184);
		svrlist.setHeaderVisible(true);
		svrlist.setLinesVisible(true);
		
		column1 = new TableColumn(svrlist, SWT.NONE);
        column1.setText("\uc2e0\uccad\uc790"); 
        column1.setWidth(80);
        column1.setResizable(true);
        column1.setMoveable(true);
        
        column2 = new TableColumn(svrlist, SWT.NONE);
        column2.setText("\uc2e0\uccad\uc77c\uc2dc"); 
        column2.setWidth(150);
        column2.setResizable(true);
        column2.setMoveable(true);
        
        column3 = new TableColumn(svrlist, SWT.NONE);
        column3.setText("\ubc84\uc804"); 
        column3.setWidth(80);
        column3.setResizable(true);
        column3.setMoveable(true);
        
        column4 = new TableColumn(svrlist, SWT.NONE);
        column4.setText("\uc2e0\uccad\uc0ac\uc720"); 
        column4.setWidth(200);
        column4.setResizable(true);
        column4.setMoveable(true);
        
		
		if(itemid.length() > 0){
			try{
				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("GET_LASTVERSION");
				
				FileData.Builder filedata_builder = FileData.newBuilder();
				
				filedata_builder.setItemid(itemid);
				filedata_builder.setFilename(filename);
				
				builder_msg.setFiledata(filedata_builder.build());
				
				UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
		
				userinfo_builder.setId(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null));
				userinfo_builder.setPasswd(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null));
				
				builder_msg.setUserinfo(userinfo_builder.build());
			
				EcamsClient syncClient = new EcamsClient(ip,port);
				returnMsg = syncClient.sendMsg(builder_msg.build());
				
				if(returnMsg.getReturnval() == 0){
					for(int i=0;i<returnMsg.getEcamsmsg().getFiledatalist().getFiledatasCount();i++){
						TableItem item = new TableItem(svrlist, SWT.NONE);
						int c = 0;
						item.setText(c++, returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getEditor());
						item.setText(c++, returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getLstdate());
						item.setText(c++, returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getViewver());
						if(returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getMsguse().equals("-")){
							item.setText(c++, "");
						}else{
							item.setText(c++, returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getMsguse());
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		setControl(container);
	}

	public Object[] getSelectedResources() {
		return selectedResources;
	}
	
	public StyledText getText() {
		return text;
	}
	
	
	public String getVer(){
		if(svrlist.getSelectionIndex() < 0){
			return "";
		}else{
			//return Integer.toString(returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(svrlist.getSelectionIndex()).getVersion());
			return returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(svrlist.getSelectionIndex()).getViewver();
		}
	}

}
