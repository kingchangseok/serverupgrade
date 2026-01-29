package com.azsoft.ecams.core.jobs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class UnSyncJob extends WorkspaceJob {
	private IProject project;
	private Logger logger = Logger.getLogger(this.getClass());
	private List findResourceList = new ArrayList();
	
	public UnSyncJob(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public UnSyncJob(String name,IProject project) {
		super(name);
		this.project = project;
		// TODO Auto-generated constructor stub
	}
	
	public IStatus runInWorkspace(IProgressMonitor monitor){
		try {
			//ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);
			
			IResource[] projectResources = getECMFiles(project);
			
			/*if(projectResources!=null){
				for (int k=0;k<projectResources.length;k++){
					projectResources[k].delete(true, monitor);
				}
			}
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, monitor);	
			*/
			ResourcesPlugin.getWorkspace().getRoot().getProject(project.getName()).refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}catch (CoreException e) {
			logger.error(e.getCause().getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
		}
		return Status.OK_STATUS;
	}
	
	//private IResource[] getECMFiles(IResource resources){
	private IResource[] getECMFiles(IResource resources){
		IResource[] findResources;
		try {
			findResources = ((IContainer) resources).members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
			
			for(int i=0;i<findResources.length;i++){
				if (findResources[i].getType()==IResource.FOLDER){
					IResource[] subResources = getECMFiles(findResources[i]);
					
					String path = findResources[i].getLocation().toOSString();
					if(path.lastIndexOf(".deco")>0){
						File decofolder = new File(path);
						if(decofolder.exists()){
							System.out.println("delete path:"+path);
							decofolder.delete();
						}
					}else{
						for (int j=0;j<subResources.length;j++){
							if (subResources[j].getType()==IResource.FILE){
								/*if (subResources[j].getName().equals(".ecm") 
										|| subResources[i].getParent().getName().equals(".deco") 
										|| subResources[i].getParent().getName().equals(".settings")){
									findResourceList.add(subResources[j]);
								}*/
								if (subResources[j].getName().lastIndexOf(".ecm-meta")>0){
									findResourceList.add(subResources[j]);
								}
							}
						}
					}
				}
				else if (findResources[i].getType()==IResource.FILE){
					/*if (findResources[i].getName().equals(".ecm") 
							|| findResources[i].getParent().getName().equals(".deco") 
							|| findResources[i].getParent().getName().equals(".settings")){
						findResourceList.add(findResources[i]);
					}*/
					/*if(findResources[i].getName().lastIndexOf(".ecm-meta")>0){
						findResourceList.add(findResources[i]);
						
					}
					*/
					/*System.out.println("parent path:"+findResources[i].getParent().getLocation());
					if(findResources[i].getParent().getName().equals(".deco")){
						String path = findResources[i].getParent().getLocation().toString();
						File decofolder = new File(path);
						if(decofolder.exists()){
							System.out.println("delete path:"+path);
							decofolder.delete();
						}
					}*/
					if(findResources[i].getName().lastIndexOf(".ecm-meta")>0){
						String filename = findResources[i].getLocation().toString();
						File decofile = new File(filename);
						if(decofile.exists()){
							System.out.println("delete file:"+filename);
							decofile.delete();
						}
					}
				}
			}
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(findResourceList != null){
			return (IResource[])findResourceList.toArray(new IResource[findResourceList.size()]);
		}else{
			return null;
		}
		
	}

}
