package com.azsoft.ecams.ui.wizard.page;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.proto.ProtoEcams.FileDataList;
import com.azsoft.ecams.proto.ProtoEcams.PrjInfo;
import com.azsoft.ecams.proto.ProtoEcams.PrjInfoList;
import com.azsoft.ecams.proto.ProtoEcams.RequestInfo;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.dialog.ConfirmMessage2Dlg;
import com.azsoft.ecams.ui.dialog.ConfirmMessage3Dlg;
import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;
import com.azsoft.ecams.util.checksum.CheckSum;
import com.azsoft.ecams.util.file.EFileToByteArray;
import com.swtdesigner.SWTResourceManager;


public class CheckInRealSelectResourcePage extends WizardPage {
	private IResource[] resources;
	private IResource[] resources2;
	private static Combo combo1;
	
	private ReturnMsg returnMsg;
	private ReturnMsg returnMsg1;
	private int sel = 0;
	
	private TreeViewer treeViewer;
	private IDialogSettings settings;
	private static ResourceSelectionTree resourceSelectionTree;
	private String List1="";
	private String List2="";
	private SelectionListener combo1_Listener;
	private Object[] selectedResources;
	private IProject project;
	
	/**
	 * Create the wizard.
	 */
	public CheckInRealSelectResourcePage(IResource[] resources) {
		super("wizardPage");
		this.resources = resources;
		settings = EcamsProviderPlugin.getPlugin().getDialogSettings();
		setPageComplete(false);
		setTitle("\ud30c\uc77c\uc120\ud0dd");
		setDescription("\uccb4\ud06c\uc778\ud558\uc2e4 \ud30c\uc77c\uc744 \uc120\ud0dd\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		setupCallbacks();
		
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		
		Label label2 = new Label(container, SWT.NONE);
		label2.setText("CSR\ubc88\ud638");
		label2.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label2.setBounds(10, 11, 56, 12);
		
		combo1 = new Combo(container, SWT.NONE);
		combo1.setEnabled(true);
		combo1.setBounds(72, 8, 500, 20);
		
		resourceSelectionTree = new ResourceSelectionTree(container, SWT.NONE, resources, true);
		resourceSelectionTree.setLocation(0, 33);
		resourceSelectionTree.setSize(582, 263);

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
		
		project = null;
		for (int i=0 ; i<((IResource[])selectedResources).length ; i++){
			project = ((IResource[])selectedResources)[i].getProject();
			break;
		}
		if (project != null){
			try {
				IEcamsStatus[] filestatuses = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatuses(resources);
				
				String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
				String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
				
				UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
				userinfo_builder.setId(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null));
				userinfo_builder.setPasswd(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null));
				
				RequestInfo.Builder requestinfo_builder = RequestInfo.newBuilder();
				requestinfo_builder.setQrycd("04");
				
				FileDataList.Builder fileDataList_builder = FileDataList.newBuilder();
				FileData.Builder fileData_builder = null;
				
				for (int i=0;i<filestatuses.length;i++){
					fileData_builder = FileData.newBuilder();
					fileData_builder.setFilename(filestatuses[i].getName());
					fileData_builder.setItemid(filestatuses[i].getItemid());
					fileData_builder.setVersion(filestatuses[i].getLastVer());
					fileDataList_builder.addFiledatas(fileData_builder);
				}
				
				SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
				sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
				sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);
				
				EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
				builder_msg.setMsgtype("GETCSRINFO");
				builder_msg.setUserinfo(userinfo_builder.build());
				builder_msg.setRequestinfo(requestinfo_builder.build());
				builder_msg.setSysinfo(sysinfo_builder.build());
				builder_msg.setFiledatalist(fileDataList_builder.build());
				
				EcamsClient syncClient = new EcamsClient(ip, port);
				returnMsg1 = syncClient.sendMsg(builder_msg.build());	
				
				//if(returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfoCount() > 1){
					combo1.add("0:\uc120\ud0dd\ud558\uc138\uc694.");
				//}

				for(int i=0;i<returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfoCount();i++){
					PrjInfo getPrjInfo = returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(i);
					if(getPrjInfo.getScsrno().length()>1){
						combo1.add(getPrjInfo.getScsrno()+":"+getPrjInfo.getScsrtitle());
					}
				}
//				if (returnMsg.getEcamsmsg().getPrjinfolist().getPrjinfoCount()>1){
//					combo1.add("0:\uc120\ud0dd\ud558\uc138\uc694.",0);
//					setCheckboxTree(false);
//				}
				combo1.addSelectionListener(combo1_Listener);
				if(returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfoCount()==1){
					combo1.select(1);
					mouseent();
				}else{
					combo1.select(0);
				}
				
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void setupCallbacks()
	{
		
		combo1_Listener = new SelectionListener(){			//CSR\ubc88\ud638 \ucf64\ubcf4\ubc15\uc2a4 Listener
	
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
	
			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				mouseent();
			}
		};
		
	}
	
	public void mouseent(){
		try {
			if(combo1.getSelectionIndex()<1){
				return;
			}
			resources2 = resources;
			
			String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null));
			userinfo_builder.setPasswd(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null));
			
			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
			sysinfo_builder.setSyscd(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]);
			sysinfo_builder.setSysmsg(project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]);
			
			PrjInfoList.Builder prjinfolist_builder = PrjInfoList.newBuilder();
			
			PrjInfo.Builder PrjInfo_builder = PrjInfo.newBuilder();
			sel = combo1.getSelectionIndex()-1;
			
			PrjInfo_builder.setScsrno(returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(sel).getScsrno());
			PrjInfo_builder.setSbizcode(returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(sel).getScsrtitle());
			PrjInfo_builder.setScsrtitle(returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(sel).getSbizcode());
			PrjInfo_builder.setSemerrequestyn(returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(sel).getSemerrequestyn());
			prjinfolist_builder.addPrjinfo(PrjInfo_builder);
			
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("GETCSRPGM");
			builder_msg.setUserinfo(userinfo_builder.build());
			builder_msg.setPrjinfolist(prjinfolist_builder.build());
			builder_msg.setSysinfo(sysinfo_builder.build());
			
			EcamsClient syncClient = new EcamsClient(ip, port);
			returnMsg = syncClient.sendMsg(builder_msg.build());
			if(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasCount()==0){
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						ConfirmMessage3Dlg confirmMessage3Dlg = new ConfirmMessage3Dlg(getShell(),"\ubaa8\ub378/\uc6b4\uc601\uccb4\ud06c\uc778\ud655\uc778","\ud574\ub2f9 CSR \ubc88\ud638\ub85c \ubaa8\ub378/\uc6b4\uc601 \uccb4\ud06c\uc778 \ub300\uc0c1 \ud504\ub85c\uadf8\ub7a8\uc774 \uc5c6\uc2b5\ub2c8\ub2e4. \r\n");
						confirmMessage3Dlg.open();
						return;
					}
				});
			}
			boolean chk1=false;
			boolean chk2=false;
			List1="";
			List2="";
			project = (resources2[0].getProject());
			String projectPath = project.getLocation().toOSString();
			
			for(int i=0;i<returnMsg.getEcamsmsg().getFiledatalist().getFiledatasCount();i++){
				if(!returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getStatus().equals("B")){
					if(List1.length()<1){
						List1=returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getFilename();
					}else{
						List1=List1+",\r\n"+returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getFilename();
					}
					chk1=true;
				}else if(chk1==false){
					String md5sum="";
					
					//String filepath = (projectPath+"/"+returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getRsrcinfo().getRsrcmsg()+"/"+returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getPathinfo().getRelativitePath());
					String filepath = (projectPath+"/"+returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getPathinfo().getRelativitePath());
					
	    			while(filepath.indexOf("/") >=0){
	    				filepath = filepath.replace("/","\\");
	    			}
	    			
	    			
	    			while(filepath.indexOf("\\\\") >=0){
	    				filepath = filepath.replace("\\\\", "\\");
	    			}

	    			while(filepath.indexOf("\\") >=0){
	    				filepath = filepath.replace("\\", "/");
	    			}	
	    			
	    			String filename = filepath+"/"+returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getFilename();
					
	    			
	    			
					byte[] testbyte = null;
					try {
						File testfile = new File(filename);
						FileChannel inChannel = new FileInputStream(testfile).getChannel();
						int size = (int)inChannel.size();
						if(size<1){
							throw new IOException("Error");
						}
						testbyte = EFileToByteArray.FileToByteArray(testfile);
					} catch (IOException xe) {
						
					}
					md5sum = CheckSum.MD5SumVal(testbyte);
					if(returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getTstmd5Sum() != null){
						if(!returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getTstmd5Sum().equals(md5sum)){
							if(List2.length() > 0){
								List2 = List2+",\r\n";
							}else{
								List2=List2+returnMsg.getEcamsmsg().getFiledatalist().getFiledatas(i).getFilename();
							}
							chk2=true;
						}
					}
				}
			}
			
			if(chk1){
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						ConfirmMessage3Dlg confirmMessage3Dlg = new ConfirmMessage3Dlg(getShell(),"\ubaa8\ub378/\uc6b4\uc601\uccb4\ud06c\uc778\ud655\uc778","\ud574\ub2f9 CSR\ubc88\ud638\ub85c \uac1c\ubc1c\uccb4\ud06c\uc778 \ub418\uc9c0 \uc54a\uc740 \ud504\ub85c\uadf8\ub7a8\uc774 \uc788\uc2b5\ub2c8\ub2e4. \r\n"+List1);
						confirmMessage3Dlg.open();
						return;
					}
				});
			}else if(chk2){
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						ConfirmMessage2Dlg confirmMessage2Dlg = new ConfirmMessage2Dlg(getShell(),"\ubaa8\ub378/\uc6b4\uc601\uccb4\ud06c\uc778\ud655\uc778","\uac1c\ubc1c\uccb4\ud06c\uc778 \ud6c4 \ubcc0\uacbd\ub41c \ud504\ub85c\uadf8\ub7a8\uc774 \uc788\uc2b5\ub2c8\ub2e4. \uacc4\uc18d\uc9c4\ud589\ud558\uaca0\uc2b5\ub2c8\uae4c? \r\n"+List2);
						confirmMessage2Dlg.open();
						return;
					}
				});
			}
		} catch (CoreException ec) {
			// TODO Auto-generated catch block
			ec.printStackTrace();
		}	
	}
	
	public void setCheckboxTree(Boolean checkBoolean) {
		resources2 = resources;
		for(int j=0;j<resources2.length;j++){
			((CheckboxTreeViewer) resourceSelectionTree.getTreeViewer()).setChecked(resources2[j], checkBoolean);
		}
	}
	
	public Boolean getSelectionCheck() {
		if (combo1.getText().split(":")[0].equals("0")){
			return true;
		}else{
			return false;
		}
	}
	
	public ReturnMsg getReturnResult() {
		return returnMsg;
	}
	
	public static ResourceSelectionTree getResourceSelectionTree() {
		return resourceSelectionTree;
	}
	
	public Object[] getSelectedResources() {
		return selectedResources;
	}
	
	public String getCSRNO() {
		String result=null;
		result=returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(sel).getScsrno()+":"+returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(sel).getScsrtitle()+":"
		+returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(sel).getSbizcode()+":"+returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(sel).getSemerrequestyn();
		return result;
	}
	
}