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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;


public class ConfirmReqGbnDlg extends Dialog {
	private Shell parentShell;
	private Button btnRadioButton1;
	private Button btnRadioButton2;
	private Button btnRadioButton3;
	private String strValue = "0";
	
	public ConfirmReqGbnDlg(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("\uc801\uc6a9\ubc29\ubc95");
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button OKBtn = super.createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,true);
		
		OKBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if(btnRadioButton1.getSelection()){
					strValue = "V";//\ubc84\uc804\ub9cc \uc62c\ub9bc
				}else if(btnRadioButton2.getSelection()){
					strValue = "D";//\uac1c\ubc1c\uc11c\ubc84\ub9cc \uc801\uc6a9
				}else{
					strValue = "A";//\ubaa8\ub450 \uc801\uc6a9
				}
				parentShell.dispose();
			}
		});
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FormLayout());		
		
		Text text = new Text(container, SWT.WRAP);
		FormData fd_text = new FormData();
		text.setLayoutData(fd_text);
		text.setEditable(false);
		text.setText("\uc801\uc6a9\ubc29\ubc95\uc744 \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.\n(ESC \ub610\ub294 \ub2eb\uae30\ub97c \ub204\ub97c \uacbd\uc6b0 \ubaa8\ub450\uc801\uc6a9\uc73c\ub85c \uc9c4\ud589\ub429\ub2c8\ub2e4.)");
		
		btnRadioButton1 = new Button(container, SWT.RADIO);
		fd_text.bottom = new FormAttachment(btnRadioButton1, -14);
		btnRadioButton1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(btnRadioButton1.getSelection()){
					btnRadioButton2.setSelection(false);
					btnRadioButton3.setSelection(false);
				}
			}
		});
		FormData fd_btnRadioButton1 = new FormData();
		fd_btnRadioButton1.bottom = new FormAttachment(100, -42);
		btnRadioButton1.setLayoutData(fd_btnRadioButton1);
		btnRadioButton1.setSelection(false);
		btnRadioButton1.setToolTipText("\uccb4\ud06c\uc778\uc644\ub8cc \uc2dc \ubc84\uc804\ub9cc \uc62c\ub9ac\uace0, \uac1c\ubc1c\uc11c\ubc84 \uc801\uc6a9\uc740 \ud558\uc9c0\uc54a\uc74c");
		btnRadioButton1.setText("\uac1c\ubc1c\uc11c\ubc84 \ubbf8\uc801\uc6a9");

		btnRadioButton2 = new Button(container, SWT.RADIO);
		fd_text.right = new FormAttachment(btnRadioButton2, 25, SWT.RIGHT);
		btnRadioButton2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(btnRadioButton2.getSelection()){
					btnRadioButton1.setSelection(false);
					btnRadioButton3.setSelection(false);
				}
			}
		});
		FormData fd_btnRadioButton2 = new FormData();
		fd_btnRadioButton2.bottom = new FormAttachment(100, -42);
		fd_btnRadioButton2.top = new FormAttachment(btnRadioButton1, 0, SWT.TOP);
		fd_btnRadioButton2.left = new FormAttachment(btnRadioButton1, 6);
		btnRadioButton2.setLayoutData(fd_btnRadioButton2);
		btnRadioButton2.setSelection(false);
		btnRadioButton2.setToolTipText("\uccb4\ud06c\uc778\uc644\ub8cc \uc2dc \ubc84\uc804\uc740 \uc62c\ub9ac\uc9c0\uc54a\uace0, \uac1c\ubc1c\uc11c\ubc84 \uc801\uc6a9\uc9c4\ud589");
		btnRadioButton2.setText("\ubc84\uc804\uc5c5 \ubbf8\uc801\uc6a9");
		
		btnRadioButton3 = new Button(container, SWT.RADIO);
		fd_text.left = new FormAttachment(btnRadioButton3, 0, SWT.LEFT);
		fd_btnRadioButton1.left = new FormAttachment(btnRadioButton3, 6);
		btnRadioButton3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(btnRadioButton3.getSelection()){
					btnRadioButton1.setSelection(false);
					btnRadioButton2.setSelection(false);
				}
			}
		});
		FormData fd_btnRadioButton3 = new FormData();
		fd_btnRadioButton3.bottom = new FormAttachment(100, -42);
		fd_btnRadioButton3.left = new FormAttachment(0, 26);
		btnRadioButton3.setLayoutData(fd_btnRadioButton3);
		btnRadioButton3.setSelection(true);
		btnRadioButton3.setToolTipText("\uccb4\ud06c\uc778\uc644\ub8cc \uc2dc \ubc84\uc804\uc744 \uc62c\ub9ac\uace0, \uac1c\ubc1c\uc11c\ubc84 \uc801\uc6a9\ub3c4 \uc9c4\ud589");
		btnRadioButton3.setText("\ubaa8\ub450\uc801\uc6a9");
		
		return container;
	}
	
	public String getReqGbn(){
		return strValue;
	}
}
