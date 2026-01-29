package com.azsoft.ecams.ui.wizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.DeleteJob;
import com.azsoft.ecams.ui.dialog.DeleteDlg;
import com.azsoft.ecams.ui.wizard.page.DeleteResourcePage;

public class DeleteWizard extends Wizard implements INewWizard{
	private IResource[] resources, selectResource;
	private IStructuredSelection selection;

	private DeleteDlg parentDialog;

	public DeleteWizard(IResource[] resources, int len) {
		this.resources = resources;
		setWindowTitle("\ud30c\uc77c\uc0ad\uc81c");
	}
	
	public void addPages() {
		this.addPage(new DeleteResourcePage(resources));
	}

	public boolean performFinish() {
		if (parentDialog.getCurrentPage() instanceof DeleteResourcePage){
			DeleteResourcePage deleteRequestPage = (DeleteResourcePage) parentDialog.getCurrentPage();
			selectResource = (IResource[])deleteRequestPage.getSelectedResources();
			
			EcamsProviderPlugin.getPlugin().getJobManager().addJob(new DeleteJob("Delete Request",selectResource));
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
		if (parentDialog.getCurrentPage() instanceof DeleteResourcePage){
			return true;
		}
		return super.canFinish();
	}
	
	public DeleteDlg getParentDialog() {
		return parentDialog;
	}

	public void setParentDialog(DeleteDlg parentDialog) {
		this.parentDialog = parentDialog;
	}

}
