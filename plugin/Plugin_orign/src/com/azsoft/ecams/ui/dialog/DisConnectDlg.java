package com.azsoft.ecams.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Label;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.UnSyncJob;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;

public class DisConnectDlg extends Dialog {
	private IResource[] inputResource;
	private Button btnRadioButton;
	private Button btnRadioButton_1;
	
	public DisConnectDlg(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	public DisConnectDlg(Shell parentShell, IResource[] inputResoruce) {
		super(parentShell);
		this.inputResource = inputResoruce;
		// TODO Auto-generated constructor stub
	}	

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Confirm Disconnect from eCAMS");
	}

	protected void createButtonsForButtonBar(Composite parent) {
		Button btnY = createButton(parent, IDialogConstants.YES_ID,IDialogConstants.YES_LABEL, false);
		createButton(parent, IDialogConstants.CANCEL_ID,IDialogConstants.NO_LABEL, false);
		btnY.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				try {
					((IProject)inputResource[0].getProject()).setPersistentProperty(new QualifiedName("Properties","useyn"), "false");
					((IProject)inputResource[0].getProject()).setPersistentProperty(new QualifiedName("Properties","autosync"), "false");
					((IProject)inputResource[0].getProject()).setPersistentProperty(new QualifiedName("Properties","ischanged"), "0");
					if (EcamsRepositoryProvider.isManagedByEcams((IProject)inputResource[0].getProject())){
						EcamsRepositoryProvider.unsetManagedByEcams((IProject)inputResource[0].getProject());
					}
					if(btnRadioButton.getSelection()) {
						EcamsProviderPlugin.getPlugin().getJobManager().addJob(new UnSyncJob("eCAMS Disconnecting...",(IProject)inputResource[0].getProject()));
					}
					List projectList = new ArrayList();
					projectList.add((IProject)inputResource[0].getProject());
					EcamsProviderPlugin.broadcastModificationStateChanges((IResource[]) projectList.toArray(new IResource[projectList.size()]));
					close();
				} catch (CoreException e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		btnRadioButton = new Button(container, SWT.RADIO);
		btnRadioButton.setBounds(20, 47, 547, 27);
		btnRadioButton.setText("Also delete the eCAMS meta-information from the file system");

		btnRadioButton_1 = new Button(container, SWT.RADIO);
		btnRadioButton_1.setBounds(20, 70, 547, 27);
		btnRadioButton_1.setText("Do not delete the eCAMS meta-information (e.g. .eCAMS subdirectories)");

		Label label = new Label(container, SWT.NONE);
		label.setBounds(10, 22, 394, 12);
		label.setText("Are you sure you want to disconnect [" + inputResource[0].getProject() + "] project(s) from eCAMS");
		
		return container;
	}
}
