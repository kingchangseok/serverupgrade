package com.azsoft.ecams.ui.wizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.RegistFileJob;
import com.azsoft.ecams.ui.dialog.RegistFileDlg;
import com.azsoft.ecams.ui.wizard.page.RegistFileSelectResourcePage;

public class RegistFileWizard extends Wizard implements INewWizard{


	private IResource[] resources;
	private IStructuredSelection selection;


	private RegistFileDlg parentDialog;
	

	public RegistFileWizard(IResource[] resources) {
		this.resources = resources;
		setWindowTitle("\ud504\ub85c\uadf8\ub7a8\uc2e0\uaddc\ub4f1\ub85d");
	}
	
	public void addPages() {
		this.addPage(new RegistFileSelectResourcePage(resources));
		//this.addPage(new RegistFileRequestPage());
	}

	public boolean performFinish() {
		/*if (parentDialog.getCurrentPage() instanceof RegistFileRequestPage){
			RegistFileRequestPage registFileRequestPage = (RegistFileRequestPage) parentDialog.getCurrentPage();
			
			String comment = registFileRequestPage.getComment();
			if(comment.length() < 1){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\ud504\ub85c\uadf8\ub7a8\uc124\uba85\uc744 \uc785\ub825\ud558\uc138\uc694.");
				messageBox.open();
				return false;
				comment = "\uac1c\ubcc4 \uc2e0\uaddc\ub4f1\ub85d";
			}
			
			
			if (registFileRequestPage.getJob().length() < 1){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\uc5c5\ubb34\ub97c \uc120\ud0dd\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
				messageBox.open();
				return false;
			}
			if (registFileRequestPage.getRsrc().length() < 1){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\ud504\ub85c\uadf8\ub7a8\uc885\ub958\ub97c \uc120\ud0dd\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
				messageBox.open();
				return false;
			}
			*/
			
			/* langage \uc0ac\uc6a9 \uc548\ud568. 20111216 hoyoon
			if(registFileRequestPage.getLang().length() < 1){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\uc5b8\uc5b4\ub97c \uc120\ud0dd\ud558\uc138\uc694.");
				messageBox.open();
				return false;
			}
			
			if (registFileRequestPage.getGrade().length() < 1){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\ud504\ub85c\uadf8\ub7a8\ub4f1\uae09\uc744 \uc120\ud0dd\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
				messageBox.open();
				return false;
			}*/
			RegistFileSelectResourcePage registFileRequestPage = (RegistFileSelectResourcePage)parentDialog.getCurrentPage();
			//IResource[] selectResource = registFileRequestPage.getResourceSelectionTree().getSelectedResources();
			IResource[] selectResource = (IResource[])registFileRequestPage.getSelectedResources();

			/* langage \uc0ac\uc6a9 \uc548\ud568. 20111216 hoyoon
			EcamsProviderPlugin.getPlugin().getJobManager().addJob(new RegistFileJob("RegistFile Request", 
					selectResource,registFileRequestPage.getJob().split(":")[0],registFileRequestPage.getGrade().split(":")[0],
					registFileRequestPage.getRsrc().split(":")[0],registFileRequestPage.getLang().split(":")[0],registFileRequestPage.getComment()));

			 */
			try{
				String job = selectResource[0].getProject().getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[0];
				//EcamsProviderPlugin.getPlugin().getJobManager().addJob(new RegistFileJob("RegistFile Request", 
				//		selectResource,registFileRequestPage.getJob().split(":")[0],"A",
				//		registFileRequestPage.getRsrc().split(":")[0],"",comment));
				EcamsProviderPlugin.getPlugin().getJobManager().addJob(new RegistFileJob("RegistFile Request",
						selectResource,job,"A","","","\uac1c\ubcc4 \uc2e0\uaddc\ub4f1\ub85d"));
			}catch(Exception e){
				return false;
			}
			return true;
		//}
		
		//return false;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		this.selection = selection;
	}
	
	public boolean canFinish() {
		// TODO Auto-generated method stub
		/*
		if (parentDialog.getCurrentPage() instanceof RegistFileSelectResourcePage){
			return false;
		}
		else if (parentDialog.getCurrentPage() instanceof RegistFileRequestPage){
			return true;
		}
		return super.canFinish();
		*/
		return true;
	}
	


	public RegistFileDlg getParentDialog() {
		return parentDialog;
	}

	public void setParentDialog(RegistFileDlg parentDialog) {
		this.parentDialog = parentDialog;
	}

}
