package com.azsoft.ecams.popmenu.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.ui.dialog.CheckOutCnlDlg;
import com.azsoft.ecams.ui.wizard.CheckOutCnlWizard;
import com.azsoft.ecams.properties.IProperty;

public class CheckOutCnl implements IObjectActionDelegate {
	private Shell shell;
	private CheckOutCnlDlg checkOutCnlDlg;
	private IResource[] resources;
	
	public void run(IAction action) {
		ObjectPluginAction myAction;
		ISelection mySelection;
		if (action instanceof ObjectPluginAction){
			myAction = (ObjectPluginAction) action;
		}
		else{
			return;
		}
		
		mySelection = myAction.getSelection();
		
		List selectedList = new ArrayList();
		
		if (mySelection instanceof IStructuredSelection){
			if (duplCheck((IStructuredSelection) mySelection)){
				MessageBox messageBox = new MessageBox(shell, SWT.OK);
				messageBox.setMessage("\ud558\ub098\uc758 \ud504\ub85c\uc81d\ud2b8 \uc790\uc6d0\ub4e4\ub9cc \uc2e0\uccad \ud558\uc2e4\uc218 \uc788\uc2b5\ub2c8\ub2e4.");
				messageBox.open();
				return;
			}
			for (Iterator it =((IStructuredSelection) mySelection).iterator();it.hasNext();){
				Object select_obj = it.next();

				if (select_obj != null && select_obj instanceof IResource){
					if (((IResource)select_obj).getType()==IResource.FILE ||
						((IResource)select_obj).getType()==IResource.FOLDER ||
						((IResource)select_obj).getType()==IResource.PROJECT){
						selectedList.add((IResource)select_obj);
					}
				}
			}
		}
		
		if (selectedList.size() < 1){
			return;
		}
		
		IResource[] selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
		//IResource[] changedResource = getChangedResources(selectResource);

		resources = getSelectedResources(selectResources);
		
		//System.out.println(resources.length);
		if (resources == null){
			return;
		}
		
		if (resources.length < 1){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("\ucde8\uc18c \ud560 \uc218 \uc788\ub294 \uc790\uc6d0\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.");
			messageBox.open();
			return;
		}
		
		if(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null) == null){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c IP\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}else if(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null).equals("")){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c IP\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}
		if(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null) == null){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c PORT\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}else if(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null).equals("")){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c PORT\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}
		if(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null) == null){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c ID\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}else if(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null).equals("")){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c ID\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}
		
		if(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null) == null){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c PASSWORD\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}else if(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null).equals("")){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c PASSWORD\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}
		
		try {
			if ((resources[0].getProject().getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0])== null){
				MessageBox messageBox = new MessageBox(shell, SWT.OK);
				messageBox.setMessage("Properties\uc5d0\uc11c \uc2dc\uc2a4\ud15c\uc744 \uc120\ud0dd\ud558\uc138\uc694.");
				messageBox.open();
				return;
			}else if((resources[0].getProject().getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]).equals("")){
				MessageBox messageBox = new MessageBox(shell, SWT.OK);
				messageBox.setMessage("Properties\uc5d0\uc11c \uc2dc\uc2a4\ud15c\uc744 \uc120\ud0dd\ud558\uc138\uc694.");
				messageBox.open();
				return;
			}
			if((resources[0].getProject().getPersistentProperty(new QualifiedName("Properties","setrsrccd")).split("/")[0].split(":")[0])== null){
				MessageBox messageBox = new MessageBox(shell, SWT.OK);
				messageBox.setMessage("Properties\uc5d0\uc11c \ud504\ub85c\uadf8\ub7a8\uc885\ub958\ub97c \uc120\ud0dd\ud558\uc138\uc694.");
				messageBox.open();
				return;
			}else if((resources[0].getProject().getPersistentProperty(new QualifiedName("Properties","setrsrccd")).split("/")[0].split(":")[0].equals(""))){
				MessageBox messageBox = new MessageBox(shell, SWT.OK);
				messageBox.setMessage("Properties\uc5d0\uc11c \ud504\ub85c\uadf8\ub7a8\uc885\ub958\ub97c \uc120\ud0dd\ud558\uc138\uc694.");
				messageBox.open();
				return;
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CheckOutCnlWizard wizard = new CheckOutCnlWizard(resources);
		checkOutCnlDlg = new CheckOutCnlDlg(shell,wizard);
		wizard.setParentDialog(checkOutCnlDlg);
		checkOutCnlDlg.open();
	}


	private IResource[] getSelectedResources(IResource[] selectResources){
		List findResourceList=null;
		Set addResources = new HashSet();
		try {
			String parentPath = "";
			boolean findflag = false;
			
			for (int i=0;i<selectResources.length;i++){
				if (selectResources[i].getType()==IResource.FILE){
					if (selectResources[i].getParent().getName().equals(".deco") 
							|| selectResources[i].getParent().getName().equals(".settings")){
						continue;
					}
					IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[i]);
					if (resourceStatus == null){
						continue;
					}
					if ((resourceStatus.isAuthority() && ((!resourceStatus.isLocked() && resourceStatus.getFileStatus().split(":")[1].equals("5")))
							&& resourceStatus.getEditor().split(":")[1].equals(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null)))){
						addResources.add(selectResources[i]);
					}
				}
				else if (selectResources[i].getType()==IResource.FOLDER || selectResources[i].getType()==IResource.PROJECT){
					IResource[] childFindResources = getSelectedResources(((IContainer) selectResources[i]).members());
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
		}else{
			return null;
		}
	}	

	public void selectionChanged(IAction action, ISelection selection) {
	}


	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		shell = targetPart.getSite().getShell();
	}
	
	public boolean duplCheck(IStructuredSelection selection){
		boolean multiProject = false;
		IProject befProject=null;
		IProject aftProject=null;
		for (Iterator it =selection.iterator();it.hasNext();){
			Object select_obj = it.next();
			if (select_obj != null && select_obj instanceof IResource){
				if (((IResource)select_obj).getType()==IResource.FILE ||
					((IResource)select_obj).getType()==IResource.FOLDER){
					if (befProject == null){
						befProject = ((IResource)select_obj).getProject();
						continue;
					}
					else{
						aftProject = ((IResource)select_obj).getProject();
						if (!(befProject.getName().equals(aftProject.getName()))){
							multiProject=true;
							break;
						}
						else{
							continue;
						}
					}
				}
				else if(((IResource)select_obj).getType()==IResource.PROJECT){
					if (befProject == null){
						befProject = (IProject)select_obj;
					}
					else{
						aftProject = (IProject)select_obj;
						if (!(befProject.getName().equals(aftProject.getName()))){
							multiProject=true;
							break;
						}
						else{
							continue;
						}
					}
				}
				else{
					continue;
				}
			}
		}
		
		return multiProject;
		
	}

}