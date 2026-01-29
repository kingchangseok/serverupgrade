package com.azsoft.ecams.popmenu.action;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
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

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.core.jobs.SyncJob;
import com.azsoft.ecams.socket.EcamsClient;
import com.azsoft.ecams.ui.dialog.RegistFileAllDlg;
import com.azsoft.ecams.ui.dialog.CheckOutDlg;
import com.azsoft.ecams.ui.wizard.RegistFileAllWizard;
import com.azsoft.ecams.ui.wizard.CheckOutWizard;
import com.azsoft.ecams.util.checksum.CheckSum;
import com.azsoft.ecams.util.file.EFileToByteArray;
import com.azsoft.ecams.properties.WorkspacePreferences;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.JobInfo;
import com.azsoft.ecams.proto.ProtoEcams.JobInfoList;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.SysInfo;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;

public class RegistFileNew implements IViewActionDelegate {
	private Shell shell;
	private RegistFileAllDlg registFileAllDlg;
	private IResource[] resources;
	//private IResource[] resources2;
	
	List projectList = new ArrayList();
	private int i;

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
		
		if (selectedList.size() < 1){
			return;
		}
		
		IResource[] selectResources = (IResource[])selectedList.toArray(new IResource[selectedList.size()]);
		//IResource[] changedResource = getChangedResources(selectResource);
		resources = getSelectedResources(selectResources);

		if (resources == null){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			messageBox.setMessage("\ud504\ub85c\uadf8\ub7a8\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.");
			messageBox.open();
			return;
		}
	
		if (resources.length < 1){
			MessageBox messageBox = new MessageBox(shell, SWT.OK);
			//messageBox.setMessage("\uccb4\ud06c\uc544\uc6c3 \ud6c4 \ub0b4\uc6a9\uc774 \uc218\uc815\ub41c \ud30c\uc77c\ub9cc \uccb4\ud06c\uc778 \uac00\ub2a5\ud569\ub2c8\ub2e4. [\ud615\uc0c1\uad00\ub9ac\ub300\uc0c1\ud30c\uc77c\ub9cc]"); //\uccb4\ud06c\uc544\uc6c3 \ud6c4 \ub0b4\uc6a9\uc774 \uc218\uc815\ub41c \ud30c\uc77c\ub9cc \uccb4\ud06c\uc778\uac00\ub2a5\ud569\ub2c8\ub2e4.
			//messageBox.setMessage("\uccb4\ud06c\uc778\ub300\uc0c1\uc774\uc5c6\uc2b5\ub2c8\ub2e4.[\uccb4\ud06c\uc544\uc6c3 \ub610\ub294 \uc2e0\uaddc\ub4f1\ub85d \ud6c4 \uccb4\ud06c\uc778\ud558\uc2dc\uae30 \ubc14\ub78d\ub2c8\ub2e4.]");
			messageBox.setMessage("\uc2e0\uaddc\ub4f1\ub85d \ub300\uc0c1\uc774 \uc5c6\uc2b5\ub2c8\ub2e4.");
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
		
		EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",selectResources,"","NONE"));
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		RegistFileAllWizard wizard = new RegistFileAllWizard(resources, "NEW");
		registFileAllDlg = new RegistFileAllDlg(shell,wizard);
		wizard.setParentDialog(registFileAllDlg);
		registFileAllDlg.open();
	}


	private IResource[] getSelectedResources(IResource[] selectResources){
		List findResourceList=null;
		Set addResources = new HashSet();
		try {
			for (int i=0;i<selectResources.length;i++){
				if (selectResources[i].getType()==IResource.FILE){
					//System.out.println(selectResources[i].getParent().getName());
					//selectResources[i].getName().equals(".ecm") 
					//|| selectResources[i].getName().lastIndexOf("-QueryInclude")>0
					//|| selectResources[i].getName().lastIndexOf("-ServiceCategory")>0
					//|| selectResources[i].getName().lastIndexOf("-PageCategory")>0
					
					
					/*
					 * 
					|| (selectResources[i].getName().substring(0,1).equals(".") 
							&& !selectResources[i].getName().equals(".project") && !selectResources[i].getName().equals(".classpath"))
							|| selectResources[i].getParent().getLocation().toString().lastIndexOf("unittest")>0
							|| selectResources[i].getParent().getLocation().toString().lastIndexOf("WebContent/log")>0
							|| selectResources[i].getParent().getLocation().toString().lastIndexOf("WebContent/upload")>0
					 */
					if (selectResources[i].getParent().getName().equals(".deco")
							//|| selectResources[i].getName().lastIndexOf(".class")>0
							|| selectResources[i].getParent().getLocation().toString().lastIndexOf(".svn")>0){
						continue;
					}
					//System.out.println(selectResources[i].getParent().getLocation());
					IEcamsStatus resourceStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(selectResources[i]);
					if (resourceStatus == null){
						//continue;
						addResources.add(selectResources[i]);
//					}else{
//						if ( resourceStatus.getFileStatus().split(":")[1].equals("3")
//								 && resourceStatus.getEditor().split(":")[1].equals(Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null))){
//							//byte[] testbyte = null;
//							try {
//								FileChannel inChannel = new FileInputStream(resourceStatus.getFile()).getChannel();
//								int size = (int)inChannel.size();
//								if(size<1){
//									continue;
//								}
//								//testbyte = EFileToByteArray.FileToByteArray(resourceStatus.getFile());
//							} catch (IOException e) {
//								// TODO Auto-generated catch block
//								continue;
//							}
//							/*
//							if(!CheckSum.MD5SumVal(testbyte).equals(resourceStatus.getMd5sum())){
//								addResources.add(selectResources[i]);
//							}
//							*/
//							addResources.add(selectResources[i]);
//						}
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
		}else {
			return null;
		}
	}	
	
	public void selectionChanged(IAction action, ISelection selection) {
	}

	public boolean duplCheck(IStructuredSelection selection){
		boolean multiProject = false;
		IProject befProject=null;
		IProject aftProject=null;
		for (Iterator it =selection.iterator();it.hasNext();){
			Object select_obj = it.next();
			
			if ( select_obj != null && select_obj instanceof JavaProject) {
				select_obj = (IResource)((JavaProject)select_obj).getResource();
			}
			else if ( select_obj != null && select_obj instanceof PackageFragmentRoot ) {
				select_obj = (IResource)((PackageFragmentRoot)select_obj).getResource();
			}
			else if ( select_obj != null && select_obj instanceof PackageFragment ) {
				select_obj = (IResource)((PackageFragment)select_obj).getResource();
			}
			else if ( select_obj != null && select_obj instanceof CompilationUnit ) {
				select_obj = (IResource)((CompilationUnit)select_obj).getResource();
			}
			
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
