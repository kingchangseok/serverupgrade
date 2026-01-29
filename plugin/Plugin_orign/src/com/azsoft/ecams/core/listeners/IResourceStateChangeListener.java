package com.azsoft.ecams.core.listeners;

import java.util.EventListener;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public interface IResourceStateChangeListener extends EventListener {
	public void resourceModified(IResource[] changedResources);
	
	public void resourceModified(IResource changedResource);
	
	/**
	 * Notifies this listener that the project has just been configured
	 * to be a Subversion project.
	 *
	 * @param project The project that has just been configured
	 */
	public void projectConfigured(IProject project);
	
	/**
	 * Notifies this listener that the project has just been deconfigured
	 * and no longer has the SVN nature.
	 *
	 * @param project The project that has just been configured
	 */
	public void projectDeconfigured(IProject project);
	
	public void initialize();
}
