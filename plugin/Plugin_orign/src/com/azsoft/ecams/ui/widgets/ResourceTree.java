package com.azsoft.ecams.ui.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.ui.decorator.DecoratedLabelProvider;

public class ResourceTree extends Composite {
	private IDialogSettings settings;
	private IResource[] resources;
	private ArrayList resourceList;
	private TreeViewer treeViewer;
	private Tree tree;
	private LabelProvider labelProvider;
	private IContainer[] compressedFolders;
	private ArrayList compressedFolderList;
	private ResourceComparator comparator = new ResourceComparator();
	private ResourceSelectionContentProvider resourceSelectionContentProvider = new ResourceSelectionContentProvider();
	private IContainer[] rootFolders;
	private IContainer[] folders;
	private ArrayList folderList;
	private String mode;
	
	
	public ResourceTree(Composite parent, int style, IResource[] resources, String mode) {
		// TODO Auto-generated constructor stub
		super(parent, style);
		
		this.mode = mode;
		this.resources = resources;
		this.settings = EcamsProviderPlugin.getPlugin().getDialogSettings();
		

		if(resources!=null){
			Arrays.sort(resources, comparator);
			resourceList = new ArrayList();
			for (int i = 0; i < resources.length; i++)
			{
				IResource resource = resources[i];
				resourceList.add(resource);
			}
		}
		
		createControls();
		
		if (resources != null && treeViewer != null){
			treeViewer.setInput(this);
			treeViewer.expandAll();
		}
	}
	
	
	public void setResources(IResource[] resources) {
		rootFolders = null;
		folders = null;
		folderList = null;
		compressedFolderList = null;
		compressedFolders = null;
		resourceList = null;
		this.resources = null;
		
		this.resources = resources;
		if(null != resources){
			Arrays.sort(resources, comparator);
		}
		if (treeViewer != null){
			treeViewer.setInput(this);
			treeViewer.expandAll();
			//for(int i=0; i<resources.length; i++){
			//	System.out.println(resources[i]);
			//	treeViewer.expandToLevel(resources[i], AbstractTreeViewer.ALL_LEVELS);
			//}
		}
	}
	
	private void createControls(){
		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(GridData.FILL_BOTH));

		ViewForm viewerPane = new ViewForm(this, SWT.BORDER | SWT.FLAT);
		viewerPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		treeViewer = new TreeViewer(viewerPane, SWT.MULTI);
		tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewerPane.setContent(tree);
		
		//labelProvider = new ResourceSelectionLabelProvider();
		//treeViewer.setLabelProvider(labelProvider);
		//treeViewer.setLabelProvider(new DecoratingLabelProvider(
		//		new WorkbenchLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		
		treeViewer.setLabelProvider(new DecoratedLabelProvider(new WorkbenchLabelProvider(), "FULL"));

		treeViewer.setContentProvider(resourceSelectionContentProvider);
		treeViewer.setUseHashlookup(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 125;
		treeViewer.getControl().setLayoutData(gd);
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public void setDeleteViewer() {
		resources = null;
		compressedFolders = null;
	}
	
	public IResource[] getSelectedResources() {
		return resources;
	}
	
	private IContainer[] getRootFolders() {
		//if (rootFolders == null) getFolders();
		getFolders();
		return rootFolders;
	}
	
	private IContainer[] getCompressedFolders() {
		if (compressedFolders == null) {
			compressedFolderList = new ArrayList();
			for (int i = 0; i < resources.length; i++) {
				if (resources[i] instanceof IContainer && !compressedFolderList.contains(resources[i]))
					compressedFolderList.add(resources[i]);
				if (!(resources[i] instanceof IContainer)) {
					IContainer parent = resources[i].getParent();
					if (parent != null && !(parent instanceof IWorkspaceRoot) && !compressedFolderList.contains(parent)) {
						compressedFolderList.add(parent);
					}
				}
			}
			compressedFolders = new IContainer[compressedFolderList.size()];
			compressedFolderList.toArray(compressedFolders);
			Arrays.sort(compressedFolders, comparator);
		}
		return compressedFolders;
	}
	
	private IResource[] getChildResources(IContainer parent) {
		ArrayList children = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			if (!(resources[i] instanceof IContainer)) {
				IContainer parentFolder = resources[i].getParent();
				if (parentFolder != null && parentFolder.equals(parent) && !children.contains(parentFolder))
					children.add(resources[i]);
			}
		}
		IResource[] childArray = new IResource[children.size()];
		children.toArray(childArray);
		return childArray;
	}
	
	private IResource[] getFolderChildren(IContainer parent) {
		ArrayList children = new ArrayList();
		folders = getFolders();
		for (int i =0; i < folders.length; i++) {
			if ( (folders[i].getParent()) != null && folders[i].getParent().equals(parent)) children.add(folders[i]);

		}
		for (int i = 0; i < resources.length; i++) {
			if (!(resources[i] instanceof IContainer) && (resources[i].getParent() != null) && resources[i].getParent().equals(parent) )
				children.add(resources[i]);
		}
		IResource[] childArray = new IResource[children.size()];
		children.toArray(childArray);
		return childArray;
	}
	
	private IContainer[] getFolders() {
		List rootList = new ArrayList();
		if (folders == null) {
			folderList = new ArrayList();
			for (int i = 0; i < resources.length; i++) {
				if (resources[i] instanceof IContainer) folderList.add(resources[i]);
				IResource parent = resources[i];
				while (parent != null && !(parent instanceof IWorkspaceRoot)) {
					if (!(parent.getParent() instanceof IWorkspaceRoot) && folderList.contains(parent.getParent())) break;
					if (parent.getParent() == null || parent.getParent() instanceof IWorkspaceRoot) {
						rootList.add(parent);
					}
					parent = parent.getParent();
					folderList.add(parent);
				}
			}
			folders = new IContainer[folderList.size()];
			folderList.toArray(folders);
			Arrays.sort(folders, comparator);
			rootFolders = new IContainer[rootList.size()];
			rootList.toArray(rootFolders);
			Arrays.sort(rootFolders, comparator);
		}
		return folders;
	}
	
	private class ResourceSelectionContentProvider extends WorkbenchContentProvider {
		public Object getParent(Object element) {
			return ((IResource)element).getParent();
		}
		public boolean hasChildren(Object element) {
			if (element instanceof IContainer) return true; //mode != MODE_FLAT && 
			else return false;
		}
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ResourceTree) {
				if (mode.equals("F")) return getCompressedFolders();
				else return getRootFolders();
			}
			if (parentElement instanceof IContainer) {
				if (mode.equals("F")) {
					return getChildResources((IContainer)parentElement);
				} else {
					return getFolderChildren((IContainer)parentElement);
				}
			}
			return new Object[0];
		}
	}
		
	private class ResourceSelectionLabelProvider extends LabelProvider {
		private WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();
		
		public Image getImage(Object element) {
			return workbenchLabelProvider.getImage(element);
		}

		private IResource getResource(Object object) {
			if (object instanceof IResource) {
				return (IResource) object;
			}
			if (object instanceof IAdaptable) {
				return (IResource) ((IAdaptable) object).getAdapter(
					IResource.class);
			}
			return null;
		}
		
		public String getText(Object element) {
			String text = ((IResource)element).getName();
			text = workbenchLabelProvider.getText(element);
			/*
			IResource resource = getResource(element);
			if (resource == null) return text;
			if (!resource.getProject().isAccessible()) {
				treeViewer.collapseAll();
				return text;
			}
			if (resource.getType() == IResource.ROOT) return text;
			
			if (element instanceof IContainer) {
				IContainer container = (IContainer)element;
				//text = container.getFullPath().makeRelative().toString();
				if(container.getType() == IResource.PROJECT){
					if (EcamsProviderPlugin.getPlugin().isManagedByEcams((IProject)resource)){
						try {
							text = text + "["+ resource.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]+"]";		
						} catch (CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}else{
				if(resource.getType() == IResource.FILE){
					try{
						if (EcamsProviderPlugin.getPlugin().isManagedByEcams(resource.getProject())){
							IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(resource);
							if (resourceStatus != null){
								String[] filestatus = resourceStatus.getFileStatus().split(":");
								
								if (filestatus == null) return text;
								if (filestatus.length < 2) return text;
								if (filestatus[1].equals("U")) return text;
		
								if (resourceStatus.isAuthority()){
									if (filestatus[1].equals("0")){
										//text = text + "[\uc6b4\uc601\uc911] "+resourceStatus.getLastUser().split(":")[0]+" ver_"+resourceStatus.getTstVer()+"("+resourceStatus.getLastVer()+")";
										text = text + "[\uc6b4\uc601\uc911] "+resourceStatus.getLastUser().split(":")[0]+" "+resourceStatus.getLastVer();
									}else{
										if (!(resourceStatus.getEditor().split(":"))[1].equals(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null))){
											//text = text + "["+resourceStatus.getEditor().split(":")[0]+"]\ub2d8  ["+filestatus[0]+"]"+" ver_"+resourceStatus.getTstVer()+"("+resourceStatus.getLastVer()+")";
											text = text + "["+resourceStatus.getEditor().split(":")[0]+"]\ub2d8  ["+filestatus[0]+"]"+" "+resourceStatus.getLastVer();
										}else{
											//text = text + "["+filestatus[0]+"]"+" ver_"+resourceStatus.getTstVer()+"("+resourceStatus.getLastVer()+")";
											text = text + "["+filestatus[0]+"]"+" ver_"+resourceStatus.getLastVer();
										}
									}
								}else{
									if (filestatus[1].equals("0")){
										//text = text + "[\uc6b4\uc601\uc911] "+resourceStatus.getLastUser().split(":")[0]+" ver_"+resourceStatus.getTstVer()+"("+resourceStatus.getLastVer()+")";
										text = text + "[\uc6b4\uc601\uc911] "+resourceStatus.getLastUser().split(":")[0]+" "+resourceStatus.getLastVer();
									}else{
										text = text + "["+resourceStatus.getEditor().split(":")[0]+"]\ub2d8 ["+filestatus[0]+"]"+" "+resourceStatus.getLastVer();
									}
								}
							}
						}
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
			}
			*/
			return text;
		}

	};
	
	private class ResourceComparator implements Comparator {
		public int compare(Object obj0, Object obj1) {
			IResource resource0 = (IResource)obj0;
			IResource resource1 = (IResource)obj1;
			return resource0.getFullPath().toOSString().compareTo(resource1.getFullPath().toOSString());
		}
	}	
}
