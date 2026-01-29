package com.azsoft.ecams.ui.wizard.page;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;
import com.swtdesigner.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;

public class CheckOutCnlRequestPage extends WizardPage {
	private Text text1;
	private ResourceSelectionTree resourceSelectionTree;
	private Button button;
	
	private Object[] selectedResources;
	
	private IProject project;
	//private Text text3;
	//private String csrno;
	

	/**
	 * Create the wizard.
	 */
	public CheckOutCnlRequestPage() {
		super("wizardPage");
		setTitle("\uccb4\ud06c\uc544\uc6c3\ucde8\uc18c");
		//setDescription("\uc2e0\uccad\uc11c\ub97c \uc791\uc131\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		
		//csrno=CheckOutCnlSelectResourcePage.getCSRNO();
		
		Label label1 = new Label(container, SWT.NONE);
		label1.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label1.setBounds(10, 10, 44, 17);
		label1.setText("\uc2dc\uc2a4\ud15c");
		
		text1 = new Text(container, SWT.BORDER);
		text1.setEditable(false);
		text1.setBounds(80, 7, 478, 23);
		
		/* \ubcc0\uacbd\uc544\uc774\ub514 \ucf64\ubcf4 \uc14b\ud305 */
		
		
		button = new Button(container, SWT.NONE);
		button.setEnabled(false);
		button.setVisible(false);
		button.setBounds(474, 33, 84, 22);
		button.setText("\ud504\ub85c\uc81d\ud2b8\uc815\ubcf4");
		
		
		resourceSelectionTree = new ResourceSelectionTree(container, SWT.NONE, null, false);
		resourceSelectionTree.setLocation(10, 33);
		resourceSelectionTree.setSize(552, 239);

		/*CSR번호
		Label label2 = new Label(container, SWT.NONE);
		label2.setText("CSR\ubc88\ud638");
		label2.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label2.setBounds(10, 39, 56, 12);
		
		text3 = new Text(container, SWT.BORDER);
		text3.setEditable(false);
		text3.setBounds(80, 35, 478, 20);
		text3.setText(csrno);
		*/
		
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
		if (project != null){
			try {
				text1.setText(project.getPersistentProperty(new QualifiedName("Properties","syscd")));				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/*
	public void setCSRNO(String acsrno) {
		this.csrno = acsrno;
		text3.setText(csrno.split(":")[0]+":"+csrno.split(":")[1]);
	}
	
	public String getText3() {
		return csrno;
	}
	*/
}
