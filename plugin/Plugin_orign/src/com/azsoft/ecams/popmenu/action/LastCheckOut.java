package com.azsoft.ecams.popmenu.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.ui.dialog.LastCheckOutDlg;

import com.azsoft.ecams.ui.wizard.LastCheckOutWizard;

public class LastCheckOut implements IObjectActionDelegate{
	private Shell shell;
	private LastCheckOutDlg lastCheckOutDlg;
	private IResource[] resources;
	private int lastcnt;
	
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
		shell = targetPart.getSite().getShell();
	}
	
	public void run(IAction action) {
		// TODO Auto-generated method stub
		ObjectPluginAction myAction;
		ISelection mySelection;
		if (action instanceof ObjectPluginAction){
			myAction = (ObjectPluginAction) action;
		}
		else{
			return;
		}
		
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveAllEditors(false);
		
		List selectedList = new ArrayList();
		mySelection = myAction.getSelection();
		int cnt = 0;
		if (mySelection instanceof IStructuredSelection){
			for (Iterator it =((IStructuredSelection) mySelection).iterator();it.hasNext();){
				Object select_obj = it.next();

				if (select_obj != null && select_obj instanceof IResource){
					if(((IResource)select_obj).getType()==IResource.FILE){
						selectedList.add((IResource)select_obj);
					}
					if (((IResource)select_obj).getType()==IResource.FOLDER || ((IResource)select_obj).getType()==IResource.PROJECT){
						cnt++;
					}
				}
			}
		}
		
		if(cnt>0){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("\ud30c\uc77c\uc744 \uc120\ud0dd\ud558\uc138\uc694");
			messageBox.setText("\ud30c\uc77c\uc120\ud0dd");
			messageBox.open();
			return;
		}
		
		IResource[] selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
		//IResource[] changedResource = getChangedResources(selectResource);

		resources = getSelectedResources(selectResources);
		
		if (resources == null){
			return;
		}
		
		
		if (resources.length < 1){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("\uccb4\ud06c\uc544\uc6c3 \ud560 \uc218 \uc788\ub294 \uc790\uc6d0\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.");
			messageBox.open();
			return;
		}else{
			if(lastcnt > 0){
				MessageBox messageBox = new MessageBox(shell, SWT.OK);
				messageBox.setMessage("\uc774\uc804\ubc84\uc804\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.");
				messageBox.open();
				return;
			}
		}
		String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
		String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
		
		if(ip == null){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c IP\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}else if(ip.equals("")){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c IP\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}
		if(port == null){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c PORT\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}else if(port.equals("")){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c PORT\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}
		if(id == null){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c ID\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}else if(id.equals("")){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c ID\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}
		
		if(passwd == null){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("Preferences\uc5d0\uc11c PASSWORD\ub97c \uc785\ub825\ud558\uc138\uc694.");
			messageBox.open();
			return;
		}else if(passwd.equals("")){
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
		
		
		//EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources));
		String filename = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]).getName();
		String itemid = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[0]).getItemid().toString();
		
		if(selectedList.size() == 1){
			LastCheckOutWizard wizard = new LastCheckOutWizard(resources, resources.length, itemid, filename, ip, port, id, passwd, "", "", false);
			lastCheckOutDlg = new LastCheckOutDlg(shell,wizard);
			wizard.setParentDialog(lastCheckOutDlg);
			lastCheckOutDlg.open();
		}
	}
	
	private IResource[] getSelectedResources(IResource[] selectResources){
		List findResourceList=null;
		Set addResources = new HashSet();
		IResource[] findResources;
		try {
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
					if (resourceStatus.isAuthority() && resourceStatus.isLocked() && resourceStatus.getFileStatus().split(":")[1].equals("0")){
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
			return (IResource[])findResourceList.toArray(new IResource[findResourceList.size()]);
		}		
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}	

}
