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


public class ConfirmMessage4Dlg extends Dialog {
	private String message;
	private Shell parentShell;
	private Button chkAns;
	private String strValue = "0";
	
	public ConfirmMessage4Dlg(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public ConfirmMessage4Dlg(Shell parentShell,String message) {
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
				if(chkAns.getSelection()){
					strValue = "1"; //항상예
				}
				parentShell.dispose();
			}
		});
		
		noBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if(chkAns.getSelection()){
					strValue = "3"; //항상아니요
				}else{
					strValue = "4"; //아니요
				}
				parentShell.dispose();
			}
		});
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FormLayout());		
		
		chkAns = new Button(container, SWT.CHECK);
		FormData fd_chkAns = new FormData();
		chkAns.setLayoutData(fd_chkAns);
		chkAns.setBounds(10, 12, 94, 16);
		chkAns.setText("\ud604\uc7ac \uc791\uc5c5\uc5d0\uc11c \ub2e4\uc2dc\ubb3b\uc9c0\uc54a\uc74c");
		
		Text text = new Text(container, SWT.WRAP);
		fd_chkAns.bottom = new FormAttachment(text, -6);
		fd_chkAns.left = new FormAttachment(text, 0, SWT.LEFT);
		text.setEditable(false);
		FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(0, 47);
		fd_text.bottom = new FormAttachment(100, -10);
		fd_text.left = new FormAttachment(0, 10);
		fd_text.right = new FormAttachment(100, -10);
		text.setLayoutData(fd_text);
		text.setText(message+"\ub294 \ucd5c\uc885\uc18c\uc2a4\uac00 \uc544\ub2d9\ub2c8\ub2e4.\n\ucd5c\uc885 \uc18c\uc2a4\ub85c \ub36e\uc5b4\uc4f0\uc2dc\uaca0\uc2b5\ub2c8\uae4c?");
		
		return container;
	}
	
	public String getChkAns(){
		return strValue;
	}
}
