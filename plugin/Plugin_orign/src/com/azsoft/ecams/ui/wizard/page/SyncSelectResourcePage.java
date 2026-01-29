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


public class SyncSelectResourcePage extends WizardPage {
	private IResource[] resources;
	
	private IDialogSettings settings;
	private ResourceSelectionTree resourceSelectionTree;
	
	private Object[] selectedResources;
	private IProject project;
	/**
	 * Create the wizard.
	 */
	public SyncSelectResourcePage(IResource[] resources) {
		super("wizardPage");
		this.resources = resources;
		settings = EcamsProviderPlugin.getPlugin().getDialogSettings();
		setPageComplete(false);
		setTitle("\ud504\ub85c\uadf8\ub7a8 \uc120\ud0dd");
		setDescription("\ubcc0\uacbd\uc0ac\ud56d\uc774 \uc788\ub294 \ud504\ub85c\uadf8\ub7a8\ubaa9\ub85d\uc785\ub2c8\ub2e4. \ub36e\uc5b4\uc4f0\uae30\ub97c \ud558\uc2dc\ub824\uba74 \ud504\ub85c\uadf8\ub7a8\uc744 \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.");
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
		resourceSelectionTree.setSize(582, 296);

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
	/*
	public Object[] getSelectedResources() {
		return selectedResources;
	}
	*/
	public Object[] getSelectedResources() {
		return selectedResources;
	}
	
	public ResourceSelectionTree getResourceSelectionTree() {
		return resourceSelectionTree;
	}
	
	public void setSelectedResources(Object[] selectedResources) {
		this.selectedResources = selectedResources;
		resourceSelectionTree.setResources((IResource[]) selectedResources);
		
		project = null;
		for (int i=0;i<((IResource[])selectedResources).length;i++){
			project = ((IResource[])selectedResources)[i].getProject();
			break;
		}
	}
	
}
