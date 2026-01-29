package com.azsoft.ecams.ui.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;

public class ResourceSelectionTree extends Composite {
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
	private boolean checkbox;

	public ResourceSelectionTree(Composite parent, int style, IResource[] resources,Boolean checkbox) {
		super(parent, style);
		
		this.resources = resources;
		this.settings = EcamsProviderPlugin.getPlugin().getDialogSettings();
		this.checkbox = checkbox;
		
		if(resources!=null)
		{
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
			if (checkbox) {
				((CheckboxTreeViewer)treeViewer).setAllChecked(true);
			}			
			
		}
		// TODO Auto-generated constructor stub
	}
	
	
	public void setResources(IResource[] resources) {
		this.resources = resources;
		if(null != resources){
			Arrays.sort(resources, comparator);
		}
		if (treeViewer != null){
			treeViewer.setInput(this);
			treeViewer.expandAll();
			if (checkbox) {
				((CheckboxTreeViewer)treeViewer).setAllChecked(true);
			}
		}
	}

	
	
	private void createControls(){
		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(GridData.FILL_BOTH));

		ViewForm viewerPane = new ViewForm(this, SWT.BORDER | SWT.FLAT);
		viewerPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		if (checkbox) {
			treeViewer = new CheckboxTreeViewer(viewerPane, SWT.MULTI); 
			  
			// Override the spacebar behavior to toggle checked state for all selected items.
			treeViewer.getControl().addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent event) {
					if (event.keyCode == 32) {
						Tree tree = (Tree)treeViewer.getControl();
						TreeItem[] items = tree.getSelection();
						for (int i = 0; i < items.length; i++) {
							if (i > 0) items[i].setChecked(!items[i].getChecked());
						}
					}
				}
			});
		}
		else{
			treeViewer = new TreeViewer(viewerPane, SWT.MULTI);
		}
		
		tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewerPane.setContent(tree);
		
		labelProvider = new ResourceSelectionLabelProvider();
		treeViewer.setLabelProvider(labelProvider);
		
		treeViewer.setContentProvider(resourceSelectionContentProvider);
		treeViewer.setUseHashlookup(true);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 125;
		treeViewer.getControl().setLayoutData(gd);
		
		if (checkbox) {	
			((CheckboxTreeViewer) treeViewer).addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					handleCheckStateChange(event);
				}
			});		
		}
	}
	
	private void handleCheckStateChange(CheckStateChangedEvent event) {
   		((CheckboxTreeViewer)treeViewer).setGrayed(event.getElement(), false);
		((CheckboxTreeViewer)treeViewer).setSubtreeChecked(event.getElement(), event.getChecked());	
	}
	
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public void setDeleteViewer() {
		resources = null;
		compressedFolders = null;
		//resourceList = new ArrayList();
	}

	public IResource[] getSelectedResources() {
		if (!checkbox){
			return resources;
		}
		ArrayList selected = new ArrayList();
		Object[] checkedResources = ((CheckboxTreeViewer)treeViewer).getCheckedElements();
		for (int i = 0; i < checkedResources.length; i++) {
			if (resourceList.contains(checkedResources[i]))
			selected.add(checkedResources[i]);
		}
		IResource[] selectedResources = new IResource[selected.size()];
		selected.toArray(selectedResources);
		return selectedResources;
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
				if (parentFolder != null && parentFolder.equals(parent) && !children.contains(parentFolder)){
					children.add(resources[i]);
				}
			}
		}
		IResource[] childArray = new IResource[children.size()];
		children.toArray(childArray);
		return childArray;
	}	
	
	private class ResourceSelectionContentProvider extends WorkbenchContentProvider {
		public Object getParent(Object element) {
			return ((IResource)element).getParent();
		}
		public boolean hasChildren(Object element) {
			if (element instanceof IContainer){
				return true;
			}
			else{
				return false;
			}
		}
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ResourceSelectionTree) {
				return getCompressedFolders();
			}
			if (parentElement instanceof IContainer) {
				return getChildResources((IContainer)parentElement);
			}
			return new Object[0];
		}
	}

	private class ResourceSelectionLabelProvider extends LabelProvider {
		private WorkbenchLabelProvider workbenchLabelProvider = new WorkbenchLabelProvider();
	
		protected ILabelProvider getDelegateLabelProvider() {
			return workbenchLabelProvider;
		}
		
		protected boolean isDecorationEnabled() {
			return true;
		}

		public Image getImage(Object element) {
			return workbenchLabelProvider.getImage(element);
		}

		public String getText(Object element) {
			String text = null;
			if (element instanceof IContainer) {
				IContainer container = (IContainer)element;
				text = container.getFullPath().makeRelative().toString();
			}
			else{
				IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus((IResource)element);
				if(resourceStatus == null){
					text = ((IResource)element).getName() + " [\uc2e0\uaddc]"; //신규
				}else{
					if(resourceStatus == null){
						text = ((IResource)element).getName();
					}else{
						if(resourceStatus.getFileStatus().split(":")[1].equals("5")){
							if (resourceStatus.isChanged()){
								text = ((IResource)element).getName() + " [\uc218\uc815]"; //수정
							}else{
								text = ((IResource)element).getName() + " [\uc218\uc815\uc0ac\ud56d\uc5c6\uc74c]"; //수정사항없음
							}
						}else if(resourceStatus.getFileStatus().split(":")[1].equals("9")){
							text = ((IResource)element).getName() + " [\ud3d0\uae30]"; //폐기
						}else if(resourceStatus.getFileStatus().split(":")[1].equals("4")){
							text = ((IResource)element).getName() + " [\uccb4\ud06c\uc544\uc6c3\uc911]"; //체크아웃중
						}else{
							text = ((IResource)element).getName();
						}
					}
				}
			}
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
