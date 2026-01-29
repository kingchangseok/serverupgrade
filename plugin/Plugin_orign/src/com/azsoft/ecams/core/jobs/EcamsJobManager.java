package com.azsoft.ecams.core.jobs;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;


public class EcamsJobManager {
	
	public EcamsJobManager(){
		
	}
	
	public void addJob(Job addJob){
		addJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
		addJob.setUser(true);
		addJob.schedule();
	}
	
	
	public void addJob(IWorkspaceRunnable addJob){
		try {
			run(addJob, ResourcesPlugin.getWorkspace().getRoot(),null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void addJob(IWorkspaceRunnable addJob,ISchedulingRule rule){
		try {
			run(addJob, rule, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addJob(IWorkspaceRunnable addJob,ISchedulingRule rule,IProgressMonitor monitor){
		try {
			run(addJob, rule,monitor);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
    public void run(final IWorkspaceRunnable job, IProgressMonitor monitor) throws CoreException {
    	final CoreException[] error = new CoreException[1];
    	try {
    		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
    			public void run(IProgressMonitor monitor) {
    				try {
    					job.run(monitor);
    				}catch (CoreException e) {
    					error[0] = e;
    				} finally {
						monitor.done();
					}    				
    			}
    		}, monitor);
    	} catch (CoreException e) {
    		error[0] = e;
    	}
    	
    	if (error[0] != null) {
    		throw error[0];
    	}
    }

    public void run(final IWorkspaceRunnable job, ISchedulingRule rule, IProgressMonitor monitor) throws CoreException {
    	final CoreException[] error = new CoreException[1];
    	try {
    		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
    			public void run(IProgressMonitor monitor) {
   					try {
   						job.run(monitor);
    				}catch (CoreException e) {
    					error[0] = e;
    				}finally {
   						monitor.done();
   					}    				
    			}
    		}, rule, IWorkspace.AVOID_UPDATE, monitor);
    	} catch (CoreException e) {
    		error[0] = e;
    	}
    	if (error[0] != null) {
    		throw error[0];
    	}
    }
    
}
