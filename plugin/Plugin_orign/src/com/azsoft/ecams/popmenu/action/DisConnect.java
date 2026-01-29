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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ObjectPluginAction;
import org.eclipse.ui.internal.ViewPluginAction;

import com.azsoft.ecams.ui.dialog.DisConnectDlg;

public class DisConnect implements IViewActionDelegate {
	private Shell shell;
	private DisConnectDlg disconnectDlg;
	private IResource[] selectResources;
	
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
					if (((IResource)select_obj).getType()==IResource.FILE ||
						((IResource)select_obj).getType()==IResource.FOLDER ||
						((IResource)select_obj).getType()==IResource.PROJECT){
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
		
		if (selectedList.size() < 1){
			return;
		}
		
		selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);

		if (selectResources == null){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("\uc5f0\uacb0\uc744 \ub04a\uc73c\uc2e4 \ud504\ub85c\uc81d\ud2b8\ub97c \uc120\ud0dd\ud574\uc8fc\uc138\uc694");
			messageBox.open();
			return;
		}
		if (!selectResources[0].getProject().isAccessible()){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("\ub2eb\ud600\uc788\ub294 \ud504\ub85c\uc81d\ud2b8 \uc785\ub2c8\ub2e4.");
			messageBox.open();
			return;
		}
		
		if (selectResources.length < 1){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("\uc5f0\uacb0\uc744 \ub04a\uc73c\uc2e4 \ud504\ub85c\uc81d\ud2b8\ub97c \uc120\ud0dd\ud574\uc8fc\uc138\uc694");
			messageBox.open();
			return;
		}
		
		disconnectDlg = new DisConnectDlg(shell,selectResources);
		disconnectDlg.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}
}
