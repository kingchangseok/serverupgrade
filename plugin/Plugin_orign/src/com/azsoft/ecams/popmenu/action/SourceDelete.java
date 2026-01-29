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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.JobInfo;
import com.azsoft.ecams.proto.ProtoEcams.JobInfoList;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.dialog.DeleteDlg;
import com.azsoft.ecams.ui.wizard.DeleteWizard;

public class SourceDelete implements IObjectActionDelegate {
	private Shell shell;
	private DeleteDlg deleteDlg;
	private IResource[] resources;
	
	public void run(IAction action) {
		ObjectPluginAction myAction;
		ISelection mySelection;
		if (action instanceof ObjectPluginAction){
			myAction = (ObjectPluginAction) action;
		} else{
			return;
		}
		
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().saveAllEditors(false);
		
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
		
		if (selectedList.size() < 1) return;
		
		IResource[] selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
		
		resources = getSelectedResources(selectResources);
		
		if (resources == null) return;
		
		if (resources.length < 1){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("\ud30c\uc77c\uc0ad\uc81c \ud560 \uc218 \uc788\ub294 \uc790\uc6d0\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.");
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
			
			UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
			userinfo_builder.setId(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null));
			userinfo_builder.setPasswd(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null));
			SysInfo.Builder sysinfo_builder = SysInfo.newBuilder();
			sysinfo_builder.setSyscd((resources[0].getProject().getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0]));
			sysinfo_builder.setSysmsg((resources[0].getProject().getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[1]));
			JobInfoList.Builder jobinfo_builder = JobInfoList.newBuilder();
			JobInfo.Builder jobinfo = JobInfo.newBuilder();
			jobinfo.setJobcd((resources[0].getProject().getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[0]));
			jobinfo.setJobname((resources[0].getProject().getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[1]));
			jobinfo_builder.addJobinfo(jobinfo);
			
			EcamsMessage.Builder builder_msg = EcamsMessage.newBuilder();
			builder_msg.setMsgtype("MYJOB_CHECK");
			builder_msg.setUserinfo(userinfo_builder.build());
			builder_msg.setSysinfo(sysinfo_builder.build());
			builder_msg.setJobinfolist(jobinfo_builder.build());
			
			String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
			String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
			EcamsClient ecamsclient = new EcamsClient(ip,port);
			ReturnMsg returnMsg = ecamsclient.sendMsg(builder_msg.build());
			
			if (returnMsg.getReturnval() != 0){
				MessageBox messageBox = new MessageBox(shell, SWT.OK);
				messageBox.setMessage(returnMsg.getReturnStr());
				messageBox.open();
				return;		
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		DeleteWizard wizard = new DeleteWizard(resources, resources.length);
		deleteDlg = new DeleteDlg(shell,wizard);
		wizard.setParentDialog(deleteDlg);
		deleteDlg.open();
	}

	private IResource[] getSelectedResources(IResource[] selectResources){
		List findResourceList=null;
		List callResourceList=null;
		Set addResources = new HashSet();
		Set callResources = new HashSet();
		IResource[] findResources;
		IResource[] pathResources=null;
		boolean findflag = false;
		String parentPath = "";
		try {
			for (int i=0;i<selectResources.length;i++){
				if (selectResources[i].getType()==IResource.FILE){
					if (selectResources[i].getName().equals(".ecm") 
							|| selectResources[i].getParent().getName().equals(".deco") 
							|| selectResources[i].getParent().getName().equals(".settings")){
						continue;
					}
					/*
					findflag = false;
					if (parentPath == null || parentPath == "") findflag = true;
					else if (!selectResources[i].getParent().getLocation().toString().equals(parentPath)) {
						callResourceList = new ArrayList(callResources);
						pathResources = (IResource[])callResourceList.toArray(new IResource[callResourceList.size()]);
						findResources = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatuses_Total(pathResources, parentPath, "9");
						
						for (int j=0;findResources.length>j;j++) {
							addResources.add(findResources[j]);
							
						}
						pathResources = null;
						findflag = true;
					}
					if (findflag) {
						parentPath = selectResources[i].getParent().getLocation().toString();
					}
					callResources.add(selectResources[i]);
					*/
					IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[i]);
					if (resourceStatus == null){
						addResources.add(selectResources[i]);
					}
					if (resourceStatus.isAuthority() && ((resourceStatus.isLocked() && resourceStatus.getFileStatus().split(":")[1].equals("0")) || resourceStatus.getFileStatus().split(":")[1].equals("3"))){
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
			
			
			/*
			if (parentPath != null && !parentPath.equals("")) {
				callResourceList = new ArrayList(callResources);
				pathResources = (IResource[])callResourceList.toArray(new IResource[callResourceList.size()]);
				findResources = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatuses_Total(pathResources, parentPath, "9");
				
				for (int j=0;findResources.length>j;j++) {
					addResources.add(findResources[j]);
					
				}
			}
			*/
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			return (IResource[])findResourceList.toArray(new IResource[findResourceList.size()]);
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
					} else{
						aftProject = ((IResource)select_obj).getProject();
						if (!(befProject.getName().equals(aftProject.getName()))){
							multiProject=true;
							break;
						} else{
							continue;
						}
					}
				} else if(((IResource)select_obj).getType()==IResource.PROJECT){
					if (befProject == null){
						befProject = (IProject)select_obj;
					} else{
						aftProject = (IProject)select_obj;
						if (!(befProject.getName().equals(aftProject.getName()))){
							multiProject=true;
							break;
						} else{
							continue;
						}
					}
				} else{
					continue;
				}
			}
		}
		return multiProject;
	}
}
