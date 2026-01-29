package com.azsoft.ecams.ui.wizard;

import java.util.HashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.LastCheckOutJob;
import com.azsoft.ecams.ui.dialog.LastCheckOutDlg;
import com.azsoft.ecams.ui.wizard.page.LastCheckOutSelectResourcePage;

public class LastCheckOutWizard extends Wizard implements INewWizard {
	private IResource[] resources, selectResource;
	private IStructuredSelection selection;
	private int i = 0;
	
	private LastCheckOutDlg parentDialog;
	private int len;
	private String itemid, filename, ip, port, id , passwd;
	private String srId, srTitle;
	private boolean chkModify;
	
	public LastCheckOutWizard(IResource[] resources, int len, String itemid, String filename, String ip, String port, String id, String passwd, String srId, String srTitle, boolean chkModify) {
		this.resources = resources;
		this.len = len;
		this.itemid = itemid;
		this.filename = filename;
		this.ip = ip;
		this.port = port;
		this.id = id;
		this.passwd = passwd;
		this.srId = srId;
		this.srTitle = srTitle;
		this.chkModify = chkModify;
		
		setWindowTitle("Previous Version CHECK-OUT");
	}
	
	public void addPages() {
		this.addPage(new LastCheckOutSelectResourcePage(resources,itemid, filename, ip, port, id, passwd));
		//this.addPage(new LastCheckOutRequestPage(itemid, filename));
	}

	public boolean performFinish() {
		if (parentDialog.getCurrentPage() instanceof LastCheckOutSelectResourcePage){
			LastCheckOutSelectResourcePage lastcheckOutSelectResourcePage = (LastCheckOutSelectResourcePage) parentDialog.getCurrentPage();
			selectResource = (IResource[]) lastcheckOutSelectResourcePage.getSelectedResources();
			
			/*if (lastcheckOutSelectResourcePage.getCSRNO().equals("X")){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("SPMS\ubc88\ud638\ub97c \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.");
				messageBox.open();
				return false;
			} else if (lastcheckOutSelectResourcePage.getText().getText().length() < 1){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\uc2e0\uccad\uc0ac\uc720\ub97c \uc785\ub825\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
				messageBox.open();
				return false;*/
			if (lastcheckOutSelectResourcePage.getVer().length() < 1){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\ubaa9\ub85d\uc5d0\uc11c \uccb4\ud06c\uc544\uc6c3\ud560 \ubc84\uc804\uc744 \uc120\ud0dd\ud574 \uc8fc\uc2ed\uc2dc\uc694.");
				messageBox.open();
				return false;
			} else {
				HashMap<String, String> VersionData = new HashMap<String, String>();
				VersionData.put("vergbn","R");
				//VersionData.put("basever",lastcheckOutSelectResourcePage.getVer().substring(0,1));
				//VersionData.put("version",lastcheckOutSelectResourcePage.getVer().substring(2,3));
				VersionData.put("basever",lastcheckOutSelectResourcePage.getVer());
				VersionData.put("version",lastcheckOutSelectResourcePage.getVer());
				//VersionData.put("version",lastcheckOutSelectResourcePage.getVer().split("\\.")[1]);
				
				EcamsProviderPlugin.getPlugin().getJobManager().addJob(new LastCheckOutJob("Previous Version Check-Out Request",selectResource,filename,lastcheckOutSelectResourcePage.getText().getText(),"",VersionData, srId, srTitle, chkModify));
				
				return true;
			}
		}

		return false;
		
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		this.selection = selection;

	}
	
	public boolean canFinish() {
		// TODO Auto-generated method stub
		if (parentDialog.getCurrentPage() instanceof LastCheckOutSelectResourcePage){
			//return false;
			return true;
		}
		/*else if (parentDialog.getCurrentPage() instanceof LastCheckOutRequestPage){
			return true;
		}*/
		return super.canFinish();
	}
	
	public LastCheckOutDlg getParentDialog() {
		return parentDialog;
	}

	public void setParentDialog(LastCheckOutDlg parentDialog) {
		this.parentDialog = parentDialog;
	}

}
