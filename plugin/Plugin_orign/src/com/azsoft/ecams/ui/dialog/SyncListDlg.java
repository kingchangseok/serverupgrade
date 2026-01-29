package com.azsoft.ecams.ui.dialog;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.azsoft.ecams.ui.wizard.page.SyncSelectResourcePage;

public class SyncListDlg extends WizardDialog implements IPageChangedListener
{
	/**
	 * @wbp.parser.constructor
	 */
	//private CheckInSelectResourcePage page1;
	//private CheckInRequestPage page2;
	private SyncSelectResourcePage page1;
	
	Object[] selectedResource;
	


	public SyncListDlg(Shell parentShell, IWizard newWizard) {
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
		page1 = (SyncSelectResourcePage)getCurrentPage();
		this.selectedResource = page1.getSelectedResources();
		/*if (selectedResource.length < 1){
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
			messageBox.setMessage("\ucd5c\uc885\ubc84\uc804\uc73c\ub85c \ub36e\uc5b4\uc4f0\uae30\ud560 \ud504\ub85c\uadf8\ub7a8\uc744 \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.");
			messageBox.open();
			return;
		}*/
		super.finishPressed();
	}	

	protected void nextPressed() {
		// TODO Auto-generated method stub
	}

	public void pageChanged(PageChangedEvent event) {
		// TODO Auto-generated method stub
	}

}
