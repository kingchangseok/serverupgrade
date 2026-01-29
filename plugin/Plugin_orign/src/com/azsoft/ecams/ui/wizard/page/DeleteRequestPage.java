package com.azsoft.ecams.ui.wizard.page;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;
import com.swtdesigner.SWTResourceManager;

public class DeleteRequestPage extends WizardPage {
	private Text text1;
	private ResourceSelectionTree resourceSelectionTree;
	private StyledText text2;
	
	private Object[] selectedResources;
	
	private IProject project = null;
	

	/**
	 * Create the wizard.
	 */
	public DeleteRequestPage() {
		super("wizardPage");
		setTitle("\uc0ad\uc81c \ub300\uc0c1 \ud30c\uc77c \ud655\uc778");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		
		Label label1 = new Label(container, SWT.NONE);
		label1.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label1.setBounds(10, 13, 44, 12);
		label1.setText("\uc2dc\uc2a4\ud15c");
		
		text1 = new Text(container, SWT.BORDER);
		text1.setEditable(false);
		text1.setBounds(80, 7, 478, 23);
		
		resourceSelectionTree = new ResourceSelectionTree(container, SWT.NONE, null, false);
		resourceSelectionTree.setLocation(0, 36);
		resourceSelectionTree.setSize(572, 246);
	}
	
	public ResourceSelectionTree getResourceSelectionTree() {
		return resourceSelectionTree;
	}
	
	public void setSelectedResources(Object[] selectedResources) {
		this.selectedResources = selectedResources;
		resourceSelectionTree.setResources((IResource[]) selectedResources);
		
		project = ((IResource[])selectedResources)[0].getProject();
		if (project != null){
			try {
				text1.setText(project.getPersistentProperty(new QualifiedName("Properties","syscd")));				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public StyledText getText2() {
		return text2;
	}	
	
}
