package com.azsoft.ecams.core.listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;

public class FileModificationManager implements IResourceChangeListener, ISaveParticipant {
	protected int INTERESTING_CHANGES = IResourceDelta.CONTENT | 
    									IResourceDelta.MOVED_FROM | 
                                        IResourceDelta.MOVED_TO |
                                        IResourceDelta.OPEN | 
                                        IResourceDelta.REPLACED |
                                        IResourceDelta.TYPE | 
                                        IResourceDelta.SYNC;

	public void resourceChanged(IResourceChangeEvent event) {
		//ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		
		final List<IResource> modifiedResources = new ArrayList<IResource>();
		
		// TODO Auto-generated method stub
		try {
			event.getDelta().accept(new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) {
					IResource resource = delta.getResource();

					if (resource.getType()==IResource.FILE) {
						if (!resource.isDerived() && !resource.getLocation().lastSegment().equals(".deco")
								&& !resource.isDerived() && !resource.getLocation().lastSegment().equals(".setting")){

							if (delta.getKind() == IResourceDelta.CHANGED && resource.exists()) {
								
								if((delta.getFlags() & INTERESTING_CHANGES) != 0) {
									if (EcamsProviderPlugin.getPlugin().getXmlStatusMgr().updateStatus(resource)){
									//if (EcamsProviderPlugin.getPlugin().getDBStatusMgr().updateStatus(resource)){
							    		modifiedResources.add(resource);
									}
									
								}
							}
							else if(delta.getKind() == IResourceDelta.ADDED && resource.exists()){
								modifiedResources.add(resource);
							}
							else if(delta.getKind() == IResourceDelta.REMOVED){
								//EcamsProviderPlugin.getPlugin().getDBStatusMgr().removeStatus(resource);
								EcamsProviderPlugin.getPlugin().getXmlStatusMgr().removeStatus(resource);
								modifiedResources.add(resource);
							}
						}
						
						if(modifiedResources.size()>0){
							IResource[] parents = getParent(resource);
							for(int i=0; i<parents.length; i++){
								modifiedResources.add(parents[i]);
							}
						}
					} else if (resource.getType()==IResource.PROJECT) {
						IProject project = (IProject)resource;
						
						if(delta.getKind() == IResourceDelta.REMOVED){
							modifiedResources.add(resource);
						}
						
						if (null == project){
							return false;
						}
						
						if (!project.isAccessible()) {
							return false;
						}
						
						
						if ((delta.getKind() & IResourceDelta.REMOVED) != 0) {
							EcamsRepositoryProvider.unsetManagedByEcams(project);
							return false;
						}

						if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
							return false;
						} 
					} else if(resource.getType()==IResource.FOLDER) {
						if (delta.getKind() == IResourceDelta.CHANGED && resource.exists()) {
							
							if((delta.getFlags() & INTERESTING_CHANGES) != 0) {
								modifiedResources.add(resource);
							}
						}else if(delta.getKind() == IResourceDelta.REMOVED || (delta.getKind() == IResourceDelta.ADDED && resource.exists())){
							modifiedResources.add(resource);
						}
						
						if(modifiedResources.size()>0){
							IResource[] parents = getParent(resource);
							for(int i=0; i<parents.length; i++){
								modifiedResources.add(parents[i]);
							}
						}
					} else {
						return true;
					}
					
					IResource project = resource.getProject();
					modifiedResources.add(project);
					project = null;
					
					return true;
				}
			});
			
			if (!modifiedResources.isEmpty()) {
                EcamsProviderPlugin.broadcastModificationStateChanges((IResource[]) modifiedResources.toArray(new IResource[modifiedResources.size()]));
			}
		} catch (CoreException e) {
			
		}
		
	}
	
	private IResource[] getParent(IResource resource){
		List findResourceList= null;
		Set<IResource> addResources = new HashSet<IResource>();
		
		IContainer parent = resource.getParent();
		if(parent.getType() == IContainer.FOLDER){
			addResources.add(parent);
			String[] splitStr = resource.getParent().getLocation().toString().replace(resource.getProject().getLocation().toString(), "").split("/");
			//System.out.println(splitStr);
			parent = resource.getParent().getParent();
			for(int j=0; j<splitStr.length; j++){
				if(parent.getType() == IContainer.FOLDER){
					addResources.add(parent);
					parent = parent.getParent();
				}
				
			}
		}
		
		findResourceList = new ArrayList(addResources);
    	
    	if(findResourceList != null){
			return (IResource[])findResourceList.toArray(new IResource[findResourceList.size()]);
		}else{
			return null;
		}
	}
	
	public void registerSaveParticipant() throws CoreException {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		ISavedState ss = ws.addSaveParticipant(EcamsProviderPlugin.getPlugin(), this);
		if (ss != null) {
			ss.processResourceChangeEvents(this);
		}
		ws.removeSaveParticipant(EcamsProviderPlugin.getPlugin());
	}
	
	public void doneSaving(ISaveContext context) {
		// TODO Auto-generated method stub
		
	}

	public void prepareToSave(ISaveContext context) throws CoreException {
		// TODO Auto-generated method stub
		
	}

	public void rollback(ISaveContext context) {
		// TODO Auto-generated method stub
		
	}

	public void saving(ISaveContext context) throws CoreException {
		// TODO Auto-generated method stub
		
	}

}
