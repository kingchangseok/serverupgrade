package com.azsoft.ecams.popmenu.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ObjectPluginAction;
import org.eclipse.ui.internal.ViewPluginAction;

import com.azsoft.ecams.core.listeners.FileOpenCheckListener;
import com.azsoft.ecams.ui.dialog.DisConnectDlg;
import com.azsoft.ecams.ui.view.ResourceView;
import com.azsoft.ecams.ui.view.ServiceRequestView;
import com.azsoft.ecams.ui.view.SyncWithView;

public class SyncWith implements IViewActionDelegate {
	private Shell shell;
	
	@Override
	public void init(IViewPart view) {
		// TODO Auto-generated method stub
		shell = view.getSite().getShell();
	}
	
	public void run(IAction action) {
		ViewPluginAction myAction;
		ISelection mySelection;
		if (action instanceof ViewPluginAction){
			myAction = (ViewPluginAction) action;
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
					if (((IResource)select_obj).getType()==IResource.PROJECT){
						selectedList.add((IResource)select_obj);
						break;
					}
				}
				else if (select_obj != null && select_obj instanceof JavaProject){
					selectedList.add((IResource)((JavaProject)select_obj).getResource());
					break;
				}
				else if (select_obj != null && select_obj instanceof PackageFragmentRoot){
					selectedList.add((IResource)((PackageFragmentRoot)select_obj).getResource());
					break;
				}
				else if (select_obj != null && select_obj instanceof PackageFragment){
					selectedList.add((IResource)((PackageFragment)select_obj).getResource());
					break;
				}
				else if (select_obj != null && select_obj instanceof CompilationUnit){
					selectedList.add((IResource)((CompilationUnit)select_obj).getResource());
					break;
				}
			}
		}

		if(selectedList.size() == 1){
			IResource[] selectResources = null;
			
			selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
	
			if (selectResources == null || selectResources.length < 1){
				MessageBox messageBox = new MessageBox(shell, SWT.OK);
				messageBox.setMessage("\ud615\uc0c1\uad00\ub9ac\uc11c\ubc84\uc640 \ube44\uad50\ud560 \ud558\ub098\uc758 \ud504\ub85c\uc81d\ud2b8\ub97c \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.");
				messageBox.open();
				return;
			}
			if (!selectResources[0].getProject().isAccessible()){
				MessageBox messageBox = new MessageBox(shell, SWT.OK);
				messageBox.setMessage("\ub2eb\ud600\uc788\ub294 \ud504\ub85c\uc81d\ud2b8 \uc785\ub2c8\ub2e4.");
				messageBox.open();
				return;
			}
			
			
			IWorkbenchPage aPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (aPage != null){
				try {
					aPage.showView("com.azsoft.ecams.ui.view.syncwithview",null,IWorkbenchPage.VIEW_ACTIVATE);
					IViewReference viewReference = aPage.findViewReference("com.azsoft.ecams.ui.view.syncwithview");
					
					if (viewReference != null) {
						IViewPart view = viewReference.getView(true);
						SyncWithView showview = (SyncWithView) view;
						showview.setFocus();
					
						showview.setResourceView(selectResources[0].getProject());
						showview = null;
						view = null;
					}
					viewReference = null;
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				aPage = null;
			}
			selectResources = null;
		}else{
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("\ud615\uc0c1\uad00\ub9ac\uc11c\ubc84\uc640 \ube44\uad50\ud560 \ud558\ub098\uc758 \ud504\ub85c\uc81d\ud2b8\ub97c \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.");
			messageBox.open();
			return;
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}
}
