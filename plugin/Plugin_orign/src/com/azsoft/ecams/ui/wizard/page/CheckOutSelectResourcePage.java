package com.azsoft.ecams.ui.wizard.page;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;


public class CheckOutSelectResourcePage extends WizardPage {
	private IResource[] resources;
	
	private IDialogSettings settings;
	private ResourceSelectionTree resourceSelectionTree;
	
	private Object[] selectedResources;
	private IProject project;
	private String ip,port,id,pwd;
	
	/**
	 * Create the wizard.
	 */
	public CheckOutSelectResourcePage(IResource[] resources) {
		super("wizardPage");
		this.resources = resources;
		settings = EcamsProviderPlugin.getPlugin().getDialogSettings();
		setPageComplete(false);
		setTitle("\ud30c\uc77c\uc120\ud0dd");
		setDescription("\uccb4\ud06c\uc544\uc6c3\ud558\uc2e4 \ud30c\uc77c\uc744 \uc120\ud0dd\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		
		resourceSelectionTree = new ResourceSelectionTree(container, SWT.NONE, resources, true);
		resourceSelectionTree.setLocation(0, 0);
		resourceSelectionTree.setSize(582, 249);

		resourceSelectionTree.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selectedResources = resourceSelectionTree.getSelectedResources();
			}
		});
		
		((CheckboxTreeViewer)resourceSelectionTree.getTreeViewer()).addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				selectedResources = resourceSelectionTree.getSelectedResources();
			}
		});
	

		selectedResources = resourceSelectionTree.getSelectedResources();
		setPageComplete(true);
	}

	public Object[] getSelectedResources() {
		return selectedResources;
	}

}
