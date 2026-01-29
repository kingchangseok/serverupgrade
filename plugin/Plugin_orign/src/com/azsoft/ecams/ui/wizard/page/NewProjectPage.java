package com.azsoft.ecams.ui.wizard.page;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.swtdesigner.SWTResourceManager;


public class NewProjectPage extends WizardPage {
	private String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
	private String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
	private String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
	private String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);

	private IPath defaultPath = Platform.getLocation();
	
	private String[] selectedList_all = null;
	private String[] selectedList_remove = null;
	private String syscd = "";
//	private String jobcd = "";
	private List jobList = new ArrayList();
	
	private Group group;
	private Label system_lb;
//	private Label job_lb;
	private Label resource_lb;
	private Label prjname_lb;
	private Label dirpath_lb;
	private Label lbldir;
	private Text txtprjnm;
	private Text txtdirpath;
	private Text txtdir;
	private CCombo system_combo;
//	private Combo job_combo;
	private Button chkdir;
	private Button add_bt;
	private Button remove_bt;
	private Button browseButton;

	private org.eclipse.swt.widgets.List all_list;
	private org.eclipse.swt.widgets.List selt_list;

	private MouseListener chkdir_Listener;
	private ModifyListener prjname_Listener;
	private SelectionListener systemSelect_Listener;
	private SelectionListener listAll_Listener;
	private SelectionListener listSet_Listener;
	private Text text;
	
	public NewProjectPage(String Name) {
		// TODO Auto-generated constructor stub
		super(Name);
	}

	public void createControl(Composite parent) {
		setupCallbacks();
		
		Composite container = new Composite(parent, SWT.NULL);
		
		group = new Group(container, SWT.NONE);
		group.setBounds(10, 10, 554, 321);
		group.setEnabled(true);
		
		prjname_lb = new Label(group, SWT.NONE);
		prjname_lb.setText("\ud504\ub85c\uc81d\ud2b8\uba85");
		prjname_lb.setBounds(10, 47, 90, 15);
		
		txtprjnm = new Text(group, SWT.BORDER);
		txtprjnm.setBounds(106, 44, 235, 23);
		txtprjnm.setText("");
		txtprjnm.setEditable(false);
		txtprjnm.setEnabled(false);
		
		dirpath_lb = new Label(group, SWT.NONE);
		dirpath_lb.setText("\ud504\ub85c\uc81d\ud2b8\uacbd\ub85c");
		dirpath_lb.setBounds(10, 76, 90, 15);
		
		txtdirpath = new Text(group, SWT.BORDER);
		txtdirpath.setBounds(106, 73, 438, 23);
		txtdirpath.setText("");
		txtdirpath.setEditable(false);
		txtdirpath.setEnabled(false);
		
		system_lb = new Label(group, SWT.NONE);
		system_lb.setText("\uc2dc\uc2a4\ud15c");
		system_lb.setBounds(10, 21, 90, 15);
		
		system_combo = new CCombo(group, SWT.BORDER);
		system_combo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		system_combo.setBounds(106, 18, 352, 20);
		system_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");
		system_combo.addSelectionListener(systemSelect_Listener);
		system_combo.setEnabled(true);
		system_combo.setEditable(false);
		system_combo.select(0);
		
		resource_lb = new Label(group, SWT.NONE);
		resource_lb.setText("\ud504\ub85c\uadf8\ub7a8\uc885\ub958");
		resource_lb.setBounds(10, 106, 76, 15);
		
		all_list = new org.eclipse.swt.widgets.List(group, SWT.BORDER | SWT.H_SCROLL | SWT.MULTI);
		all_list.setBounds(106, 102, 147, 195);
		all_list.setEnabled(false);
		all_list.addSelectionListener(listAll_Listener);
		
		add_bt = new Button(group, SWT.NONE);
		add_bt.setText("\u25b6");
		add_bt.setEnabled(false);
		add_bt.setBounds(259, 147, 36, 22);
		
		remove_bt = new Button(group, SWT.NONE);
		remove_bt.setText("\u25c0");
		remove_bt.setEnabled(false);
		remove_bt.setBounds(259, 182, 36, 22);
				
		selt_list = new org.eclipse.swt.widgets.List(group, SWT.BORDER | SWT.H_SCROLL | SWT.MULTI);
		selt_list.setBounds(301, 102, 243, 195);
		selt_list.setEnabled(false);
		
		selt_list.addSelectionListener(listSet_Listener);
		
		/*
		job_lb = new Label(group, SWT.NONE);
		job_lb.setText("\ud504\ub85c\uc81d\ud2b8-\uc5c5\ubb34");
		job_lb.setBounds(10, 40, 90, 15);
		
		job_combo = new Combo(group, SWT.NONE);
		job_combo.setBounds(106, 37, 235, 23);
		job_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");
		job_combo.setEnabled(true);
		job_combo.select(0);
		*/
		/*
		lblprjnm = new Label(container, SWT.NONE);
		lblprjnm.setText("Project name:");
		lblprjnm.setBounds(10, 10, 84, 15);
		
		txtprjnm = new Text(container, SWT.BORDER);
		txtprjnm.setBounds(100, 7, 379, 19);
		txtprjnm.setText("");
		txtprjnm.addModifyListener(prjname_Listener);
		
		chkdir = new Button(container, SWT.CHECK);
		chkdir.setBounds(10, 31, 132, 16);
		chkdir.setText("Use default location");
		chkdir.setSelection(true);
		chkdir.addMouseListener(chkdir_Listener);
		
		lbldir = new Label(container, SWT.NONE);
		lbldir.setText("Location:");
		lbldir.setBounds(10, 55, 52, 15);
		lbldir.setEnabled(false);

		txtdir = new Text(container, SWT.BORDER);
		txtdir.setBounds(67, 53, 315, 19);
		txtdir.setText(defaultPath.toString());
		txtdir.setEnabled(false);
		
		browseButton = new Button(container, SWT.PUSH);
		browseButton.setBounds(388, 51, 90, 22);
		browseButton.setText("Browse");
		browseButton.setEnabled(false);
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				dialog.setFilterPath(txtdir.getText());
				dialog.setMessage("Select a directory");
				String directory = dialog.open();
				if (directory != null) {
					txtdir.setText(directory);
					if(txtprjnm.getText().length()==0){
						txtprjnm.setText(directory.substring(directory.lastIndexOf("\\")+1));
					}
				}
			}
		});
		*/
		if(id.length()>0 && passwd.length()>0 && ip.length()>0 && port.length()>0){
			performDefaults();
		}
		setControl(container);
	}
	
	public void performDefaults() {
		all_list.removeAll();
		selt_list.removeAll();
		system_combo.removeAll();
//		job_combo.removeAll();
		jobList = null;
		
		system_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");				
		system_combo.select(0);
		
		EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
		builder_msg.setMsgtype("SYSINFOLIST_USER_GET");

		UserInfo.Builder userinfo_builder = UserInfo.newBuilder();

		userinfo_builder.setId(id);
		userinfo_builder.setPasswd(passwd);
		
		
		builder_msg.setUserinfo(userinfo_builder.build());
	
		EcamsClient syncClient = new EcamsClient(ip,port);
		ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());

		if (returnMsg.getReturnval() == 0){
			for(int i=0;i<returnMsg.getEcamsmsg().getSysinfolist().getSysinfoCount();i++){
				SysInfo getSysInfo = returnMsg.getEcamsmsg().getSysinfolist().getSysinfo(i);
				system_combo.add(getSysInfo.getSyscd()+":"+getSysInfo.getSysmsg()+":"+getSysInfo.getAnalyn()+":"+getSysInfo.getPrjname()+":"+getSysInfo.getSysinfo());
			}
			
			if (syscd.length()>0 && selt_list.getItems().length>0){
				for(int i=0;i<system_combo.getItemCount();i++){
					if(system_combo.getItem(i).equals(syscd)){
						system_combo.select(i);
						joblist();
						break;
					}/*else{
						job_combo.select(0);
					}*/
				}
			}
		}
	}
	
	public void setupCallbacks()
	{
		prjname_Listener = new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				// TODO Auto-generated method stub
				if (chkdir.getSelection()){
					String path = defaultPath.toString();
					if(txtprjnm.getText().length()>0){
						path = path + "/" + txtprjnm.getText();
					}
					txtdir.setText(path);
				}
			}
		};
		
		chkdir_Listener = new MouseListener(){

			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				if (chkdir.getSelection()){
					lbldir.setEnabled(false);
					txtdir.setEnabled(false);
					browseButton.setEnabled(false);
					txtdir.setText(defaultPath.toString()+"/"+txtprjnm.getText());
				} else {
					lbldir.setEnabled(true);
					txtdir.setEnabled(true);
					browseButton.setEnabled(true);
					txtdir.setText("");
				}
			}
		};
		
		systemSelect_Listener = new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				if(system_combo.getText().length()>0 && system_combo.getSelectionIndex()>0){
					txtprjnm.setText(system_combo.getText().split(":")[3]);
					txtdirpath.setText(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()+"/"+system_combo.getText().split(":")[3]);
					joblist();
				}else{
					txtprjnm.setText("");
					txtdirpath.setText("");
				}
			}
		};
		
		listAll_Listener = new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				selectedList_all = all_list.getSelection();
			}
		};
		
		listSet_Listener = new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}

			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				selectedList_remove = selt_list.getSelection();
			}

		};
	}
	
	public void joblist(){
		EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
		builder_msg.setMsgtype("JOBLIST_GET");
		
		SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
		sysinfo_builder.setSyscd(system_combo.getText().split(":")[0]);
		sysinfo_builder.setSysmsg(system_combo.getText().split(":")[1]);
		
		builder_msg.setSysinfo(sysinfo_builder.build());
		
		UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
		
		userinfo_builder.setId(id);
		userinfo_builder.setPasswd(passwd);
		
		
		builder_msg.setUserinfo(userinfo_builder.build());				
		
		EcamsClient syncClient = new EcamsClient(ip,port);
		ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
		
		if(returnMsg.getReturnval() == 0){
			int rsrclen = returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfoCount();
			
			all_list.removeAll();
			selt_list.removeAll();
			jobList = new ArrayList();
			
			for (int i=0;i<rsrclen;i++){
				selt_list.add(returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getRsrccd()+":"+returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getRsrcmsg()+":"+returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getExename());
			}

			for(int i=0;i<returnMsg.getEcamsmsg().getJobinfolist().getJobinfoCount();i++){
				jobList.add(returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getJobcd()+":"+returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getJobname()+":"+returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getDeptcd());
			}
			
			syscd = system_combo.getText();
			
			/*
			job_combo.setEnabled(true);
			job_combo.removeAll();
			job_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");				
			job_combo.select(0);
	
			for(int i=0;i<returnMsg.getEcamsmsg().getJobinfolist().getJobinfoCount();i++){
				job_combo.add(returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getJobcd()+":"+returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getJobname()+":"+returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getDeptcd());
			}
			
			if (syscd.length()>0 && jobcd != null && selt_list.getItemCount()>0){
				for(int i=0;i<job_combo.getItemCount();i++){
					if(job_combo.getItem(i).equals(jobcd)){
						job_combo.select(i);
						break;
					}
				}
			}
			*/
		}
	}
	
	public String getPreference(){
		if(id.length()>0 && passwd.length()>0 && ip.length()>0 && port.length()>0){
			return "0";
		}else{
			return "1";
		}
	}
	/*
	public String getPrjnm(){
		return txtprjnm.getText();
	}
	
	public String getDir(){
		return txtdir.getText();
	}
	*/
	public String getSys() {
		if(system_combo.getSelectionIndex() == 0){
			system_combo.setText("");
		}
		return system_combo.getText();
	}	
	
	/*
	public String getJob() {
		if(job_combo.getSelectionIndex() == 0){
			job_combo.setText("");
		}
		return job_combo.getText();
	}
	*/
	
	public org.eclipse.swt.widgets.List getAll_List(){
		return all_list;
	}
	
	public org.eclipse.swt.widgets.List getSelt_List(){
		return selt_list;
	}
	
	public List getJob_List(){
		return jobList;
	}
}
