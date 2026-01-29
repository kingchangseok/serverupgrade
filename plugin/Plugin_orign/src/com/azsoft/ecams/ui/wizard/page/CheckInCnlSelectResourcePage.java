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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;
import org.eclipse.swt.widgets.Label;
import com.swtdesigner.SWTResourceManager;
import org.eclipse.swt.widgets.Combo;


public class CheckInCnlSelectResourcePage extends WizardPage {
	private IResource[] resources;
	private IResource[] resources2;
	private static Combo combo;
	
	private ReturnMsg returnMsg1;
	private int sel = 0;
	
	private IDialogSettings settings;
	private ResourceSelectionTree resourceSelectionTree;
	
	private Object[] selectedResources;
	private IProject project;

	private SelectionListener combo_Listener;
	/**
	 * Create the wizard.
	 */
	public CheckInCnlSelectResourcePage(IResource[] resources) {
		super("wizardPage");
		this.resources = resources;
		settings = EcamsProviderPlugin.getPlugin().getDialogSettings();
		setPageComplete(false);
		setTitle("CSR/\ud30c\uc77c\uc120\ud0dd");
		setDescription("\ucde8\uc18c\ud558\uc2e4 CSR\ubc88\ud638\ub97c \uc120\ud0dd\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		setupCallbacks();
		
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		
		Label label = new Label(container, SWT.NONE);
		label.setText("CSR\ubc88\ud638");
		label.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label.setBounds(10, 10, 56, 12);
		
		combo = new Combo(container, SWT.NONE);
		combo.setEnabled(true);
		combo.setBounds(72, 7, 500, 20);
		
		
		resourceSelectionTree = new ResourceSelectionTree(container, SWT.NONE, resources, true);
		resourceSelectionTree.setLocation(0, 33);
		resourceSelectionTree.setSize(582, 263);
		
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
	
	public void setupCallbacks()
	{
	
		combo_Listener = new SelectionListener(){
	
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
	
			public void widgetSelected(SelectionEvent e) {
				resources2 = resources;
				selectedResources = resourceSelectionTree.getSelectedResources();	
			}
		};
		
	}

	public Object[] getSelectedResources() {
		return selectedResources;
	}
	
	public String getCSRNO() {
		String result=null;
		result=returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(sel).getScsrno()+":"+returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(sel).getScsrtitle()+":"
		+returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(sel).getSbizcode()+":"+returnMsg1.getEcamsmsg().getPrjinfolist().getPrjinfo(sel).getSemerrequestyn();
		return result;
	}
}