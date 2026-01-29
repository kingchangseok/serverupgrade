package com.azsoft.ecams.ui.wizard;


import javax.swing.JOptionPane;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.CheckInJob;
import com.azsoft.ecams.core.jobs.RegistFileJob;
import com.azsoft.ecams.ui.dialog.CheckInDlg;
import com.azsoft.ecams.ui.wizard.page.CheckInRequestPage;
import com.azsoft.ecams.ui.wizard.page.CheckInSelectResourcePage;

public class CheckInWizard extends Wizard implements INewWizard{
	private IResource[] resources;
	private IStructuredSelection selection;
	private CheckInDlg parentDialog;
	private String srId, srTitle, ReqGbn;
	private boolean chkModify;
	
	public CheckInWizard(IResource[] resources, String srid, String title, String ReqGbn, boolean chkModify) {
		this.resources = resources;
		setWindowTitle("\ud504\ub85c\uadf8\ub7a8\ub4f1\ub85d");
		
		this.srId = srid;
		this.srTitle = title;
		this.ReqGbn = ReqGbn;
		this.chkModify = chkModify;
	}
	
	public void addPages() {
		this.addPage(new CheckInSelectResourcePage(resources));
		this.addPage(new CheckInRequestPage());
	}

	public boolean performFinish() {
		if (parentDialog.getCurrentPage() instanceof CheckInRequestPage){
			CheckInRequestPage checkInRequestPage = (CheckInRequestPage) parentDialog.getCurrentPage();
			
			if(checkInRequestPage.getErrFlg()){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\ubaa9\ub85d\uc5d0 \uc624\ub958\uac74\uc774 \uc874\uc7ac\ud569\ub2c8\ub2e4. \ub2e4\uc2dc \ud655\uc778\ud558\uc2dc\uae30 \ubc14\ub78d\ub2c8\ub2e4.");
				messageBox.open();
				return false;
			}
			
			//ReqGbn = "A";
			EcamsProviderPlugin.getPlugin().getJobManager().addJob(new CheckInJob("CheckIn Request",
																				  checkInRequestPage.getSelectedResources(),
																				  checkInRequestPage.getModList(),
																				  checkInRequestPage.getSayu(),
																				  "07",
																				  srId, 
																				  srTitle, 
																				  ReqGbn, 
																				  chkModify));
			checkInRequestPage = null;
			
			return true;
		}
		return false;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		this.selection = selection;
	}
	
	public boolean canFinish() {
		// TODO Auto-generated method stub
		if (parentDialog.getCurrentPage() instanceof CheckInSelectResourcePage){
			return false;
		}
		else if (parentDialog.getCurrentPage() instanceof CheckInRequestPage){
			return true;
		}
		return super.canFinish();
	}

	public CheckInDlg getParentDialog() {
		return parentDialog;
	}

	public void setParentDialog(CheckInDlg parentDialog) {
		this.parentDialog = parentDialog;
	}

}
