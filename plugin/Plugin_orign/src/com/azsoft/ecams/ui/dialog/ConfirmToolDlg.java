package com.azsoft.ecams.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.properties.WorkspacePreferences;

public class ConfirmToolDlg extends Dialog {
	protected WorkspacePreferences m_store;
	
	private Combo tool_combo;
	private Shell parentShell;
	
	public ConfirmToolDlg(Shell parentShell) {
		super(parentShell);
		this.parentShell = parentShell;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Tool Selection");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button okBtn = super.createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,true);
		
		okBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if(tool_combo.getSelectionIndex() == 0){
					m_store.putString(IProperty.TOOL,"I");
				}else{
					m_store.putString(IProperty.TOOL,"E");
				}
				m_store.save();
				//parentShell.dispose();
				close();
			}
			
		});
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		m_store = new WorkspacePreferences();
		
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new FormLayout());		
		
		tool_combo = new Combo(container, SWT.NONE);
		FormData fd_tool_combo = new FormData();
		fd_tool_combo.top = new FormAttachment(0, 36);
		fd_tool_combo.left = new FormAttachment(0, 10);
		fd_tool_combo.bottom = new FormAttachment(0, 86);
		fd_tool_combo.right = new FormAttachment(100, -10);
		fd_tool_combo.width = 200;
		tool_combo.setLayoutData(fd_tool_combo);
		tool_combo.add("iStudio");
		tool_combo.add("eClipse");
		tool_combo.select(0);
		
		return container;
	}
}
