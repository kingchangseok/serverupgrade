package com.azsoft.ecams.ui.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.image.ImageUtil;
import com.azsoft.ecams.proto.ProtoEcams.FileData;
import com.azsoft.ecams.ui.view.SyncWithView;

public class SyncWithResourceTree extends Composite {
	private IDialogSettings settings;
	private IResource[] resources;
	private ArrayList resourceList;
	private TreeViewer treeViewer;
	private Tree tree;
	private IContainer[] compressedFolders;
	private ArrayList compressedFolderList;
	private ResourceComparator comparator = new ResourceComparator();
	private ResourceSelectionContentProvider resourceSelectionContentProvider = new ResourceSelectionContentProvider();
	private IContainer[] rootFolders;
	private IContainer[] folders;
	private ArrayList folderList;
	private String mode;
	
	private LabelProvider labelProvider;
	private List addLists = new ArrayList();
	
	
	public SyncWithResourceTree(Composite parent, int style, IResource[] resources, String mode) {
		// TODO Auto-generated constructor stub
		super(parent, style);
		
		this.mode = mode;
		this.resources = resources;
		this.settings = EcamsProviderPlugin.getPlugin().getDialogSettings();
		

		if(null != resources){
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
			//treeViewer.expandAll();
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
			//treeViewer.expandAll();
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
		
		//treeViewer.setLabelProvider(new SyncWithDecoratedLabelProvider(new WorkbenchLabelProvider()));
		labelProvider = new ResourceSelectionLabelProvider();
		treeViewer.setLabelProvider(labelProvider);
		
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
	
	public void setAddLists(List list){
		this.addLists = list;
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
			//Arrays.sort(compressedFolders, comparator);
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
			//Arrays.sort(folders, comparator);
			rootFolders = new IContainer[rootList.size()];
			rootList.toArray(rootFolders);
			//Arrays.sort(rootFolders, comparator);
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
			if (parentElement instanceof SyncWithResourceTree) {
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

			ResourceManager manager = ((DecoratorManager)EcamsProviderPlugin.getDefault().getWorkbench().getDecoratorManager()).getResourceManager();
			if(manager == null || !(element instanceof IFile)){
				//return EcamsProviderPlugin.getDefault().getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
				return workbenchLabelProvider.getImage(element);
			}
			IResource resource = getResource(element);
			
			if (null == resource){
				return workbenchLabelProvider.getImage(element);
			}
			if(null == resource.getProject()){
				return workbenchLabelProvider.getImage(element);
			}
			if (!resource.getProject().isAccessible()) {
				return workbenchLabelProvider.getImage(element);
			}
			if (resource.getType() == IResource.ROOT){
				return workbenchLabelProvider.getImage(element);
			}
			
			if (resource.getType() == IResource.FILE){
				try{
					if (!EcamsProviderPlugin.getPlugin().isManagedByEcams(resource.getProject())){
						return workbenchLabelProvider.getImage(element);
					}
					/*
					 * gbn
					 * C (로컬수정사항있음 I)
					 * L (로컬신규 I)
					 *
					 * V (수정사항없이 버전변경됨 O)
					 * S (서버신규 O)
					 * D (서버폐기 O)
					 * X (체크아웃없이 수정함 O)
					 */

					DecorationOverlayIcon icon = null;
					FileData filedata = null;
					for(int listCnt=0; listCnt<addLists.size(); listCnt++){
						filedata = (FileData) addLists.get(listCnt);
						
						String filepath = resource.getProject().getLocation()+filedata.getPathinfo().getRelativitePath();
	        			
	        			while(filepath.indexOf("/") >=0){
	        				filepath = filepath.replace("/","\\");
	        			}
	        			
	        			while(filepath.indexOf("\\\\") >=0){
	        				filepath = filepath.replace("\\\\", "\\");
	        			}
	
	        			while(filepath.indexOf("\\") >=0){
	        				filepath = filepath.replace("\\", "/");
	        			}	
	        			
	        			String filename = filepath+"/"+filedata.getFilename();
	        			String resourcePath = resource.getLocation().toString();
	        			if(filename.equals(resourcePath)){
	        				if("C".equals(filedata.getStatus())){
								//IN (→)
								icon = new DecorationOverlayIcon((Image)workbenchLabelProvider.getImage(element),
		    							ImageUtil.getImageRegistry().getDescriptor("In"), IDecoration.TOP_RIGHT);
								break;
	        				}else if("L".equals(filedata.getStatus())){
								//IN+ (→+)
								icon = new DecorationOverlayIcon((Image)workbenchLabelProvider.getImage(element),
		    							ImageUtil.getImageRegistry().getDescriptor("Inplus"), IDecoration.TOP_RIGHT);
								break;
							}else if("V".equals(filedata.getStatus())){
								//OUT (←) 
								icon = new DecorationOverlayIcon((Image)workbenchLabelProvider.getImage(element),
		    							ImageUtil.getImageRegistry().getDescriptor("Out"), IDecoration.TOP_RIGHT);
								break;
							}else if("S".equals(filedata.getStatus())){
								//OUT + (+←)
								icon = new DecorationOverlayIcon((Image)workbenchLabelProvider.getImage(element),
		    							ImageUtil.getImageRegistry().getDescriptor("Outplus"), IDecoration.TOP_RIGHT);
								break;
							}else if("D".equals(filedata.getStatus())){
								//OUT - (-←)
								icon = new DecorationOverlayIcon((Image)workbenchLabelProvider.getImage(element),
		    							ImageUtil.getImageRegistry().getDescriptor("Outminus"), IDecoration.TOP_RIGHT);
								break;
							}else if("X".equals(filedata.getStatus())){
								//CONFLICTS (↔)
								icon = new DecorationOverlayIcon((Image)workbenchLabelProvider.getImage(element),
		    							ImageUtil.getImageRegistry().getDescriptor("Different"), IDecoration.TOP_RIGHT);
								break;
							}else{
								return workbenchLabelProvider.getImage(element);
							}
	        			}
	        			filedata = null;
					}
        			filedata = null;
					
					if(icon == null){
						return workbenchLabelProvider.getImage(element);
					}else{
						return manager.createImage(icon);
					}
				}catch (Exception e){
					e.printStackTrace();
					return workbenchLabelProvider.getImage(element);
				}
			}else{
				return workbenchLabelProvider.getImage(element);
			}
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
			//text = workbenchLabelProvider.getText(element);
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
