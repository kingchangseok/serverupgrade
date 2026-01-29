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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.ObjectPluginAction;
import org.eclipse.ui.internal.ViewPluginAction;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.SyncJob;
import com.azsoft.ecams.ui.wizard.page.SyncSelectResourcePage;

public class SyncSelectedResource implements IViewActionDelegate{
	IResource[] selectResources;
	private Shell shell;
	private List selectedList = new ArrayList();
	private int i=0;
	
	@Override
	public void init(IViewPart view) {
		// TODO Auto-generated method stub
		shell = view.getSite().getShell();
	}

	public void run(IAction action) {
		ViewPluginAction myAction;
		int j, listsize;
		ISelection mySelection;
		
		if (action instanceof ViewPluginAction){
			myAction = (ViewPluginAction) action;
		}
		else{
			return;
		}

		mySelection = myAction.getSelection();
		
		selectedList = new ArrayList();
		if (mySelection instanceof IStructuredSelection){
			
			for (Iterator it =((IStructuredSelection) mySelection).iterator();it.hasNext();){
				Object select_obj = it.next();
				
				if (select_obj != null && select_obj instanceof IResource){
					if (((IResource)select_obj).getType()==IResource.PROJECT){
						selectedList.add((IResource)select_obj);
					}
					else if (((IResource)select_obj).getType()==IResource.FOLDER){
						selectedList.add((IResource)select_obj);
					}
					else if (((IResource)select_obj).getType()==IResource.FILE){
						selectedList.add((IResource)select_obj);
					}
				}
				else if (select_obj != null && select_obj instanceof JavaProject){
					selectedList.add((IResource)((JavaProject)select_obj).getResource());
				}
				else if (select_obj != null && select_obj instanceof PackageFragmentRoot){
					selectedList.add((IResource)((PackageFragmentRoot)select_obj).getResource());
				}
				else if (select_obj != null && select_obj instanceof PackageFragment){
					selectedList.add((IResource)((PackageFragment)select_obj).getResource());
				}
				else if (select_obj != null && select_obj instanceof CompilationUnit){
					selectedList.add((IResource)((CompilationUnit)select_obj).getResource());
				}
				
			}
		}

		selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
		EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",selectResources,"","NONE"));
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

}
