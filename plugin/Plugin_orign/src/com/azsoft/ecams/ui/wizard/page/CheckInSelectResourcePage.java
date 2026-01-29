package com.azsoft.ecams.ui.wizard.page;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;


public class CheckInSelectResourcePage extends WizardPage {
	private Logger logger = Logger.getLogger(this.getClass());
	private IResource[] resources;
	private Button chkModify;
	private MouseListener chkModifyButtonCheck_Listener;
	
	private IDialogSettings settings;
	private ResourceSelectionTree resourceSelectionTree;
	private Object[] selectedResources;
	
	/**
	 * Create the wizard.
	 */
	public CheckInSelectResourcePage(IResource[] resources) {
		super("wizardPage");
		this.resources = resources;
		settings = EcamsProviderPlugin.getPlugin().getDialogSettings();
		setPageComplete(false);
		setTitle("\ud30c\uc77c\uc120\ud0dd");
		setDescription("\ud30c\uc77c\uc744 \uc120\ud0dd\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		setupCallbacks();
		
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		
		resourceSelectionTree = new ResourceSelectionTree(container, SWT.NONE, resources, true);
		resourceSelectionTree.setLocation(0, 0);
		resourceSelectionTree.setBounds(5, 5, 598, 323);
		
		
		chkModify = new Button(container, SWT.CHECK);
		chkModify.setBounds(10, 255, 177, 16);
		chkModify.setText("\uc218\uc815\uc0ac\ud56d \uc788\ub294 \ud30c\uc77c\ub9cc \uccb4\ud06c\uc778");
		chkModify.addMouseListener(chkModifyButtonCheck_Listener);
		chkModify.setVisible(false);
		

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
	
	public void setupCallbacks() {
		chkModifyButtonCheck_Listener = new MouseListener(){
			public void mouseDoubleClick(MouseEvent e) {}
			public void mouseDown(MouseEvent e) {}
			public void mouseUp(MouseEvent e) {
				
				IResource[] selectedResources2;
				
				if (chkModify.getSelection()){
					int resourceCnt = 0;
					resourceCnt = resources.length;
					ArrayList chkModifyData = new ArrayList();
					
					for( int i=0; i<resourceCnt; i++ ) {
						IEcamsStatus resourceChangeStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus( ((IResource[])resources)[i] );
						
						if( (resourceChangeStatus != null) && (resourceChangeStatus.isChanged() == true) ) { //
							chkModifyData.add(resources[i]);
						}
						resourceChangeStatus = null;
					}
					selectedResources2 = new IResource[chkModifyData.size()];
					chkModifyData.toArray(selectedResources2);
					
					selectedResources = selectedResources2;
					resourceSelectionTree.setResources( (IResource[]) selectedResources );
					resourceSelectionTree.getTreeViewer().refresh();
					
				} else{
					selectedResources = resources;
					resourceSelectionTree.setResources( (IResource[]) selectedResources );
					resourceSelectionTree.getTreeViewer().refresh();
				}
				
				selectedResources2 = null;
			}
			
		};
	}

	public Object[] getSelectedResources() {
		return selectedResources;
	}
}
