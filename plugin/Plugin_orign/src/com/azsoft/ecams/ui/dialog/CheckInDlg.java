package com.azsoft.ecams.ui.dialog;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.azsoft.ecams.ui.wizard.page.CheckInRequestPage;
import com.azsoft.ecams.ui.wizard.page.CheckInSelectResourcePage;

public class CheckInDlg extends WizardDialog implements IPageChangedListener
{
	/**
	 * @wbp.parser.constructor
	 */
	private CheckInSelectResourcePage page1;
	private CheckInRequestPage page2;
	
	Object[] selectedResource;
	


	public CheckInDlg(Shell parentShell, IWizard newWizard) {
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
		// TODO Auto-generated method stub
		page1 = (CheckInSelectResourcePage)getCurrentPage();
		this.selectedResource = page1.getSelectedResources();
		if (selectedResource.length < 1){
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
			messageBox.setMessage("\uc790\uc6d0\uc744 \uc120\ud0dd\ud558\uc5ec\uc8fc\uc2ed\uc2dc\uc694.");
			messageBox.open();
			return;
		}
		
		page2 = (CheckInRequestPage)page1.getNextPage();
		if (selectedResource != null && selectedResource instanceof IResource[]){
			page2.setSelectedResources(selectedResource);
		}
		
		
		super.nextPressed();
	}

	public void pageChanged(PageChangedEvent event) {
		// TODO Auto-generated method stub
		
	}

}
