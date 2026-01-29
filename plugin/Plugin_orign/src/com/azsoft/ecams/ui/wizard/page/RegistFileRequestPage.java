package com.azsoft.ecams.ui.wizard.page;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.StyledText;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;
import com.swtdesigner.SWTResourceManager;

public class RegistFileRequestPage extends WizardPage{
	private Logger logger = Logger.getLogger(this.getClass());
	
	private ResourceSelectionTree resourceSelectionTree;
	//private SelectionListener rsrc_combo_Listener;
	
	private Object[] selectedResources;
	
	private IProject project;
	
	private Label system_lb;
	private Text system_text;
	
	private Label pgmcomment_lb;
	private StyledText pgmcomment_text;
	
	private Label job_lb;
	//private Combo job_combo;20130308
	private Text job_text;
	
	private Label rsrc_lb;
	private Combo rsrc_combo;
	
	//private Label lang_lb;
	//private Combo lang_combo;
	
	//private Label pgmgrade_lb;
	//private Combo pgmgrade_combo;
	
	
	/**
	 * Create the wizard.
	 */
	public RegistFileRequestPage() {
		super("wizardPage");
		setTitle("\uc2e0\uaddc\ub4f1\ub85d \uc815\ubcf4\uc785\ub825");
		setDescription("\uc2e0\uccad\uc11c\ub97c \uc791\uc131\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		/* langage 사용 안함. 20111216 hoyoon
		setupCallbacks();
		*/
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		
		system_lb = new Label(container, SWT.NONE);
		system_lb.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		system_lb.setBounds(10, 10, 44, 17);
		system_lb.setText("\uc2dc\uc2a4\ud15c:");
		
		system_text = new Text(container, SWT.BORDER);
		system_text.setEditable(false);
		system_text.setBounds(60, 7, 498, 23);
		
		resourceSelectionTree = new ResourceSelectionTree(container, SWT.NONE, null, false);
		resourceSelectionTree.setLocation(10, 43);
		resourceSelectionTree.setSize(562, 130);
		
		job_lb = new Label(container, SWT.NONE);
		job_lb.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		job_lb.setBounds(10, 187, 34, 12);
		job_lb.setText("\uc5c5\ubb34:");
		/*20130308
		job_combo = new Combo(container, SWT.NONE);
		job_combo.setEnabled(false);
		job_combo.setBounds(105, 185, 170, 20);
		job_combo.removeAll();
		job_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");
		job_combo.select(0);
		*/
		job_text = new Text(container, SWT.BORDER);
		job_text.setEditable(false);
		job_text.setBounds(105, 185, 170, 20);
		
		rsrc_lb = new Label(container, SWT.NONE);
		rsrc_lb.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		rsrc_lb.setBounds(289, 187, 95, 12);
		rsrc_lb.setText("\ud504\ub85c\uadf8\ub7a8\uc885\ub958:");
		
		rsrc_combo = new Combo(container, SWT.NONE);
		rsrc_combo.setBounds(386, 185, 170, 20);
		rsrc_combo.removeAll();
		rsrc_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");
		rsrc_combo.select(0);

		pgmcomment_lb = new Label(container, SWT.NONE);
		pgmcomment_lb.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		pgmcomment_lb.setBounds(10, 213, 89, 12);
		pgmcomment_lb.setText("\ud504\ub85c\uadf8\ub7a8\uc124\uba85:");

		pgmcomment_text = new StyledText(container, SWT.BORDER);
		pgmcomment_text.setBounds(10, 238, 562, 39);		

		/* langage 없음.  20111216 hoyoon
		rsrc_combo.addSelectionListener(rsrc_combo_Listener);
		
		lang_lb = new Label(container, SWT.NONE);
		lang_lb.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		lang_lb.setBounds(289, 213, 34, 12);
		lang_lb.setText("\uc5b8\uc5b4:");
		
		lang_combo = new Combo(container, SWT.NONE);
		lang_combo.setBounds(386, 211, 170, 20);
		lang_combo.setEnabled(false);
		lang_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");
		
		pgmgrade_lb = new Label(container, SWT.NONE);
		pgmgrade_lb.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		pgmgrade_lb.setBounds(289, 187, 95, 12);
		pgmgrade_lb.setText("\ud504\ub85c\uadf8\ub7a8\ub4f1\uae09:");
		
		pgmgrade_combo = new Combo(container, SWT.NONE);
		pgmgrade_combo.setBounds(386, 185, 170, 20);
		
		pgmgrade_combo.removeAll();
		pgmgrade_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");
		pgmgrade_combo.select(0);
		*/
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
		
		//pgmgrade_combo.removeAll();
		//pgmgrade_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");
		//pgmgrade_combo.select(0);
		
		/*2130308
		job_combo.removeAll();
		job_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");
		job_combo.select(0);
		*/
		rsrc_combo.removeAll();
		rsrc_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");
		rsrc_combo.select(0);
		if (project != null){
			try {
				system_text.setText(project.getPersistentProperty(new QualifiedName("Properties","syscd")));
				/*if (project.getPersistentProperty(new QualifiedName("Properties","setjobcd"))!= null){
					if (!project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).equals("")){
						String[] jobary = project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split("/");
						for (int i=0;i<jobary.length;i++){
							job_combo.add(jobary[i]);
						}
					}
				}
				
				if (project.getPersistentProperty(new QualifiedName("Properties","setrsrccd"))!= null){
					if (!project.getPersistentProperty(new QualifiedName("Properties","setrsrccd")).equals("")){
						String[] rsrcary = project.getPersistentProperty(new QualifiedName("Properties","setrsrccd")).split("/");
						for (int i=0;i<rsrcary.length;i++){
							rsrc_combo.add(rsrcary[i]);
						}
					}
				}
				*/
				
				String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
				String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
				String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
				String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
				
				UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
				userinfo_builder.setId(id);
				userinfo_builder.setPasswd(passwd);

				SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
				sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
	    		sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);
							
				EcamsMessage.Builder  builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("JOBLIST_GET");
				builder_msg.setSysinfo(sysinfo_builder.build());
				builder_msg.setUserinfo(userinfo_builder.build());	
				
				EcamsClient  syncClient = new EcamsClient(ip,port);
				ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());
				/*20130308
				if(returnMsg.getEcamsmsg().getJobinfolist().getJobinfoCount()>0){
					for (int i=0;i<returnMsg.getEcamsmsg().getJobinfolist().getJobinfoCount();i++){
						job_combo.add(returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getJobcd()+":"+returnMsg.getEcamsmsg().getJobinfolist().getJobinfo(i).getJobname());
					}
					job_combo.select(1);
				}
				*/
				job_text.setText(project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[0]
				                  +":"+project.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[1]);
				
				
				if(returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfoCount()>0){
					for (int i=0;i<returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfoCount();i++){
						rsrc_combo.add(returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getRsrccd()+":"+returnMsg.getEcamsmsg().getSysinfo().getRsrcinfolist().getRsrcinfo(i).getRsrcmsg());
					}
					rsrc_combo.select(1);
				}
				
				/*
				CodeInfo.Builder codeinfo_builder = CodeInfo.newBuilder();
				codeinfo_builder.setMacode("PGMGRADE");//"PGMTYPE");
				
				builder_msg.setMsgtype("GETCODEINFO");
				builder_msg.setUserinfo(userinfo_builder.build());
				builder_msg.setCodeinfo(codeinfo_builder.build());
				
				ReturnMsg returnMsg = syncClient.sendMsg(builder_msg.build());		
				
				if(returnMsg.getEcamsmsg().getCodeinfolist().getCodeinfoCount()>0){
					for(int i=0;i<returnMsg.getEcamsmsg().getCodeinfolist().getCodeinfoCount();i++){
						CodeInfo getCodeInfo = returnMsg.getEcamsmsg().getCodeinfolist().getCodeinfo(i);
						if(getCodeInfo.getMicode().length()>0){
							pgmgrade_combo.add(getCodeInfo.getMicode()+":"+getCodeInfo.getCodename());
						}
					}	
					pgmgrade_combo.select(1);
				}
				*/
			}catch(CoreException e){
				logger.error(e.getCause().getMessage());
			}
		}
	}
	
	public String getJob(){
		/*20130308
		if (job_combo.getSelectionIndex() == -1){
			return "";
		}else if(job_combo.getSelectionIndex() == 0){
			return "";
		}
		
		return job_combo.getItem(job_combo.getSelectionIndex());
		*/
		return job_text.getText();
	}
	
	public String getRsrc(){
		if (rsrc_combo.getSelectionIndex() == -1){
			return "";
		}else if(rsrc_combo.getSelectionIndex() == 0){
			return "";
		}
		
		return rsrc_combo.getItem(rsrc_combo.getSelectionIndex());
	}
	
	public String getComment(){
		return pgmcomment_text.getText();
	}
	
	
	/* langage 사용 안함. 20111216 hoyoon
	public void setupCallbacks()
	{
	
		rsrc_combo_Listener = new SelectionListener(){
	
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
	
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
				String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
				String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
				
				UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
				userinfo_builder.setId(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null));
				userinfo_builder.setPasswd(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null));
				
				RsrcInfoList.Builder rsrcInfoList_builder = RsrcInfoList.newBuilder();
				RsrcInfo.Builder rsrcInfo_builder = RsrcInfo.newBuilder();
				rsrcInfo_builder.setRsrccd(((Combo)e.widget).getText().split(":")[0]);
				rsrcInfo_builder.setRsrcmsg(((Combo)e.widget).getText().split(":")[1]);
				
				rsrcInfoList_builder.addRsrcinfo(rsrcInfo_builder.build());
				
				SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
				sysinfo_builder.setSyscd(system_text.getText().split(":")[0]);
				sysinfo_builder.setSysmsg(system_text.getText().split(":")[1]);
				sysinfo_builder.setRsrcinfolist(rsrcInfoList_builder.build());
				
				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("GETLANG");
				builder_msg.setUserinfo(userinfo_builder.build());
				builder_msg.setSysinfo(sysinfo_builder.build());
				
				EcamsClient ecamsclient = new EcamsClient(ip,port);
				ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
				
				if(returnMsg.getEcamsmsg().getLanginfolist().getLanginfoCount()>0){
					lang_combo.setEnabled(true);
					lang_combo.removeAll();
					lang_combo.add("\uc120\ud0dd\ud558\uc138\uc694.");
					for(int i=0;i<returnMsg.getEcamsmsg().getLanginfolist().getLanginfoCount();i++){
						LangInfo langInfo = returnMsg.getEcamsmsg().getLanginfolist().getLanginfo(i);
						if(langInfo.getLangcd().length()>0){
							lang_combo.add(langInfo.getLangcd()+":"+langInfo.getLangname());
						}
					}
					lang_combo.select(0);
				}
			}
		};
		
	}
	*/
	
	/*  langage 사용 안함. 20111216 hoyoon
	public String getLang(){
		if(lang_combo.getSelectionIndex() == -1){
			return "";
		}else if(lang_combo.getSelectionIndex() == 0){
			return "";
		}
		
		return lang_combo.getItem(lang_combo.getSelectionIndex());
	}
	*/
	
	/*
	public String getGrade(){
		if (pgmgrade_combo.getSelectionIndex() == -1){
			return "";
		}else if(pgmgrade_combo.getSelectionIndex() == 0){
			return "";
		}
				
		return pgmgrade_combo.getItem(pgmgrade_combo.getSelectionIndex());
	}
	*/
}
