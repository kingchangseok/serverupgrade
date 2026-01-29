package com.azsoft.ecams.ui.dialog;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.azsoft.ecams.ui.wizard.page.RegistFileAllPage;
import com.azsoft.ecams.ui.wizard.page.CheckInSelectResourcePage;

public class RegistFileAllDlg extends WizardDialog implements IPageChangedListener
{
	/**
	 * @wbp.parser.constructor
	 */
	private RegistFileAllPage page1;
	//private CheckInSelectResourcePage page1;
	//private CheckInRequestPage page2;
	
	Object[] selectedResource;
	//public String CSRNO;
	


	public RegistFileAllDlg(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
		this.setHelpAvailable(false);
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
//		page1 = (CheckInRegstAllPage)getCurrentPage();
//		this.selectedResource = page1.getSelectedResources();
//		//this.CSRNO = page1.getCSRNO();
//		if (selectedResource.length < 1){
//			MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
//			messageBox.setMessage("\uc790\uc6d0\uc744 \uc120\ud0dd\ud558\uc5ec\uc8fc\uc2ed\uc2dc\uc694.");
//			messageBox.open();
//			return;
//		}
		/*if(CSRNO.equals("X")){
			MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
			messageBox.setMessage("SPMS\ubc88\ud638\ub97c \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.");
			messageBox.open();
			return;
		}*/
		/*
		page2 = (CheckInRequestPage)page1.getNextPage();
		if (selectedResource != null && selectedResource instanceof IResource[]){
			page2.setSelectedResources(selectedResource);
			//page2.setCSRNO(CSRNO);
		}
		*/
		super.nextPressed();
	}

	public void pageChanged(PageChangedEvent event) {
		// TODO Auto-generated method stub
		
	}

}
