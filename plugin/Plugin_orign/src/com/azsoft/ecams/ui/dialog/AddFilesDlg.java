package com.azsoft.ecams.ui.dialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.ui.widgets.ResourceTree;

public class AddFilesDlg extends Dialog {
	private Shell parentShell;
	private IResource[] resources;
	private IResource[] retResources = null;
	private IResource[] selResources = null;
	private Button okButton;
	protected DisposeListener listener;
	private ResourceTree treeViewer;
	
	public AddFilesDlg(Shell parentShell, IResource[] resources) {
		// TODO Auto-generated constructor stub
		super(parentShell);
		this.parentShell = parentShell;
		this.resources = resources;
	}	
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("CheckOut Files Selection");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = super.createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,false);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID,IDialogConstants.CANCEL_LABEL, true);
		
		okButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				IStructuredSelection sel = (IStructuredSelection)treeViewer.getTreeViewer().getSelection();
				List findResourceList = new ArrayList();
				Set addResources = new HashSet();
				int i=0;
				for(i=0;i<sel.size();i++){
					Object[] selObj = sel.toArray();
					IResource tmpresource = (IResource)selObj[i];
					addResources.add(tmpresource);
				}
				if(null != resources){
					for(i=0;i<resources.length;i++){
						addResources.add(resources[i]);
					}
				}
				findResourceList = new ArrayList(addResources);
				selResources = (IResource[])findResourceList.toArray(new IResource[findResourceList.size()]);
			}
		});
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		
		Label ip_lb = new Label(container, SWT.NONE);
		ip_lb.setBounds(10, 10, 204, 16);
		ip_lb.setText("&Choose the CheckOut files to add:");
		

		treeViewer = new ResourceTree(container, SWT.NONE, null, "T");
		treeViewer.setBounds(10, 30, 422, 235);
		
		treeViewer.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateActionEnablement();
			}
		});
		treeViewer.getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				// TODO Auto-generated method stub
				IStructuredSelection sel = (IStructuredSelection)treeViewer.getTreeViewer().getSelection();
				if((IResource)sel.getFirstElement() instanceof IContainer){
					treeViewer.getTreeViewer().expandToLevel(sel.getFirstElement(), AbstractTreeViewer.ALL_LEVELS);
				}else{
					if(((IResource)sel.getFirstElement()).getType() == IResource.FILE){
						List findResourceList = new ArrayList();
						Set addResources = new HashSet();
						addResources.add((IResource)sel.getFirstElement());
						if(null != resources){
							for(int i=0;i<resources.length;i++){
								addResources.add(resources[i]);
							}
						}
						findResourceList = new ArrayList(addResources);
						selResources = (IResource[])findResourceList.toArray(new IResource[findResourceList.size()]);
						close();
					}
				}
			}
		});
		
		setCallBack();
		
		return container;
	}
	
	private void setCallBack(){
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		
		List projectList = new ArrayList();
		for (int i=0; i<projects.length; i++) {
			projectList.add(projects[i].getProject());
		}
		if(null != projectList){
			IResource[] allresources = (IResource[])(projectList.toArray(new IResource[(projectList).size()]));
			
			retResources = getSelectedResources(allresources);
			
			if(null != retResources){
				treeViewer.setDeleteViewer();
				treeViewer.setResources(retResources);
				treeViewer.redraw();
			}
			
			allresources = null;
		}
	}
	
	private IResource[] getSelectedResources(IResource[] prjresources){
		List findResourceList=null;
		try{
			Set addResources = new HashSet();
			for(int i=0;i<prjresources.length;i++){
				if (prjresources[i].getType() == IResource.FILE){
					if (prjresources[i].getParent().getName().equals(".deco") 
							|| prjresources[i].getParent().getName().equals(".settings")){
						continue;
					}
					if(null != resources){
						for(int j=0; j<resources.length; j++){
							System.out.println(resources[j]);
							System.out.println((IResource)prjresources[i]);
		    				if((resources[j].toString()).equals(((IResource)prjresources[i]).toString())){
								continue;
		    				}
							addResources.add((IResource)prjresources[i]);
						}
					}else{
						addResources.add((IResource)prjresources[i]);
					}
				} else if (prjresources[i].getType()==IResource.FOLDER || prjresources[i].getType()==IResource.PROJECT){
					if(prjresources[i].getType()==IResource.PROJECT){
						IProject project = (IProject)prjresources[i];
						if(!EcamsRepositoryProvider.isManagedByEcams(project)){
							//형상관리와 연결된 프로젝트가 아니면 파일추가에서 제외
							continue;
						}
					}
					IResource[] childFindResources = getSelectedResources(((IContainer) prjresources[i]).members());
					if (childFindResources != null){
						for (int j=0;j<childFindResources.length;j++){
							addResources.add(childFindResources[j]);
						}
					}
				}
			}
			findResourceList = new ArrayList(addResources);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
		}
		if(findResourceList != null){
			return (IResource[])findResourceList.toArray(new IResource[findResourceList.size()]);
		}else {
			return null;
		}
	}
	
	private void updateActionEnablement(){
		IStructuredSelection sel = (IStructuredSelection)treeViewer.getTreeViewer().getSelection();
		int cnt = 0;
		for(int i=0;i<sel.size();i++){
			Object[] selObj = sel.toArray();
			IResource tmpresource = (IResource)selObj[i];
			System.out.println(tmpresource);
			if(tmpresource.getType()!=IResource.FILE){
				cnt++;
				break;
			}
			okButton.setEnabled(cnt==0);
		}
	}
	
	public IResource[] getSelectedResources(){
		return selResources;
	}
}
