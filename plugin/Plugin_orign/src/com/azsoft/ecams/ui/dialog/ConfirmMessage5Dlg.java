package com.azsoft.ecams.ui.dialog;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;


public class ConfirmMessage5Dlg extends Dialog {
	private String message;
	private Shell parentShell;
	private String strValue = "N";
	
	public ConfirmMessage5Dlg(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public ConfirmMessage5Dlg(Shell parentShell,String message) {
		super(parentShell);
		this.parentShell = parentShell;
		this.message = message;
		// TODO Auto-generated constructor stub
	}	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("\ud655\uc778\uba54\uc138\uc9c0");
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button yesBtn = super.createButton(parent, IDialogConstants.YES_ID, IDialogConstants.YES_LABEL,true);
		Button noBtn = super.createButton(parent, IDialogConstants.NO_ID, IDialogConstants.NO_LABEL, false);
		
		yesBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				strValue = "1"; //\ud56d\uc0c1\uc608
				parentShell.dispose();
			}
		});
		
		noBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				strValue = "4"; //\uc544\ub2c8\uc694
				parentShell.dispose();
			}
		});
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FormLayout());		
	
		Text text = new Text(container, SWT.WRAP);
		text.setEditable(false);
		FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(0, 20);
		fd_text.bottom = new FormAttachment(100, -10);
		fd_text.left = new FormAttachment(0, 10);
		fd_text.right = new FormAttachment(100, -10);
		text.setLayoutData(fd_text);
		text.setText("\uc120\ud0dd\ud558\uc2e0 \ud504\ub85c\uadf8\ub7a8\uc740 \ucd5c\uc885\ubc84\uc804\uc73c\ub85c \ub36e\uc5b4\uc4f0\uae30 \ub429\ub2c8\ub2e4. \uacc4\uc18d\uc9c4\ud589 \ud558\uc2dc\uaca0\uc2b5\ub2c8\uae4c?");
		
		return container;
	}
	
	public String getChkAns(){
		return strValue;
	}
}
