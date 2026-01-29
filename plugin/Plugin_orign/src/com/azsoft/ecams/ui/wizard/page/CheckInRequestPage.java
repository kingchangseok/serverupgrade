package com.azsoft.ecams.ui.wizard.page;


import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.FileDataList;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.swtdesigner.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.StyledText;

public class CheckInRequestPage extends WizardPage {
	private Text text1;
	private TableViewer viewer;
	private Table svrlist;
	private StyledText styledText;
	final private int MAX_CHAR = 100;
	
	private boolean errFlg = false;
	
	private ArrayList<HashMap<String, String>> modList = new ArrayList<HashMap<String, String>>();
	private Object[] selectedResources;
	
	/**
	 * Create the wizard.	
	 */
	public CheckInRequestPage() {
		super("wizardPage");
		setTitle("\uc815\ubcf4\uc785\ub825");
		setDescription("\uc2e0\uccad\uc11c\ub97c \uc791\uc131\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		setControl(container);
		
		Label label1 = new Label(container, SWT.NONE);
		label1.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label1.setBounds(5, 7, 48, 12);
		label1.setText("\uc2dc\uc2a4\ud15c");
		
		text1 = new Text(container, SWT.BORDER);
		text1.setEditable(false);
		text1.setEnabled(false);
		text1.setBounds(60, 5, 542, 23);
		
		viewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
		
		svrlist = viewer.getTable();
		svrlist.setBounds(5, 33, 597, 209);
		//svrlist.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		svrlist.setHeaderVisible(true);
		svrlist.setLinesVisible(true);
		
		TableViewerColumn viewercolumn1 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column1 = viewercolumn1.getColumn();
		column1.setWidth(140);
		column1.setText("\ud504\ub85c\uadf8\ub7a8\uba85");
		column1.setResizable(true);
        column1.setAlignment(SWT.RIGHT);
        
        TableViewerColumn viewercolumn2 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column2 = viewercolumn2.getColumn();
		column2.setWidth(155);
		column2.setText("\ud504\ub85c\uadf8\ub7a8\uc885\ub958");
		column2.setResizable(true);
		column2.setAlignment(SWT.LEFT);
		
        TableViewerColumn viewercolumn3 = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column3 = viewercolumn3.getColumn();
		column3.setWidth(277);
		column3.setText("\ud504\ub85c\uadf8\ub7a8 \uacbd\ub85c");
		column3.setResizable(true);
		column3.setAlignment(SWT.LEFT);
		
		Label label = new Label(container, SWT.NONE);
		label.setFont(org.eclipse.wb.swt.SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label.setBounds(5, 248, 151, 12);
		label.setText("\uc2e0\uccad\uc0ac\uc720 (100\uc790 \uc81c\ud55c)");
		
		styledText = new StyledText(container, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP );
		styledText.setBounds(5, 266, 597, 84);
		/*styledText.setTextLimit(100);
		
		styledText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if( styledText.getText().length() > 100 ){
					styledText.setText(styledText.getText().substring(0,100));
					return;
				}
				System.out.println(styledText.getText().length());
			}
		});
		*/
		setPageComplete(true);
	}
	
	public void setSelectedResources(Object[] selectedResources) {
		this.selectedResources = selectedResources;
		
		IProject project = null;
		project = ((IResource[])selectedResources)[0].getProject();
		if (project != null){
			try {
				text1.setText(project.getPersistentProperty(new QualifiedName("Properties","syscd")));
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		setupCallbacks(project);
	}
	
	public IResource[] getSelectedResources(){
		return (IResource[])selectedResources;
	}
	
	public boolean getErrFlg(){
		return errFlg;
	}
	
	public ArrayList<HashMap<String, String>> getModList(){
		return modList;
	}
	
	public String getSayu(){
		String sayu = styledText.getText();
		if( sayu.length() > MAX_CHAR ) {
			sayu = sayu.substring(0, MAX_CHAR);
		}
		return sayu;
	}
	
	public void setupCallbacks(IProject project){
		errFlg = false;
		svrlist.removeAll();
		modList.clear();
		
		String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
		String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);

		if(ip == null || ip.equals("")){
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c IP\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}
		if(port == null || port.equals("")){
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c PORT\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}
		if(id == null || id.equals("")){
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c ID\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}
		
		if(passwd == null || passwd.equals("")){
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c PASSWORD\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}

		try {
			int i = 0;
			boolean calSw = false;
			
			IResource[] tmpResource = (IResource[]) selectedResources;
			
			IEcamsStatus ecmSta = null;
			for(i=0; i<tmpResource.length; i++){
				ecmSta = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(tmpResource[i]);
				if(null != ecmSta){
					if("1".equals(ecmSta.getRsrcinfo().substring(3,4)) || "1".equals(ecmSta.getRsrcinfo().substring(8,9))){
						calSw = true;
						break;
					}
				}
				ecmSta = null;
			}
			
			if(calSw){
				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("GETDOWNFILELIST");
				
				SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
	    		sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
	    		sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);
				builder_msg.setSysinfo(sysinfo_builder.build());
	    		
				UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
				userinfo_builder.setId(id);
				userinfo_builder.setPasswd(passwd);
				builder_msg.setUserinfo(userinfo_builder.build());

				FileDataList.Builder filedatalist_builder = FileDataList.newBuilder();
				for(i=0; i<tmpResource.length; i++){
					ecmSta = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(tmpResource[i]);
					
					if(ecmSta != null){
						FileData.Builder filedata_builder = FileData.newBuilder();
						filedata_builder.setFilename(tmpResource[i].getName());
						filedata_builder.setItemid(ecmSta.getItemid());
						filedatalist_builder.addFiledatas(filedata_builder.build());
						filedata_builder = null;
					}
					ecmSta = null;
				}
				builder_msg.setFiledatalist(filedatalist_builder.build());
				
				EcamsClient syncClient = new EcamsClient(ip,port);
				ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
		
				if (returnMsg.getReturnStr().startsWith("SOCKERR")){
					MessageBox messageBox = new MessageBox(null, SWT.OK);
					messageBox.setMessage("Connection refused: no Further information");
					messageBox.setText("ERROR");
					messageBox.open();
				}else{
					if(returnMsg.getReturnval() == 0){
						HashMap<String, String> modObj = new HashMap<String, String>();
						
						for(i=0;i<returnMsg.getEcamsmsg().getFiledatalist().getFiledatasCount();i++){
							TableItem item = new TableItem(svrlist, SWT.NONE);
							int c = 0;
							
							item.setText(c++, returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getFilename());
							item.setText(c++, returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getRsrcinfo().getRsrcmsg());
							item.setText(c++, returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getPathinfo().getRelativitePath());
							if("ERROR".equals(returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getItemid())){
								item.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
								errFlg = true;
							}else{
								if(returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getItemid().equals(returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getBaseitem())){
									item.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
								}else{
									item.setForeground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
									modObj = new HashMap<String, String>();
									modObj.put("rsrcname", returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getFilename());
									modObj.put("dirpath", returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getPathinfo().getRelativitePath());
									modObj.put("baseitem", returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getBaseitem());
									modObj.put("itemid", returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getItemid());
									modObj.put("rsrccd", returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getRsrcinfo().getRsrccd());
									modObj.put("rsrctypename", returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getRsrcinfo().getRsrcmsg());
									modObj.put("cminfo", returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getRsrcinfo().getCminfo());
									modList.add(modObj);
									modObj = null;
								}
							}
						}
					}
				}
				
				returnMsg = null;
				syncClient = null;
				
			} else {
				for(i=0; i<tmpResource.length; i++){
					ecmSta = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(tmpResource[i]);
					
					if(ecmSta != null){
						TableItem item = new TableItem(svrlist, SWT.NONE);
						int c = 0;
						item.setText(c++, ecmSta.getName());
						item.setText(c++, ecmSta.getRsrccodename());
						item.setText(c++, ecmSta.getRelativitePath());
						item.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
					}
					ecmSta = null;
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		svrlist.redraw();
	}
}
