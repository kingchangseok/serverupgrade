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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Label;


public class ConfirmMergeMessageDlg extends Dialog {
	private String mergeEnable;
	private String message;
	private Shell parentShell;
	private Button chkAllways;
	private Button radioReplace;
	private Button radioMerge;
	private Button radioNoAction;
	
	
	final static public int CONFIRM_INIT			= 0; //초기화
	final static public int CONFIRM_NO_ACTION		= 1; //그대로두기
	final static public int CONFIRM_NO_ACTION_ALL	= 2; //항상 그대로두기
	final static public int CONFIRM_REPLACE			= 3; //덮어쓰기
	final static public int CONFIRM_REPLACE_ALL		= 4; //항상덮어쓰기
	final static public int CONFIRM_MERGE			= 5; //병합하기
	final static public int CONFIRM_MERGE_ALL		= 6; //항상병합하기
	
	private int iStatus = CONFIRM_INIT;
	//private String strValue = "0";
	
	public ConfirmMergeMessageDlg(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @wbp.parser.constructor
	 */
	public ConfirmMergeMessageDlg(Shell parentShell, String message, String mergeEnable) {
		super(parentShell);
		this.parentShell = parentShell;
		this.message = message;
		this.mergeEnable = mergeEnable;
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
				
				if( chkAllways.getSelection() ){ //\ud56d\uc0c1
					if( radioReplace.getSelection() ) {
						iStatus = CONFIRM_REPLACE_ALL; //\ud56d\uc0c1 \ub36e\uc5b4\uc4f0\uae30
					} else if ( radioMerge.getSelection() ) {
						iStatus = CONFIRM_MERGE_ALL; //\ud56d\uc0c1 \ubcd1\ud569
					} else {
						iStatus = CONFIRM_NO_ACTION_ALL; //\ud56d\uc0c1 \uadf8\ub300\ub85c\ub450\uae30
					}					
				} else {
					if( radioReplace.getSelection() ) {
						iStatus = CONFIRM_REPLACE; //\ud55c\ubc88 \ub36e\uc5b4\uc4f0\uae30
					} else if ( radioMerge.getSelection() ) {
						iStatus = CONFIRM_MERGE; //\ud55c\ubc88 \ubcd1\ud569
					} else {
						iStatus = CONFIRM_NO_ACTION; // \ud55c\ubc88 \uadf8\ub300\ub85c \ub450\uae30
					}	
				}
				parentShell.dispose();
			}
		});
		
		noBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if( chkAllways.getSelection() ){
					iStatus = CONFIRM_NO_ACTION_ALL; //\ud56d\uc0c1 \uadf8\ub300\ub85c\ub450\uae30
				}else{
					iStatus = CONFIRM_NO_ACTION; // \ud55c\ubc88 \uadf8\ub300\ub85c \ub450\uae30
				}
				parentShell.dispose();
			}
		});
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FormLayout());		
		
		chkAllways = new Button(container, SWT.CHECK);
		chkAllways.setToolTipText("");
		FormData fd_chkAns = new FormData();
		fd_chkAns.left = new FormAttachment(0, 5);
		chkAllways.setLayoutData(fd_chkAns);
		chkAllways.setBounds(0, 12, 94, 16);
		chkAllways.setText("\ud604\uc7ac \uc791\uc5c5\uc5d0\uc11c \ub2e4\uc2dc\ubb3b\uc9c0\uc54a\uc74c");
		
		Text text = new Text(container, SWT.WRAP);
		text.setEditable(false);
		FormData fd_text = new FormData();
		fd_text.top = new FormAttachment(0, 10);
		fd_text.left = new FormAttachment(0, 10);
		fd_text.right = new FormAttachment(100, -10);
		text.setLayoutData(fd_text);
		text.setText(message+"\ub294 \ucd5c\uc885\uc18c\uc2a4\uac00 \uc544\ub2d9\ub2c8\ub2e4.\n\uc5b4\ub5bb\uac8c \ud558\uc2dc\uaca0\uc2b5\ub2c8\uae4c?");
		
		Group group = new Group(container, SWT.NONE);
		fd_text.bottom = new FormAttachment(group, -6);
		fd_chkAns.top = new FormAttachment(0, 128);
		FormData fd_group = new FormData();
		fd_group.left = new FormAttachment(0);
		fd_group.right = new FormAttachment(100);
		fd_group.bottom = new FormAttachment(chkAllways, -6);
		fd_group.top = new FormAttachment(0, 90);
		group.setLayoutData(fd_group);
		
		radioReplace = new Button(group, SWT.RADIO);
		radioReplace.setLocation(10, 10);
		radioReplace.setSize(69, 16);
		radioReplace.setToolTipText("");
		radioReplace.setSelection(true);
		radioReplace.setText("\ub36e\uc5b4\uc4f0\uae30");
		
		radioMerge = new Button(group, SWT.RADIO);
		radioMerge.setLocation(95, 10);
		radioMerge.setSize(69, 16);
		radioMerge.setToolTipText("");
		radioMerge.setText("\ubcd1\ud569\ud558\uae30");
		radioMerge.setVisible(false);
		
		radioNoAction = new Button(group, SWT.RADIO);
		//radioNoAction.setBounds(172, 10, 92, 16);
		radioNoAction.setBounds(95, 10, 92, 16);
		radioNoAction.setText("\uadf8\ub300\ub85c\ub450\uae30");
		
		Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_label = new FormData();
		fd_label.top = new FormAttachment(chkAllways, 6);
		fd_label.left = new FormAttachment(group, 0, SWT.LEFT);
		fd_label.right = new FormAttachment(group, 0, SWT.RIGHT);
		fd_label.bottom = new FormAttachment(100, -7);
		label.setLayoutData(fd_label);
		
		if( "N".equals(mergeEnable) ) {
			radioMerge.setEnabled(false);
		}
		
		return container;
	}
	
	public int getChkAllways(){
		return iStatus;
	}
}
