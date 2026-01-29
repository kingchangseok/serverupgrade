package com.azsoft.ecams.ui.wizard;


import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.CheckInJob;
import com.azsoft.ecams.core.jobs.RegistFileAllJob;
import com.azsoft.ecams.core.jobs.RegistFileNewJob;
import com.azsoft.ecams.ui.dialog.RegistFileAllDlg;
import com.azsoft.ecams.ui.wizard.page.RegistFileAllPage;

public class RegistFileAllWizard extends Wizard implements INewWizard{
	private IResource[] resources,selectResource;
	private IStructuredSelection selection;
	private String		gbnCd = "";
	//private IProject project;
	private RegistFileAllDlg parentDialog;
	public RegistFileAllWizard(IResource[] resources, String gbnCd) {
		this.resources = resources;
		this.gbnCd = gbnCd;
		
		if( "ALL".equals(this.gbnCd) ) {
			setWindowTitle("\ucd5c\ucd08\uc77c\uad04\ub4f1\ub85d");
		} else if( "NEW".equals(this.gbnCd) ) {
			setWindowTitle("\uc2e0\uaddc\ub4f1\ub85d");
		} else {}
	}
	
	public void addPages() {
		this.addPage(new RegistFileAllPage(resources, gbnCd));
		//this.addPage(new CheckInRequestPage());
	}

	public boolean performFinish() {
		//CheckInRequestPage checkInRequestPage = (CheckInRequestPage) parentDialog.getCurrentPage();
		//if (parentDialog.getCurrentPage() instanceof CheckInRequestPage){
			/*if (checkInRequestPage.getText2().getText().length() < 1){
				MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
				messageBox.setMessage("\uc2e0\uccad\uc0ac\uc720\ub97c \uc785\ub825\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
				messageBox.open();
				return false;
			}*/
		RegistFileAllPage registFileAllPage = (RegistFileAllPage) parentDialog.getCurrentPage();
		Hashtable<Integer, Hashtable<String, Object> > registFileAllResource = (Hashtable<Integer, Hashtable<String, Object> >)registFileAllPage.getRegAllResources();
		if( registFileAllResource.size() > 0 ) {
			if( "ALL".equals(gbnCd) ) {
				EcamsProviderPlugin.getPlugin().getJobManager().addJob(new RegistFileAllJob("RegistFileAll Request",registFileAllResource,"16"));
			} else if( "NEW".equals(gbnCd) ){
				EcamsProviderPlugin.getPlugin().getJobManager().addJob(new RegistFileNewJob("RegistFileNew Request",registFileAllResource));
			} else {}
		}
//		selectResource = (IResource[])checkInRequestAllPage.getSelectedResources();
//		EcamsProviderPlugin.getPlugin().getJobManager().addJob(new CheckInJob("CheckIn Request",selectResource,"\ucd5c\ucd08\uc77c\uad04\ub4f1\ub85d","16","","","V", false));
			
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
		/*if (parentDialog.getCurrentPage() instanceof CheckInSelectResourcePage){
			return false;
		}
		else if (parentDialog.getCurrentPage() instanceof CheckInRequestPage){
			return true;
		}*/
		if (parentDialog.getCurrentPage() instanceof RegistFileAllPage){
			return true;
		}
		return super.canFinish();
	}

	public RegistFileAllDlg getParentDialog() {
		return parentDialog;
	}

	public void setParentDialog(RegistFileAllDlg parentDialog) {
		this.parentDialog = parentDialog;
	}

}
