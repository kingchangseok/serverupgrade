package com.azsoft.ecams.ui.propertiespage;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.PropertyPage;

import com.azsoft.ecams.core.EcamsProjectNature;
import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.swtdesigner.SWTResourceManager;

import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;


public class EcamsPropertiesPage extends PropertyPage{
	private Logger logger = Logger.getLogger(this.getClass());
	
	private String ip,port,id,passwd;
	private String[] selectedList_all;
	private String[] selectedList_remove;
	
	private String syscd;
	private String jobcd;
	public List jobList = new ArrayList();
	
	private Group group;
	private Button useyn_bt;
	private Button autosync_bt;
	private Button sync_bt;
	
	private Label system_lb;
	private CCombo system_combo;
	private Label job_lb;
	private CCombo job_combo;
	private Label resource_lb;
	private org.eclipse.swt.widgets.List all_list;
	private Button add_bt;
	private Button remove_bt;
	private org.eclipse.swt.widgets.List selt_list;
	
	private MouseListener userynButtonCheck_Listener;
	
	private MouseListener addButtonClick_Listener;
	private MouseListener removeButtonClick_Listener;
	private MouseListener syncButtonClick_Listener;
	
	private SelectionListener systemSelect_Listener;
	private SelectionListener jobSelect_Listener;
	private SelectionListener listAll_Listener;
	private SelectionListener listSet_Listener;
	private Text textProject;
	private	String NO_PROJECT_NAME = "\ud504\ub85c\uc81d\ud2b8\uba85\uaddc\uce59\ubbf8\uc785\ub825";
	
	/**
	 * Create the preference page.
	 */
	public EcamsPropertiesPage() {
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
		
		setupCallbacks();
		
		Composite container = new Composite(parent, SWT.NULL);
		
		group = new Group(container, SWT.NONE);
		group.setBounds(10, 28, 494, 269);
		
		system_lb = new Label(group, SWT.NONE);
		system_lb.setText("\uc2dc\uc2a4\ud15c");
		system_lb.setBounds(10, 15, 90, 15);
		
		system_combo = new CCombo(group, SWT.BORDER);
		system_combo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		system_combo.setBounds(106, 12, 235, 20);
		system_combo.setEnabled(true);
		system_combo.setEditable(false);
		//system_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");
		system_combo.addSelectionListener(systemSelect_Listener);
		
		resource_lb = new Label(group, SWT.NONE);
		resource_lb.setText("\ud504\ub85c\uadf8\ub7a8 \uc885\ub958");
		resource_lb.setBounds(10, 65, 76, 15);
		
		all_list = new org.eclipse.swt.widgets.List(group, SWT.BORDER | SWT.H_SCROLL | SWT.MULTI);
		all_list.setBounds(106, 61, 147, 200);
		all_list.setEnabled(false);
		all_list.addSelectionListener(listAll_Listener);
		
		add_bt = new Button(group, SWT.NONE);
		add_bt.setText("\u25b6");
		add_bt.setEnabled(false);
		add_bt.setBounds(259, 115, 36, 22);
		add_bt.addMouseListener(addButtonClick_Listener);
		
		remove_bt = new Button(group, SWT.NONE);
		remove_bt.setText("\u25c0");
		remove_bt.setEnabled(false);
		remove_bt.setBounds(259, 155, 36, 22);
		remove_bt.addMouseListener(removeButtonClick_Listener);
				
		selt_list = new org.eclipse.swt.widgets.List(group, SWT.BORDER | SWT.H_SCROLL | SWT.MULTI);
		selt_list.setBounds(301, 61, 183, 200);
		selt_list.setEnabled(false);
		selt_list.addSelectionListener(listSet_Listener);
		
		autosync_bt = new Button(group, SWT.CHECK);
		autosync_bt.setBounds(10, 245, 113, 16);
		autosync_bt.setText("\uc790\ub3d9 \ub3d9\uae30\ud654 \uc0ac\uc6a9");
		autosync_bt.setVisible(false);
		/*	//20201222 
		job_lb = new Label(group, SWT.NONE);
		job_lb.setText("\uc5c5\ubb34");
		job_lb.setBounds(10, 40, 90, 15);
		
		job_combo = new CCombo(group, SWT.BORDER);
		job_combo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		job_combo.setBounds(106, 37, 235, 20);
		job_combo.setEnabled(true);
		job_combo.setEditable(false);
		job_combo.addSelectionListener(jobSelect_Listener);
		*/
		Label lblProject = new Label(group, SWT.NONE);
		lblProject.setText("\ud504\ub85c\uc81d\ud2b8\uba85");
		lblProject.setBounds(10, 40, 90, 15);
		
		textProject = new Text(group, SWT.BORDER);
		textProject.setEditable(false);
		textProject.setBounds(106, 38, 235, 18);
	
		//sync_bt = new Button(group, SWT.NONE);
		//sync_bt.setBounds(407, 236, 77, 22);
		//sync_bt.setText("\ub3d9\uae30\ud654");
		//sync_bt.addMouseListener(syncButtonClick_Listener);
		
		useyn_bt = new Button(container, SWT.CHECK);
		useyn_bt.setBounds(10, 10, 122, 16);
		useyn_bt.setText("\ud615\uc0c1\uad00\ub9ac\uc0ac\uc6a9\uc5ec\ubd80");
		useyn_bt.addMouseListener(userynButtonCheck_Listener);
		
		updateInterface();
		
		return container;
	}

	public void updateInterface(){
		try {
			resetData();
			
			ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
			passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
			
			if("true".equals(((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","useyn")))){
				useyn_bt.setSelection(true);
			}
			else {
				useyn_bt.setSelection(false);
			}
			
			if (useyn_bt.getSelection()){
				group.setEnabled(true);
				system_combo.setEnabled(true);
				//job_combo.setEnabled(true);	//20201222
				performDefaults();
			}
			else{
				group.setEnabled(false);
				system_combo.setEnabled(false);
				//job_combo.setEnabled(false);	//20201222			
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public boolean performOk()
	{
		try{
			if (useyn_bt.getSelection()){
				
				String prjName = ((Hashtable)system_combo.getData("prjname")).get(system_combo.getSelectionIndex()).toString();
				//String prjName = job_combo.getText().split(":")[2];	//20201222
				
				if( system_combo.getSelectionIndex() <= 0 ){
					MessageBox messageBox = new MessageBox(system_combo.getParent().getShell(), SWT.OK);
					messageBox.setMessage("\uc2dc\uc2a4\ud15c\uc744 \uc120\ud0dd\ud574 \uc8fc\uc2ed\uc2dc\uc624.");
					messageBox.open();
					return false;
				}

				/*	// 20201222 
				if( job_combo.getSelectionIndex() <= 0 ){
					MessageBox messageBox = new MessageBox(job_combo.getParent().getShell(), SWT.OK);
					messageBox.setMessage("\uc5c5\ubb34\ub97c \uc120\ud0dd\ud558\uc138\uc694.");
					messageBox.open();
					return false;
				}
				*/
				//\uc77c\ub2e8 \ud604\uc7ac \ud504\ub85c\uc81d\ud2b8\uba85 \uc5c6\uc774 \uac00\ub2a5\ud1a0\ub85d
				//if(!prjName.equals(NO_PROJECT_NAME) && !prjName.equals(((IProject)getElement()).getProject().getName())){
				if(prjName.equals(NO_PROJECT_NAME) || !prjName.equals(((IProject)getElement()).getProject().getName())){
				//if(!system_combo.getText().split(":")[3].equals(((IProject)getElement()).getProject().getName())){
					MessageBox messageBox = new MessageBox(system_combo.getParent().getShell(), SWT.OK);
					messageBox.setMessage("\ud504\ub85c\uc81d\ud2b8\uba85\uc774 \uc77c\uce58\ud558\uc9c0 \uc54a\uc2b5\ub2c8\ub2e4. [\ud504\ub85c\uc81d\ud2b8\uba85:"+prjName+"]");
					messageBox.open();
					return false;
				}

				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("ADMIN");

				UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
				userinfo_builder.setId(id);
				userinfo_builder.setPasswd(passwd);
				
				builder_msg.setUserinfo(userinfo_builder.build());

				EcamsClient syncClient = new EcamsClient(ip,port);
				ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
				
				if (returnMsg.getReturnStr().startsWith("SOCKERR")){
					java.lang.System.setProperty("isAdmin", "0");
					
					MessageBox messageBox = new MessageBox(null, SWT.OK);
					messageBox.setMessage("Connection refused: no Further information");
					messageBox.setText("ERROR");
					messageBox.open();
					
					return false;
					
				}else{
					if( 0 == returnMsg.getReturnval() ){
						if( "1".equals(returnMsg.getReturnStr()) ) {
							java.lang.System.setProperty("isAdmin", "1");
						} else {
							java.lang.System.setProperty("isAdmin", "0");
						}
					}else {
						java.lang.System.setProperty("isAdmin", "0");
					}
					
					
					((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","useyn"), "true");
					//((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","syscd"), system_combo.getText());
					((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","syscd"), ((Hashtable)system_combo.getData("sysall")).get(system_combo.getSelectionIndex()).toString());
					((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","allrsrccd"), StringUtils.join(all_list.getItems(),"/"));
					((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","setrsrccd"), StringUtils.join(selt_list.getItems(),"/"));
					((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","setjobcd"), StringUtils.join(jobList.toArray(),"/"));	//20201222
					//((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","setanalyn"), system_combo.getText().split(":")[2]);
					//((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","sysinfo"), system_combo.getText().split(":")[4]);
					((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","setanalyn"), ((Hashtable)system_combo.getData("analyn")).get(system_combo.getSelectionIndex()).toString());
					((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","sysinfo"), ((Hashtable)system_combo.getData("sysinfo")).get(system_combo.getSelectionIndex()).toString());
					//((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","setjobcd"), job_combo.getText());	// 20201222
	
	
					if (autosync_bt.getSelection()){
						((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","autosync"), "true");
					} else {
						((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","autosync"), "false");
					}
					/*
					String chgflg = ((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","ischanged"));
					if(chgflg == null || chgflg.equals("")) chgflg = "0";
					((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","ischanged"), chgflg);
					*/
					((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","ischanged"), "0");
					
					EcamsRepositoryProvider.setManagedByEcams((IProject)getElement());
					
					if(((IProject)getElement()).toString().substring(2).length()<1){
						MessageBox messageBox = new MessageBox(system_combo.getParent().getShell(), SWT.OK);
						messageBox.setMessage("\ud504\ub85c\uc81d\ud2b8\uba85\uc774 \uc77c\uce58\ud558\uc9c0\uc54a\uc2b5\ub2c8\ub2e4.");
						messageBox.open();
						return false;
					}
					
					if(!((IProject)getElement()).getProject().isNatureEnabled(EcamsProjectNature.NATURE_ID)) {
						EcamsProjectNature.setEcamsNature( ((IProject)getElement()).getProject() );
					}
				}
			} else {
				java.lang.System.setProperty("isAdmin", "0");
				
				((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","useyn"), "false");
				((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","autosync"), "false");
				((IResource) getElement()).setPersistentProperty(new QualifiedName("Properties","ischanged"), "0");
				
				if (EcamsRepositoryProvider.isManagedByEcams((IProject)getElement())){
					EcamsRepositoryProvider.unsetManagedByEcams((IProject)getElement());
					//EcamsProviderPlugin.getPlugin().getJobManager().addJob(new UnSyncJob("eCAMS Disconnect...",(IProject)getElement()));
				}
				
				if( ((IProject)getElement()).getProject().isNatureEnabled(EcamsProjectNature.NATURE_ID) ) {
					EcamsProjectNature.delEcamsNature( ((IProject)getElement()).getProject() );
				}
			}
			List projectList = new ArrayList();
			projectList.add((IProject)getElement());
			EcamsProviderPlugin.broadcastModificationStateChanges((IResource[]) projectList.toArray(new IResource[projectList.size()]));
		} catch (CoreException e) {
			return false;
		}
		return true;
	}

	public void performDefaults()
	{
		try{
			//System.out.println(((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","syscd")));
			if (((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","syscd"))!= null){
				if (!((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","syscd")).equals("")){
					syscd = ((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","syscd"));
				}
			}
			
			if (((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","allrsrccd"))!= null){
				if (!((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","allrsrccd")).equals("")){
					all_list.setItems( (((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","allrsrccd"))).split("/"));
				}
			}
			if (((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","setrsrccd"))!= null){
				if (!((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","setrsrccd")).equals("")){
					selt_list.setItems( (((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","setrsrccd"))).split("/"));
				}
			}
			
			// 20201222
			/*if (((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","setjobcd"))!= null){
				if (!((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","setjobcd")).equals("")){
					jobcd = ((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","setjobcd"));
					//Collections.addAll(jobList, (((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","setjobcd"))).split("/"));
				}
			}*/			
			 
			system_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");				
			system_combo.select(0);
			system_combo.setEnabled(true);
			
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("SYSINFOLIST_USER_GET");
	
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
	
			userinfo_builder.setId(id);
			userinfo_builder.setPasswd(passwd);
			
			
			builder_msg.setUserinfo(userinfo_builder.build());
		
			EcamsClient syncClient = new EcamsClient(ip,port);
			ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
	
			if (returnMsg.getReturnStr().startsWith("SOCKERR")){
				MessageBox messageBox = new MessageBox(null, SWT.OK);
				messageBox.setMessage("Connection refused: no Further information");
				messageBox.setText("ERROR");
				messageBox.open();
			}else{
				Hashtable <Integer, String> hashTblTempSysAll = new Hashtable<Integer, String>();
				Hashtable <Integer, String> hashTblTempSysCd = new Hashtable<Integer, String>();
				Hashtable <Integer, String> hashTblTempSysMsg = new Hashtable<Integer, String>();
				Hashtable <Integer, String> hashTblTempAnalYN = new Hashtable<Integer, String>();
				Hashtable <Integer, String> hashTblTempPrjName = new Hashtable<Integer, String>();
				Hashtable <Integer, String> hashTblTempSysInfo = new Hashtable<Integer, String>();
				
				system_combo.removeAll();
				system_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");				
				system_combo.select(0);
				
				hashTblTempSysAll.put(0,	"");
				hashTblTempSysCd.put(0,		"");
				hashTblTempSysMsg.put(0,	"");
				hashTblTempAnalYN.put(0,	"");
				hashTblTempPrjName.put(0,	"");
				hashTblTempSysInfo.put(0,	"");
					
				int iSysInfoCnt = returnMsg.getEcamsmsg().getSysinfolist().getSysinfoCount();
				
				for(int i=0;i<iSysInfoCnt;i++){
					SysInfo getSysInfo = returnMsg.getEcamsmsg().getSysinfolist().getSysinfo(i);
					
					hashTblTempSysAll.put(i+1,	getSysInfo.getSyscd()+":"+getSysInfo.getSysmsg()+":"+getSysInfo.getAnalyn()+":"+getSysInfo.getPrjname()+":"+getSysInfo.getSysinfo());
					hashTblTempSysCd.put(i+1,	getSysInfo.getSyscd());
					//hashTblTempSysCd.put(i+1,	getSysInfo.getSyscd()+":"+getSysInfo.getSysmsg()+":"+getSysInfo.getAnalyn()+":"+getSysInfo.getPrjname()+":"+getSysInfo.getSysinfo());
					hashTblTempSysMsg.put(i+1,	getSysInfo.getSysmsg());
					hashTblTempAnalYN.put(i+1,	getSysInfo.getAnalyn());
					hashTblTempPrjName.put(i+1,	getSysInfo.getPrjname());
					hashTblTempSysInfo.put(i+1,	getSysInfo.getSysinfo());
					
					system_combo.add( getSysInfo.getSysmsg() );
				}
				
				system_combo.setData("sysall",	hashTblTempSysAll);
				system_combo.setData("syscd",	hashTblTempSysCd);
				system_combo.setData("sysmsg",	hashTblTempSysMsg);
				system_combo.setData("analyn",	hashTblTempAnalYN);
				system_combo.setData("prjname",	hashTblTempPrjName);
				system_combo.setData("sysinfo",	hashTblTempSysInfo);
				
				if (syscd.length()>0 && selt_list.getItems().length>0){
					//system_combo.setEnabled(false);
//					if(syscd.substring(syscd.length()-1).equals(":")){
//						syscd = syscd.substring(0, syscd.length()-1);
//					}
					syscd = syscd.split(":")[0];
					
					for(int i=0;i<system_combo.getItemCount();i++){
						//System.out.println(system_combo.getItem(i));
						//System.out.println(syscd);
//						if(system_combo.getItem(i).equals(syscd)){
//							system_combo.select(i);
//							joblist();
//							break;
//						}
						
						if(((Hashtable)system_combo.getData("syscd")).get(i).toString().equals(syscd)){
							system_combo.select(i);
							joblist();
							break;
						}						
					}
				}
				
				
				/*
				for(int i=0;i<returnMsg.getEcamsmsg().getSysinfolist().getSysinfoCount();i++){
					SysInfo getSysInfo = returnMsg.getEcamsmsg().getSysinfolist().getSysinfo(i);
					system_combo.add(getSysInfo.getSyscd()+":"+getSysInfo.getSysmsg()+":"+getSysInfo.getAnalyn()+":"+getSysInfo.getPrjname()+":"+getSysInfo.getSysinfo());
				}
				
				if (syscd.length()>0 && selt_list.getItems().length>0){
					//system_combo.setEnabled(false);
					if(syscd.substring(syscd.length()-1).equals(":")){
						syscd = syscd.substring(0, syscd.length()-1);
					}
					for(int i=0;i<system_combo.getItemCount();i++){
						//System.out.println(system_combo.getItem(i));
						//System.out.println(syscd);
						if(system_combo.getItem(i).equals(syscd)){
							system_combo.select(i);
							joblist();
							break;
						}
	//					}else{
	//						job_combo.select(0);
	//					}
					}
				}
				*/
				
				if (((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","autosync"))!= null){
					if (((IResource) getElement()).getPersistentProperty(new QualifiedName("Properties","autosync")).equals("true")){
						autosync_bt.setSelection(true);
					}
					else {
						autosync_bt.setSelection(false);
					}
				}
			}
		} catch (CoreException e) {
		}
	}
	
	public void resetData(){
		useyn_bt.setSelection(false);
		autosync_bt.setSelection(false);
		all_list.removeAll();
		selt_list.removeAll();
		jobList = null;
		system_combo.removeAll();
//		job_combo.removeAll();
		syscd = "";
		selectedList_all=null;
		selectedList_remove=null;		
		textProject.setText("");
	}

	public void setupCallbacks()
	{
		userynButtonCheck_Listener = new MouseListener(){

			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				if (useyn_bt.getSelection()){
					group.setEnabled(true);
					autosync_bt.setEnabled(true);
					performDefaults();
				}
				else{
					group.setEnabled(false);
					autosync_bt.setEnabled(false);
					resetData();
				}
			}
			
		};
		
		systemSelect_Listener = new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				joblist();
			}
		};

		// 20201222
		/*jobSelect_Listener = new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				if (job_combo.getSelectionIndex()>0) {
					textProject.setText(job_combo.getText().split(":")[2]);
				} else {
					textProject.setText("");
				}
			}
		};
		*/
		
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
		
		
		syncButtonClick_Listener = new MouseListener(){

			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}


		};
		
		addButtonClick_Listener = new MouseListener(){

			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseDown(MouseEvent e) {
				/*
				if(selt_list.getItemCount()>0){
					for(int i=0;i<selectedList_all.length;i++){
						//IProject findProject = EcamsRepositoryProvider.findProject(syscd.split(":")[0], selectedList_all[i]);
						IProject findProject = EcamsRepositoryProvider.findProject(syscd.split(":")[0],jobcd.split(":")[0]);
						if (findProject != null && findProject!=((IResource) getElement())){
							MessageBox messageBox = new MessageBox(all_list.getParent().getShell(), SWT.OK);
							messageBox.setMessage("\ub2e4\ub978 Project\uc5d0 \ud3ec\ud568\ub41c \uc790\uc6d0\uc785\ub2c8\ub2e4.");
							messageBox.open();
							return;
						}
						for(int j=0;j<selt_list.getItemCount();j++){
							if(selectedList_all[i].equals(selt_list.getItem(j))){
								MessageBox messageBox = new MessageBox(all_list.getParent().getShell(), SWT.OK);
								messageBox.setMessage("\uc911\ubcf5 \uc2e0\uccad \uc874\uc7ac");
								messageBox.open();
								return;
							}
						}
						selt_list.add(selectedList_all[i]);
					}
				}else{
					for(int i=0;i<selectedList_all.length;i++){
						selt_list.add(selectedList_all[i]);
					}
				}
				selectedList_all = null;
				*/
			}

			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
				if(selt_list.getItemCount()>0){
					system_combo.setEnabled(false);
				}else{
					system_combo.setEnabled(true);
				}
				selt_list.deselectAll();
				all_list.deselectAll();
			}


		};
		
		removeButtonClick_Listener = new MouseListener(){

			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				for(int i=0;i<selectedList_remove.length;i++){
					for(int j=0;j<selt_list.getItemCount();j++){
						if(selectedList_remove[i].equals(selt_list.getItem(j))){
							selt_list.remove(j);
						}
					}
				}
				selectedList_remove = null;
			}

			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				if(selt_list.getItemCount()>0){
					system_combo.setEnabled(false);
				}else{
					system_combo.setEnabled(true);
				}
				selt_list.deselectAll();
				all_list.deselectAll();
			}
		};
	
	}
	
	public void joblist(){
		EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
		builder_msg.setMsgtype("JOBLIST_GET");
		
		SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
//		sysinfo_builder.setSyscd(system_combo.getText().split(":")[0]);
//		sysinfo_builder.setSysmsg(system_combo.getText().split(":")[1]);
		
		sysinfo_builder.setSyscd( ((Hashtable)system_combo.getData("syscd")).get(system_combo.getSelectionIndex()).toString() );
		sysinfo_builder.setSysmsg( ((Hashtable)system_combo.getData("sysmsg")).get(system_combo.getSelectionIndex()).toString() );
		
		builder_msg.setSysinfo(sysinfo_builder.build());
		
		UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
		
		userinfo_builder.setId(id);
		userinfo_builder.setPasswd(passwd);
		
		
		builder_msg.setUserinfo(userinfo_builder.build());				
		
		EcamsClient syncClient = new EcamsClient(ip,port);
		ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
		
		int rsrclen = returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfoCount();
		
		all_list.removeAll();
		selt_list.removeAll();
		jobList = new ArrayList();	// 20201222
		
		for (int i=0;i<rsrclen;i++){
			//all_list.add(returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getRsrccd()+":"+returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getRsrcmsg());
			selt_list.add(returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getRsrccd()+":"+returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getRsrcmsg()+":"+returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getExename());
			//selt_list.add(returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getRsrccd()+":"+returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getRsrcmsg());
		}
		
		for(int i=0;i<returnMsg.getEcamsmsg().getJobinfolist().getJobinfoCount();i++){	// 20201222 주석제거
			jobList.add(returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getJobcd()+":"+returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getJobname()+":"+returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getDeptcd());
		}
		
		//syscd = system_combo.getText();
		syscd = ((Hashtable)system_combo.getData("sysall")).get(system_combo.getSelectionIndex()).toString();
		textProject.setText(((Hashtable)system_combo.getData("prjname")).get(system_combo.getSelectionIndex()).toString());
		
		/*	// 20201222 주석처리
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
					textProject.setText(job_combo.getItem(i).split(":")[2]);
					break;
				}
			}
		}else{
			job_combo.setEnabled(true);
		}
		*/
		
	}
}
