package com.azsoft.ecams.ui.dialog;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Text;

import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;


public class ErrorMessageDlg extends Dialog {
	private IResource[] inputResource;
	private ResourceSelectionTree resourceSelectionTree;
	private String title;
	private String message;
	private Text text;
	

	public ErrorMessageDlg(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public ErrorMessageDlg(Shell parentShell, IResource[] inputResoruce,String title,String message) {
		super(parentShell);
		setInputResource(inputResoruce);
		this.title = title;
		this.message = message;
		// TODO Auto-generated constructor stub
	}	

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("ERROR");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		/*Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FormLayout());*/	
		
		Composite container = new Composite(parent, SWT.NULL);
		
		Label err_label = new Label(container, SWT.NONE);
		err_label.setBounds(10, 10, 600, 20);
		/*FormData fd_err_label = new FormData();
		fd_err_label.top = new FormAttachment(0, 10);
		fd_err_label.left = new FormAttachment(0, 10);
		fd_err_label.bottom = new FormAttachment(0, 27);
		fd_err_label.right = new FormAttachment(0, 800);
		err_label.setLayoutData(fd_err_label);*/
		err_label.setText(title);

		text = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text.setEditable(false);
		text.setBounds(10, 30, 600, 100);
		/*FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(err_label, 6);
		fd_text.bottom = new FormAttachment(100, -150);
		fd_text.left = new FormAttachment(0, 10);
		fd_text.right = new FormAttachment(0, 800);
		text.setLayoutData(fd_text);*/
		text.setText(message);
		
		resourceSelectionTree = new ResourceSelectionTree(container, SWT.NONE, null, false);
		resourceSelectionTree.setBounds(5, 140, 600, 184);
		/*FormData fd_resourceSelectionTree = new FormData();
		fd_resourceSelectionTree.top = new FormAttachment(text, 6);
		fd_resourceSelectionTree.left = new FormAttachment(0, 10);
		fd_resourceSelectionTree.right = new FormAttachment(100, -10);
		fd_resourceSelectionTree.bottom = new FormAttachment(100, -10);
		resourceSelectionTree.setLayoutData(fd_resourceSelectionTree);*/
		if (inputResource != null){
			resourceSelectionTree.setResources(inputResource);
		}
		
		//resourceSelectionTree.setLocation(10, 136);
		//resourceSelectionTree.setSize(562, 150);
		
		return container;
	}
		
	public void setInputResource(IResource[] inputResource){
		this.inputResource = inputResource;
	}
}
