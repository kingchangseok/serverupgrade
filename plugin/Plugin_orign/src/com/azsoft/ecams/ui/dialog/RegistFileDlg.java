package com.azsoft.ecams.ui.dialog;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.azsoft.ecams.ui.wizard.page.RegistFileSelectResourcePage;

public class RegistFileDlg extends WizardDialog implements IPageChangedListener{
	/**
	 * @wbp.parser.constructor
	 */
	private RegistFileSelectResourcePage page1;
	//private RegistFileRequestPage page2;
	
	Object[] selectedResource;
	


	public RegistFileDlg(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
		// TODO Auto-generated constructor stub
	}


	protected void backPressed() {
		// TODO Auto-generated method stub
		selectedResource = null;
		super.backPressed();
	}

	protected void finishPressed() {
		// TODO Auto-generated method stub
		super.finishPressed();
	}	

	protected void nextPressed() {
		page1 = (RegistFileSelectResourcePage)getCurrentPage();
		this.selectedResource = page1.getSelectedResources();
		if (selectedResource.length < 1){
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
			messageBox.setMessage("\uc790\uc6d0\uc744 \uc120\ud0dd\ud558\uc5ec\uc8fc\uc2ed\uc2dc\uc694.");
			messageBox.open();
			return;
		}
		/*
		page2 = (RegistFileRequestPage)page1.getNextPage();
		if (selectedResource != null && selectedResource instanceof IResource[]){
			page2.setSelectedResources(selectedResource);
		}
		super.nextPressed();
		*/
	}

	public void pageChanged(PageChangedEvent event) {
		// TODO Auto-generated method stub
		
	}
}
