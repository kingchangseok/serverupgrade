package com.azsoft.ecams.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;

public class ConfirmMessageDlg extends Dialog {
	private String title;
	private String message;
	private Text text;
	

	public ConfirmMessageDlg(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public ConfirmMessageDlg(Shell parentShell,String title,String message) {
		super(parentShell);
		this.title = title;
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
		Button okButton = super.createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,true);
		Button cancelButton = super.createButton(parent, IDialogConstants.CANCEL_ID,IDialogConstants.CANCEL_LABEL, false);
		
//		okButton.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseDown(MouseEvent e) {
//				CheckInRealRequestPage.setchkBefSelection(false);
//			}
//		});
		
//		cancelButton.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseDown(MouseEvent e) {
//				CheckInRealRequestPage.setChkBefSelection(true);
//			}
//		});
	}
	
	
//	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
//		Button button = super.createButton(parent, id, label, defaultButton);
//		        if (id == IDialogConstants.OK_ID) {
//		                okButton = button; 
//		                okButton.setEnabled(false);
//		        }
//		return button;
//	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FormLayout());		
		
		Label err_label = new Label(container, SWT.NONE);
		FormData fd_err_label = new FormData();
		fd_err_label.top = new FormAttachment(0, 10);
		fd_err_label.left = new FormAttachment(0, 10);
		fd_err_label.bottom = new FormAttachment(0, 27);
		err_label.setLayoutData(fd_err_label);
		err_label.setText(title);
		
		text = new Text(container, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
		//text.setBackground(SWT.COLOR_WHITE);
		fd_err_label.right = new FormAttachment(text, 0, SWT.RIGHT);
		text.setEditable(false);
		FormData fd_text = new FormData();
		fd_text.bottom = new FormAttachment(100, -10);
		fd_text.top = new FormAttachment(err_label, 6);
		fd_text.left = new FormAttachment(0, 10);
		fd_text.right = new FormAttachment(0, 434);
		text.setLayoutData(fd_text);
		text.setText(message);
		//resourceSelectionTree.setLocation(10, 136);
		//resourceSelectionTree.setSize(562, 150);
		
		return container;
	}
}
