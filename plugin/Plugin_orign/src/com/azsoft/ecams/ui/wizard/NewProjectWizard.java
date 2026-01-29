package com.azsoft.ecams.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.azsoft.ecams.core.EcamsProjectNature;
import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.NewSyncJob;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.ui.wizard.page.NewProjectPage;


public class NewProjectWizard extends Wizard implements INewWizard {
	private IProject getProjectHandle;
	private IProject newProject;
	private IResource[] selectResources;
	private	String NO_PROJECT_NAME = "\ud504\ub85c\uc81d\ud2b8\uba85\uaddc\uce59\ubbf8\uc785\ub825";
	
	//private WizardNewProjectCreationPage mainPage;
	private NewProjectPage newProjectPage;
	
	public void addPages(){
		//mainPage = new WizardNewProjectCreationPage("New Custom Project");
		//mainPage.setTitle("New Custom Project");
		//mainPage.setDescription("Create a new project in the workspace");

		newProjectPage = new NewProjectPage("New eCAMS Project");
		newProjectPage.setTitle("New eCAMS Project");
		newProjectPage.setDescription("\uc2dc\uc2a4\ud15c\uc744 \uc120\ud0dd\ud574 \uc8fc\uc2ed\uc2dc\uc624.");
		
		//addPage(mainPage);
		addPage(newProjectPage);
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		setWindowTitle("New eCAMS Project");
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		//System.out.println("##############"+newProjectPage);
		
		if(newProjectPage != null){
			/*if( newProjectPage.getSys().split(":")[0].equals("01000") ){
				if( !mainPage.getProjectName().equals("WINK") && !mainPage.getProjectName().equals("Education")) {
					MessageBox messageBox = new MessageBox(this.getShell(), SWT.OK);
					messageBox.setMessage("\uacc4\uc815\uacc4\uc2dc\uc2a4\ud15c\uc758 \ud504\ub85c\uc81d\ud2b8\uba85\uc740 'WINK'\ub9cc \uac00\ub2a5\ud569\ub2c8\ub2e4.");
					messageBox.open();
					return false;
				}
			}*/
			
			if (newProjectPage.getPreference().equals("1")) {
				MessageDialog.openError(getShell(),"\ud655\uc778", "[Window>Preferences>eCAMS Plugin]\uc815\ubcf4\ub97c  \ud655\uc778\ud558\uc2ed\uc2dc\uc624.");
				return false;
			} else if(newProjectPage.getSys().length() < 1) {
				MessageDialog.openError(getShell(),"\ud655\uc778", "\uc2dc\uc2a4\ud15c\uc744 \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.");
				return false;
			} /*else if(newProjectPage.getJob().length() < 1) {
				MessageDialog.openError(getShell(),"\ud655\uc778", "\uc5c5\ubb34\ub97c \uc120\ud0dd\ud558\uc2ed\uc2dc\uc624.");
				return false;
			}*/

			String prjname = newProjectPage.getSys().split(":")[3];
			if(null == prjname || "".equals(newProjectPage.getSys().split(":")[3]) || prjname.equals(NO_PROJECT_NAME)){
				MessageDialog.openError(getShell(),"\ud655\uc778", "\ud615\uc0c1\uad00\ub9ac \uc6f9 \ud654\uba74 [\uad00\ub9ac\uc790>\uc2dc\uc2a4\ud15c\uc815\ubcf4]\uc5d0\uc11c \ud504\ub85c\uc81d\ud2b8\uba85\uc744 \uc785\ub825\ud574 \uc8fc\uc138\uc694.");
				return false;
			}
			
			//getProjectHandle = mainPage.getProjectHandle();
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			getProjectHandle = root.getProject(prjname);
			
			if(createNewProject() == null){
				return false;
			}

			try {
				getProjectHandle.setPersistentProperty(new QualifiedName("Properties","useyn"), "true");
				getProjectHandle.setPersistentProperty(new QualifiedName("Properties","syscd"), newProjectPage.getSys());
				getProjectHandle.setPersistentProperty(new QualifiedName("Properties","allrsrccd"), StringUtils.join(newProjectPage.getAll_List().getItems(),"/"));
				getProjectHandle.setPersistentProperty(new QualifiedName("Properties","setrsrccd"), StringUtils.join(newProjectPage.getSelt_List().getItems(),"/"));
				getProjectHandle.setPersistentProperty(new QualifiedName("Properties","setjobcd"), StringUtils.join(newProjectPage.getJob_List().toArray(),"/"));
				//System.out.println(StringUtils.join(newProjectPage.getJob_List().toArray(),"/"));
				
//						getProjectHandle.setPersistentProperty(new QualifiedName("Properties","setjobcd"), newProjectPage.getJob());
				getProjectHandle.setPersistentProperty(new QualifiedName("Properties","setanalyn"), newProjectPage.getSys().split(":")[2]);
				getProjectHandle.setPersistentProperty(new QualifiedName("Properties","sysinfo"), newProjectPage.getSys().split(":")[4]);
				getProjectHandle.setPersistentProperty(new QualifiedName("Properties","autosync"), "false");
				getProjectHandle.setPersistentProperty(new QualifiedName("Properties","ischanged"), "0");
				

				if( !getProjectHandle.getProject().isNatureEnabled(EcamsProjectNature.NATURE_ID) ) {
					EcamsProjectNature.setEcamsNature( getProjectHandle.getProject() );
				}
				 
				EcamsRepositoryProvider.setManagedByEcams(getProjectHandle);
				
				final List<IProject> projectList = new ArrayList<IProject>();
				projectList.add(getProjectHandle);
				EcamsProviderPlugin.broadcastModificationStateChanges((IResource[]) projectList.toArray(new IResource[projectList.size()]));
				
				//\ud504\ub85c\uc81d\ud2b8 \uc14b\ud305\uc644\ub8cc \ud6c4 \ub3d9\uae30\ud654 \uc791\uc5c5
				
				//EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",(IResource[]) projectList.toArray(new IResource[projectList.size()]),"","NEW"));
				EcamsProviderPlugin.getPlugin().getJobManager().addJob(new NewSyncJob("eCAMS Sync",(IResource[]) projectList.toArray(new IResource[projectList.size()])));
				
				EcamsProviderPlugin.broadcastModificationStateChanges((IResource[]) projectList.toArray(new IResource[projectList.size()]));
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		return false;
	}
	
	public IProject createNewProject(){
		if (newProject != null) {
			return newProject;
		}
		
		//getProjectHandle = mainPage.getProjectHandle();
		
		//IPath defaultPath = Platform.getLocation();
		//IPath newPath = mainPage.getLocationPath();
		//IPath newPath = new Path(getDirTxt);
		
		//if(defaultPath.equals(newPath)) newPath = null;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace.newProjectDescription(getProjectHandle.getName());
		//description.setLocation(newPath);
		
		WorkspaceModifyOperation op = new WorkspaceModifyOperation(){
			@Override
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException,
					InterruptedException {
				// TODO Auto-generated method stub
				createProject(description, getProjectHandle, monitor);
			}
		};
		
		try {
			getContainer().run(false,true, op);
			
			newProject = getProjectHandle;
			
			return newProject;
		} catch (InterruptedException e) {
			MessageDialog.openError(getShell(),"\ud504\ub85c\uc81d\ud2b8 \uc0dd\uc131\uc2e4\ud328", e.getCause().toString());
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			MessageDialog.openError(getShell(),"\ud504\ub85c\uc81d\ud2b8 \uc0dd\uc131\uc2e4\ud328", e.getCause().toString());
			e.printStackTrace();
			return null;
		}
	}
	
	public void createProject(IProjectDescription description, IProject projectHandle, 
			IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		try {
			monitor.beginTask("", 2000);
			
			projectHandle.create(description, new SubProgressMonitor(monitor, 1000));
			
			if(monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			
			projectHandle.open(new SubProgressMonitor(monitor, 1000));
			
		} finally {
			monitor.done();
		}
	}

	public IProject getNewProject() {
		return newProject;
	}
	
	public boolean canFinish() {
		// TODO Auto-generated method stub
		if(this.getContainer().getCurrentPage() instanceof NewProjectPage){
			return true;
		}else{
			return false;
		}
	}
	
}
