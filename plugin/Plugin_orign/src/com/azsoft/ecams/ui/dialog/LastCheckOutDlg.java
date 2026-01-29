package com.azsoft.ecams.ui.dialog;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import com.azsoft.ecams.ui.wizard.page.LastCheckOutSelectResourcePage;

public class LastCheckOutDlg extends WizardDialog implements IPageChangedListener {
	
	private LastCheckOutSelectResourcePage page1;
	//private LastCheckOutRequestPage page2;
	
	Object[] selectedResource;
	
	public LastCheckOutDlg(Shell parentShell, IWizard newWizard) {
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
		page1 = (LastCheckOutSelectResourcePage)getCurrentPage();
		this.selectedResource = page1.getSelectedResources();
		if (selectedResource.length < 1){
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
			messageBox.setMessage("\uc790\uc6d0\uc744 \uc120\ud0dd\ud558\uc5ec\uc8fc\uc2ed\uc2dc\uc694.");
			messageBox.open();
			return;
		}
		super.finishPressed();
	}	

	protected void nextPressed() {
		// TODO Auto-generated method stub
	}

	public void pageChanged(PageChangedEvent event) {
		// TODO Auto-generated method stub
	}

}
