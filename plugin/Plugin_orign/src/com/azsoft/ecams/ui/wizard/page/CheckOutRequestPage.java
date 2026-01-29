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


import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;
import com.swtdesigner.SWTResourceManager;
import org.eclipse.swt.widgets.Text;

public class CheckOutRequestPage extends WizardPage {
	private Text text1;
	private StyledText text2;
	//private Text text3;
	private ResourceSelectionTree resourceSelectionTree;
	
	private IProject project;

	//private String csrno;

	/**
	 * Create the wizard.
	 */
	public CheckOutRequestPage() {
		super("wizardPage");
		setTitle("\uccb4\ud06c\uc544\uc6c3 \uc815\ubcf4\uc785\ub825");
		setDescription("\uc2e0\uccad\uc11c\ub97c \uc791\uc131\ud558\uc5ec \uc8fc\uc2ed\uc2dc\uc694.");
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
		label1.setBounds(10, 10, 44, 12);
		label1.setText("\uc2dc\uc2a4\ud15c");
		
		text1 = new Text(container, SWT.BORDER);
		text1.setEditable(false);
		text1.setBounds(100, 6, 465, 23);
		
		/*Label label2 = new Label(container, SWT.NONE);
		label2.setText("SPMS\ubc88\ud638");
		label2.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label2.setBounds(10, 39, 76, 12);
		text3 = new Text(container, SWT.BORDER);
		text3.setEditable(false);
		text3.setBounds(100, 35, 465, 20);*/
		
		resourceSelectionTree = new ResourceSelectionTree(container, SWT.NONE, null, false);
		resourceSelectionTree.setLocation(10, 97);
		resourceSelectionTree.setSize(562, 191);
		
		text2 = new StyledText(container, SWT.BORDER);
		text2.setBounds(100, 41, 465, 50);
		
		Label label4 = new Label(container, SWT.NONE);
		label4.setFont(SWTResourceManager.getFont("\uad74\ub9bc", 10, SWT.BOLD));
		label4.setBounds(10, 42, 61, 12);
		label4.setText("\uc2e0\uccad\uc0ac\uc720");
	}
	
	
	public ResourceSelectionTree getResourceSelectionTree() {
		return resourceSelectionTree;
	}
/*
	public void setCSRNO(String CSRNO){
		this.csrno = CSRNO;
		if(csrno==null || csrno.equals("")){
			text3.setText("");
		}else{
			text3.setText(csrno);
		}
	}
*/	
	public void setSelectedResources(Object[] selectedResources) {
		resourceSelectionTree.setResources((IResource[]) selectedResources);
		
		project = null;
		/*for (int i=0;i<((IResource[])selectedResources).length;i++){
			project = ((IResource[])selectedResources)[i].getProject();
			break;
		}*/

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
/*	
	public Text getText3() {
		return text3;
	}
*/		
}
