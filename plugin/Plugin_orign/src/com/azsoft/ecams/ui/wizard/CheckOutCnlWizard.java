package com.azsoft.ecams.ui.wizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.CheckOutCnlJob;
import com.azsoft.ecams.ui.dialog.CheckOutCnlDlg;
import com.azsoft.ecams.ui.wizard.page.CheckOutCnlSelectResourcePage;

public class CheckOutCnlWizard extends Wizard implements INewWizard{
	private IResource[] resources,selectResource;
	private IStructuredSelection selection;

	private CheckOutCnlDlg parentDialog;
	

	public CheckOutCnlWizard(IResource[] resources) {
		this.resources = resources;
		setWindowTitle("\uccb4\ud06c\uc544\uc6c3\ucde8\uc18c");
	}
	
	public void addPages() {
		this.addPage(new CheckOutCnlSelectResourcePage(resources));
	}

	public boolean performFinish() {
		if (parentDialog.getCurrentPage() instanceof CheckOutCnlSelectResourcePage){
			CheckOutCnlSelectResourcePage checkOutcnlSelectResourcePage = (CheckOutCnlSelectResourcePage) parentDialog.getCurrentPage();
			selectResource = (IResource[]) checkOutcnlSelectResourcePage.getSelectedResources();

			EcamsProviderPlugin.getPlugin().getJobManager().addJob(new CheckOutCnlJob("CheckOutCancel Request",selectResource,"","", false));
			
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
		if (parentDialog.getCurrentPage() instanceof CheckOutCnlSelectResourcePage){
			//return false;
			return true;
		}
		return super.canFinish();
	}

	public CheckOutCnlDlg getParentDialog() {
		return parentDialog;
	}
	
	public void setParentDialog(CheckOutCnlDlg parentDialog) {
		this.parentDialog = parentDialog;
	}
	

}
