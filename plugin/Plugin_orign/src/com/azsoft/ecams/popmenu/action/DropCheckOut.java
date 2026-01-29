package com.azsoft.ecams.popmenu.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.ui.view.CheckOutView;


public class DropCheckOut implements IObjectActionDelegate {
	private Shell shell;
	private IResource[] selectResources;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		shell = targetPart.getSite().getShell();
	}
	
	public void run(IAction action) {
		ObjectPluginAction myAction;
		ISelection mySelection;
		if (action instanceof ObjectPluginAction){
			myAction = (ObjectPluginAction) action;
		}
		else{
			return;
		}
		
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveAllEditors(true);
		
		mySelection = myAction.getSelection();
		
		List selectedList = new ArrayList();
		
		if (mySelection instanceof IStructuredSelection){
			for (Iterator it =((IStructuredSelection) mySelection).iterator();it.hasNext();){
				Object select_obj = it.next();

				if (select_obj != null && select_obj instanceof IResource){
					if (((IResource)select_obj).getType()==IResource.FILE){
						if (((IResource)select_obj).getParent().getName().equals(".deco")){
							//|| ((IResource)select_obj).getParent().getName().equals(".settings")
							continue;
						}
						IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus((IResource)select_obj);
						if (resourceStatus == null){
							continue;
						}
						if (resourceStatus.isAuthority() && resourceStatus.isLocked() && resourceStatus.getFileStatus().split(":")[1].equals("0")){
							selectedList.add((IResource)select_obj);
						}
						// ||
						//((IResource)select_obj).getType()==IResource.FOLDER ||
						//((IResource)select_obj).getType()==IResource.PROJECT
					}
				}
			}
		}
		
		if (selectedList.size() < 1){
			return;
		}
		
		selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
		
		if(selectResources == null){
			return;
		}

		
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			page.showView("com.azsoft.ecams.ui.view.checkoutview",null,IWorkbenchPage.VIEW_VISIBLE);
			IViewReference viewReference = page.findViewReference("com.azsoft.ecams.ui.view.checkoutview");
			if (viewReference != null) {
				IViewPart view = viewReference.getView(true);
				CheckOutView showview = (CheckOutView) view;
				
				if(showview instanceof CheckOutView){
					IResource[] resources = showview.getViewer().getSelectedResources();
					List<IResource> tmplist = new ArrayList<IResource>();
					if(null != resources){
						for(int i=0;i<resources.length; i++){
							tmplist.add(resources[i]);
							System.out.println("resources2[i] : "+resources[i]);
						}
						boolean duplFlg = false;
						for(int i=0;i<selectResources.length; i++){
							for(int j=0; j<resources.length; j++){
								if(resources[j].getLocation().equals(selectResources[i].getLocation())){
									System.out.println("\uc911\ubcf5 : "+resources[j].getLocation());
									duplFlg = true;
									break;
								}
							}
							if(!duplFlg){
								tmplist.add(selectResources[i]);
								System.out.println("resources[j] : "+selectResources[i]);
							}
						}
					}else{
						for(int i=0;i<selectResources.length; i++){
							tmplist.add(selectResources[i]);
						}
					}
					if(tmplist != null){
						selectResources = null;
						selectResources = (IResource[])tmplist.toArray(new IResource[tmplist.size()]);
						showview.getViewer().setDeleteViewer();
						showview.getViewer().setResources(selectResources);
						showview.getViewer().redraw();
					}
					tmplist = null;
					resources = null;
					
					showview.setResources(selectResources);
					showview.addItemAction.setEnabled(selectResources.length>0);
					showview.deleteAllAction.setEnabled(selectResources.length>0);
				}
			}
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
