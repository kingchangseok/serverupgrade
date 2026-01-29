package com.azsoft.ecams.ui.wizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.CheckOutJob;
import com.azsoft.ecams.ui.dialog.CheckOutDlg;
import com.azsoft.ecams.ui.wizard.page.CheckOutRequestPage;
import com.azsoft.ecams.ui.wizard.page.CheckOutSelectResourcePage;

public class CheckOutWizard extends Wizard implements INewWizard{
	private IResource[] resources, selectResource;
	private IStructuredSelection selection;

	private CheckOutDlg parentDialog;

	public CheckOutWizard(IResource[] resources) {
		this.resources = resources;
		setWindowTitle("\uccb4\ud06c\uc544\uc6c3");
	}
	
	public void addPages() {
		this.addPage(new CheckOutSelectResourcePage(resources));
		this.addPage(new CheckOutRequestPage());
	}

	public boolean performFinish() {
		if (parentDialog.getCurrentPage() instanceof CheckOutRequestPage){
			CheckOutRequestPage checkOutRequestPage = (CheckOutRequestPage) parentDialog.getCurrentPage();
			selectResource = checkOutRequestPage.getResourceSelectionTree().getSelectedResources();

			EcamsProviderPlugin.getPlugin().getJobManager().addJob(new CheckOutJob("CheckOut Request",selectResource,checkOutRequestPage.getText2().getText(),"","","", false));
			
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
		if (parentDialog.getCurrentPage() instanceof CheckOutSelectResourcePage){
			return false;
		}
		else if (parentDialog.getCurrentPage() instanceof CheckOutRequestPage){
			return true;
		}
		return super.canFinish();
	}
	


	public CheckOutDlg getParentDialog() {
		return parentDialog;
	}

	public void setParentDialog(CheckOutDlg parentDialog) {
		this.parentDialog = parentDialog;
	}

}
