package com.azsoft.ecams.ui.wizard.page;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;
import com.swtdesigner.SWTResourceManager;

public class LastCheckOutRequestPage extends WizardPage{
	private Text text1;
	private ResourceSelectionTree resourceSelectionTree;
	private Combo combo1;
	private StyledText text2;
	
	private Object[] selectedResources;
	private IProject project;
	private String itemid, filename;

	/**
	 * Create the wizard.
	 */
	public LastCheckOutRequestPage(String itemid, String filename) {
		super("wizardPage");
		setTitle("\uc774\uc804\ubc84\uc804\uccb4\ud06c\uc544\uc6c3 \uc815\ubcf4\uc785\ub825");
		setDescription("\uc2e0\uccad\uc11c\ub97c \uc791\uc131\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
		this.itemid = itemid;
		this.filename = filename;
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		
		String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		
		Label label1 = new Label(container, SWT.NONE);
		label1.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label1.setBounds(10, 9, 44, 17);
		label1.setText("\uc2dc\uc2a4\ud15c:");
		
		text1 = new Text(container, SWT.BORDER);
		text1.setEditable(false);
		text1.setBounds(95, 7, 477, 23);
		
		Label label2 = new Label(container, SWT.NONE);
		label2.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label2.setBounds(10, 36, 67, 17);
		label2.setText("\uc774\uc804\uc815\ubcf4:");
		
		combo1 = new Combo(container, SWT.NONE);
		combo1.setEnabled(true);
		combo1.setBounds(95, 36, 477, 20);
		
		UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
		userinfo_builder.setId(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null));
		userinfo_builder.setPasswd(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null));
		
		EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
		builder_msg.setMsgtype("GET_LASTVERSION");
		builder_msg.setUserinfo(userinfo_builder.build());
		
		FileData.Builder filedata_builder = FileData.newBuilder();
		
		filedata_builder.setItemid(itemid);
		filedata_builder.setFilename(filename);
		builder_msg.setFiledata(filedata_builder.build());
		
		EcamsClient syncClient = new EcamsClient(ip, port);
		ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
		
		for(int i=0;i<returnMsg.getEcamsmsg().getFiledatalist().getFiledatasCount();i++){
			FileData filedatas = returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i);
			combo1.add(filedatas.getLstdate()+"   "+filedatas.getEditor()+"   "+filedatas.getVersion());
		}
		combo1.select(0);
		
		
		resourceSelectionTree = new ResourceSelectionTree(container, SWT.NONE, null, false);
		resourceSelectionTree.setLocation(10, 136);
		resourceSelectionTree.setSize(562, 150);
		
		text2 = new StyledText(container, SWT.BORDER);
		text2.setBounds(20, 79, 552, 51);
		
		Label label4 = new Label(container, SWT.NONE);
		label4.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label4.setBounds(10, 61, 61, 12);
		label4.setText("\uc2e0\uccad\uc0ac\uc720");
		
		

	}
	
	
	public ResourceSelectionTree getResourceSelectionTree() {
		return resourceSelectionTree;
	}
	
	public void setSelectedResources(Object[] selectedResources) {
		this.selectedResources = selectedResources;
		resourceSelectionTree.setResources((IResource[]) selectedResources);
		
		project = null;
		for (int i=0;i<((IResource[])selectedResources).length;i++){
			project = ((IResource[])selectedResources)[i].getProject();
			break;
		}
		if (project != null){
			try {
				text1.setText(project.getPersistentProperty(new QualifiedName("Properties","syscd")));				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public StyledText getText2() {
		return text2;
	}	
	
	public String getCombo1() {
		return combo1.getText();
	}
}
