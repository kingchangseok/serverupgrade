package com.azsoft.ecams.ui.view;



import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.EditorInputTransfer;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;

import com.azsoft.ecams.image.ImageUtil;
import com.azsoft.ecams.ui.dialog.AddFilesDlg;
import com.azsoft.ecams.ui.widgets.ResourceSelectionTree;

public class CheckOutView extends ViewPart{
	private ResourceSelectionTree viewer;
	public Action addFileAction, addItemAction, deleteItemAction, deleteAllAction;
	private IResource[] resources,resources2;
	private IResource[] SelectedResource;
	
    public CheckOutView() {
    }
	
    @Override
    public void createPartControl(Composite parent) {
    	viewer = new ResourceSelectionTree(parent, SWT.NONE, SelectedResource, false);
    	
		addDropSupport(viewer.getParent());
		
        createActions();
        initializeToolBar();
    }
    
    public ResourceSelectionTree getViewer(){
    	return viewer;
    }
    
    public void setResources(IResource[] resources){
    	this.SelectedResource = resources;
    }
    public void createActions() {
        // Create the actions
    	deleteAllAction = new Action("Delete ALL") {
    		public void run() {
    			if(viewer.getTreeViewer().getInput()!=null){
    				viewer.setDeleteViewer();
    				viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
    				//viewer.getTreeViewer().getTree().removeAll();
    				viewer.getTreeViewer().refresh();
    				resources = null;
    				resources2 = null;
    				SelectedResource = null;
			        addItemAction.setEnabled(false);
			        deleteAllAction.setEnabled(false);
    			}
    		}
 	    };
 	    
 	   deleteItemAction = new Action("Selected Delete") {
 		   public void run() {
 			   if(viewer.getTreeViewer().getInput()!=null){
 				   List<IResource> tmpList = new ArrayList<IResource>();
 				   IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
 				   for(int i=0;i<SelectedResource.length;i++){
 					   IResource tmpresource = (IResource)sel.getFirstElement();
 					   System.out.println("sel.getFirstElement(): "+sel.getFirstElement());
 					   if (tmpresource.getType() == IResource.PROJECT){
 						   System.out.println("SelectedResource[i].getProject(): "+SelectedResource[i].getProject());
 						   if(!sel.getFirstElement().equals(SelectedResource[i].getProject())){
 							   tmpList.add(SelectedResource[i]);
 						   }
 					   } else if (tmpresource.getType() == IResource.FOLDER){
 						   System.out.println("SelectedResource[i].getParent(): "+SelectedResource[i].getParent());
 						   if(!sel.getFirstElement().equals(SelectedResource[i].getParent())){
 							   tmpList.add(SelectedResource[i]);
 						   }
 					   } else if (tmpresource.getType() == IResource.FILE){
 						   System.out.println("SelectedResource[i]: "+SelectedResource[i]);
 						   if(!sel.getFirstElement().equals(SelectedResource[i])){
 							   tmpList.add(SelectedResource[i]);
 						   }
 					   }
 				   }
 				   viewer.setDeleteViewer();
 				   viewer.getTreeViewer().remove(viewer.getTreeViewer().getInput());
 				   viewer.getTreeViewer().refresh();
 				   if(tmpList.size()>0){
 					   SelectedResource = null;
 					   SelectedResource = (IResource[])tmpList.toArray(new IResource[tmpList.size()]);
 					   viewer.setResources(SelectedResource);
 					   viewer.redraw();
 				   }
 				   resources = null;
 				   resources2 = null;
 				   deleteAllAction.setEnabled(viewer.getTreeViewer().getTree().getItemCount()>0);
 				   deleteItemAction.setEnabled(false);
 			   }
 		   }
 	   };
 	   
 	   addItemAction = new Action("CheckOut") {
 		   public void run() {
 			   System.out.println("SelectedResource: "+SelectedResource);
 			   if(null == SelectedResource){
 				   MessageBox messageBox = new MessageBox(viewer.getShell(), SWT.OK);
 				   messageBox.setMessage("\uccb4\ud06c\uc544\uc6c3 \ud560 \uc218 \uc788\ub294 \uc790\uc6d0\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.");
 				   messageBox.open();
 				   return;
 			   }
 			   
 			   //EcamsProviderPlugin.getPlugin().getJobManager().addJob(new CheckOutJob("CheckOut Request",SelectedResource,"CheckOut Sayu",""));

 			   deleteAllAction.run();
   				
 			   MessageBox messageBox = new MessageBox(viewer.getShell(), SWT.OK);
 			   messageBox.setMessage("CheckOut\uc2e0\uccad\uc644\ub8cc");
 			   messageBox.open();
 		   }
 	   };
	   
 	  addFileAction = new Action("AddFiles") {
 		   public void run() {
 			  final AddFilesDlg addFileDlg = new AddFilesDlg(viewer.getShell(), SelectedResource);
			   addFileDlg.open();
			   
			   IResource[] tmpSelResources = null;
			   tmpSelResources = addFileDlg.getSelectedResources();
			   if(null != tmpSelResources){
				   SelectedResource = tmpSelResources;
				   viewer.setDeleteViewer();
				   viewer.setResources(SelectedResource);
				   viewer.redraw();
				   addItemAction.setEnabled(SelectedResource.length>0);
				   deleteAllAction.setEnabled(SelectedResource.length>0);
			   }
 		   }
 	   };
 	   
 	   viewer.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
 		   public void selectionChanged(SelectionChangedEvent event) {
 			   updateActionEnablement();
 		   }
 	   });
 	   
 	   MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
 	   menuMgr.setRemoveAllWhenShown(true);
 	   menuMgr.addMenuListener(new IMenuListener() {
 		   public void menuAboutToShow(IMenuManager manager) {
 			   Action action = new Action() {
 				   public void run() {
 					   super.run();
 					   // TODO do something
 					  addFileAction.run();
 				   }
 			   };
 			   action.setText("Add CheckOut files..");
 			   //action.setImageDescriptor(EcamsImages.getImageDescriptor("Add"));
 			   action.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("Add"));
 			   manager.add(action);
 		   }
 	   });
 	   menuMgr.addMenuListener(new IMenuListener() {
 		   public void menuAboutToShow(IMenuManager manager) {
 			   Action action = new Action() {
 				   public void run() {
 					   super.run();
 					   // TODO do something
 					   deleteItemAction.run();
 				   }
 			   };
 			   IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
 			   action.setEnabled(sel.size() > 0);
 			   action.setText("Remove Selected CheckOut List");
 			   //action.setImageDescriptor(EcamsImages.getImageDescriptor("SelDel"));
 			   action.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("SelDel"));
 			   manager.add(action);
 		   }
 	   });
 	   menuMgr.addMenuListener(new IMenuListener() {
 		   public void menuAboutToShow(IMenuManager manager) {
				Action action = new Action() {
					public void run() {
						super.run();
						// TODO do something
						deleteAllAction.run();
					}
				};
				action.setEnabled(viewer.getTreeViewer().getTree().getItemCount()>0);
				action.setText("Remove All CheckOut List");
				//action.setImageDescriptor(EcamsImages.getImageDescriptor("AllDel"));
				action.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("AllDel"));
				manager.add(action);
			}
		});
    	
		Menu menu = menuMgr.createContextMenu(viewer.getTreeViewer().getTree());
		viewer.getTreeViewer().getTree().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer.getTreeViewer());

	}

    private void updateActionEnablement() {
        IStructuredSelection sel = (IStructuredSelection)viewer.getTreeViewer().getSelection();
        deleteItemAction.setEnabled(sel.size() > 0);
    }
    
    private void initializeToolBar() {
    	//if(null == getViewSite()) return;
    	
        IToolBarManager toolbarManager=getViewSite().getActionBars().getToolBarManager();
        
        toolbarManager.add(addFileAction);
        toolbarManager.add(addItemAction);
        toolbarManager.add(deleteItemAction);
        toolbarManager.add(deleteAllAction);
        
        addFileAction.setToolTipText("Add CheckOut files..");
        //addFileAction.setImageDescriptor(EcamsImages.getImageDescriptor("Add"));
        addFileAction.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("Add"));
        addItemAction.setToolTipText("Run CheckOut");
        //deleteItemAction.setImageDescriptor(EcamsImages.getImageDescriptor("SelDel"));
        deleteItemAction.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("SelDel"));
        deleteItemAction.setToolTipText("Remove Selected CheckOut List");
        //deleteAllAction.setImageDescriptor(EcamsImages.getImageDescriptor("AllDel"));
        deleteAllAction.setImageDescriptor(ImageUtil.getImageRegistry().getDescriptor("AllDel"));
        deleteAllAction.setToolTipText("Remove All CheckOut List");
        
        addFileAction.setEnabled(true);
        addItemAction.setEnabled(false);
        deleteAllAction.setEnabled(false);
        deleteItemAction.setEnabled(false);
    }
    
    @Override
    public void setFocus() {
        // set the focus
    	viewer.setFocus();
    }
    
    public void addDropSupport(Control control) {
		int operations = DND.DROP_COPY | DND.DROP_DEFAULT;
		DropTarget target = new DropTarget(control, operations);
		
		final EditorInputTransfer editorInputTransfer = EditorInputTransfer.getInstance();
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] transferTypes = new Transfer[] {editorInputTransfer, textTransfer};
		target.setTransfer(transferTypes);

		target.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) 
			{
				if (event.detail == DND.DROP_DEFAULT) 
				{
					 if ((event.operations & DND.DROP_COPY) != 0)
					 	event.detail = DND.DROP_COPY;
					 else 
					 	event.detail = DND.DROP_NONE; 
				}
			}
			
			public void dragOver(DropTargetEvent event) {
			}
			
			public void dragOperationChanged(DropTargetEvent event) 
			{
				if (event.detail == DND.DROP_DEFAULT) 
				{
					 if ((event.operations & DND.DROP_COPY) != 0)
					 	event.detail = DND.DROP_COPY;
					 else 
					 	event.detail = DND.DROP_NONE; 
				}
			}
			
			public void dragLeave(DropTargetEvent event) {
			}
			
			public void dropAccept(DropTargetEvent event) {
			}
			
			public void drop(DropTargetEvent event) 
			{
				if (editorInputTransfer.isSupportedType(event.currentDataType)) 
				{
					EditorInputData[] EditorInputData = (EditorInputData[])event.data;
					for(int k=0; k<EditorInputData.length; k++){
						EditorInputData editorInput = ((EditorInputData[])event.data)[k];
						IPersistableElement inData = editorInput.input.getPersistable();
						List<IFile> fileList = new ArrayList<IFile>();
						List<IResource> tmplist = new ArrayList<IResource>();
						if (inData instanceof FileEditorInput){
							IFile file = ((FileEditorInput)inData).getFile();
							fileList.add(file);
							resources = null;
							resources = (IFile[]) fileList.toArray(new IFile[fileList.size()]);
							//System.out.println(file.getProject()+"="+file.getLocation()+"="+file.getName());
							resources2 = null;
							resources2 = viewer.getSelectedResources();
							//resources2 = (IResource[])tmplist.toArray(new IResource[tmplist.size()]);
							if(null != resources2){
								for(int i=0;i<resources2.length; i++){
									tmplist.add(resources2[i]);
									System.out.println("resources2[i] : "+resources2[i]);
								}
								boolean duplFlg = false;
								for(int i=0;i<resources.length; i++){
									for(int j=0; j<resources2.length; j++){
										//System.out.println(resources2[i].getLocation());
										//System.out.println(resources[j].getLocation());
										/*if(!resources2[j].getProject().equals(resources[i].getProject())){
											System.out.println("\ud504\ub85c\uc81d\ud2b8\ubd88\uc77c\uce58 : "+resources2[j].getProject()+" : "+resources[i].getProject());
											duplFlg = true;
											break;
										}else */if(resources2[j].getLocation().equals(resources[i].getLocation())){
											System.out.println("\uc911\ubcf5 : "+resources2[j].getLocation());
											duplFlg = true;
											break;
										}
									}
									if(!duplFlg){
										tmplist.add(resources[i]);
										System.out.println("resources[j] : "+resources[i]);
									}
								}
							}else{
								if(resources != null){
									for(int i=0;i<resources.length; i++){
										tmplist.add(resources[i]);
									}
								}
							}
							if(tmplist != null){
								SelectedResource = null;
								SelectedResource = (IResource[])tmplist.toArray(new IResource[tmplist.size()]);
								viewer.setDeleteViewer();
								viewer.setResources(SelectedResource);
								viewer.redraw();
							}
							tmplist = null;
							resources = null;
							resources2 = null;
						}
					}
					addItemAction.setEnabled(SelectedResource.length>0);
			        deleteAllAction.setEnabled(SelectedResource.length>0);
				}
			}
		});
		
    }

}