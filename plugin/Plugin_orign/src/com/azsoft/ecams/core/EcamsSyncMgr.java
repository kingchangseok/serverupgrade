package com.azsoft.ecams.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import com.azsoft.ecams.core.jobs.SyncJob;

public class EcamsSyncMgr {
	
	public EcamsSyncMgr(){
		int i;
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		List<IProject> ecamsProjects = new ArrayList<IProject>();
		
		for (i=0;i<projects.length;i++){
			try {
				if (EcamsProviderPlugin.getPlugin().isManagedByEcams(projects[i]) && projects[i].getPersistentProperty(new QualifiedName("Properties","autosync")).equals("true")){
					ecamsProjects.add(projects[i]);
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (ecamsProjects.size() > 0){
			EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",(IResource[])ecamsProjects.toArray(new IResource[ecamsProjects.size()]),"","NONE"));
		}
	}
}
