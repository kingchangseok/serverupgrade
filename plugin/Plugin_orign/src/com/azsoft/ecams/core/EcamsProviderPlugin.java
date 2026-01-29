package com.azsoft.ecams.core;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.azsoft.ecams.core.jobs.EcamsJobManager;
import com.azsoft.ecams.core.listeners.FileModificationManager;
import com.azsoft.ecams.core.listeners.IResourceStateChangeListener;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.core.status.XmlStatusMgr;
import com.azsoft.ecams.image.ImageUtil;
import com.azsoft.ecams.properties.DefaultPreferences;
import com.azsoft.ecams.util.xml.XmlReadWriter;

/**
 * The activator class controls the plug-in life cycle
 */
public class EcamsProviderPlugin extends AbstractUIPlugin implements IStartup{

	// Ecams plug-in ID
	
	public static final String ID = "com.azsoft.ecams.core"; //$NON-NLS-1$
	public static final String PROVIDER_ID="com.azsoft.ecams.core.ecamsnature"; //$NON-NLS-1$
	
	// all projects shared with ecams will have this nature
	private static final String NATURE_ID = ID + ".ecamsnature"; //$NON-NLS-1$

	
	// the plugin instance. @see getPlugin()
	private static volatile EcamsProviderPlugin instance;

	
	private FileModificationManager fileModificationManager;
	private XmlStatusMgr xmlStatusMgr;
	
	//private MetaFileManager metaFileManager;
	
	private EcamsJobManager jobManager;
	
	public EcamsJobManager getJobManager() {
		return jobManager;
	}

	public EcamsProviderPlugin() {
	    super();
	    instance = this;
	}
	
	public static EcamsProviderPlugin getPlugin(){
		return instance;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		
		
		jobManager = new EcamsJobManager();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		
		xmlStatusMgr = new XmlStatusMgr();
		fileModificationManager = new FileModificationManager();
		workspace.addResourceChangeListener(fileModificationManager,IResourceChangeEvent.POST_CHANGE);

		fileModificationManager.registerSaveParticipant();
		
		//Meta DB관리
		//dbStatusMgr = new DBStatusMgr();
		//EcamsLocalDBConn.setConnection();
		
		initialiseDefaultPreferences();	
	}


	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(fileModificationManager);

		//Meta DB관리
		//EcamsLocalDBConn.closeConnection();
		ImageUtil.removeImageRegistry();
		
		
		/*tmp file delete*/
		Bundle bundle = Platform.getBundle("com.azsoft.ecams.core");
		IPath path = Platform.getStateLocation(bundle);
		String fileUrl = path.toString()+"/.ecamsprojects";
		Document doc = XmlReadWriter.getDocument("ecamsprojects",fileUrl);
		if(doc == null) {
			return;
		}
		Element rootElement = doc.getRootElement();
		if(rootElement == null){
			return;
		}
		
		Element findElement=null;
		Iterator i;
		for (i = rootElement.elementIterator("ProjectInfo");i.hasNext();){
			findElement = (Element) i.next();
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(findElement.attributeValue("ProjectNM"));
			System.out.println("DELETE PROJECT:"+project.getName());
			
			if(project == null) continue;
			
			if(!project.isAccessible()) continue;
			
			if(!EcamsRepositoryProvider.isManagedByEcams(project)) continue;
			
			String folderpath = project.getLocation().toString()+"/.ecm_tmp";
			File tmpFolder = new File(folderpath);
			if(tmpFolder.exists()){
				File[] tmpfiles = tmpFolder.listFiles();
				for(int k=0; k<tmpfiles.length; k++){
					if(tmpfiles[k].exists()){
						System.out.println("DELETE TMP FILE:"+folderpath+"/"+tmpfiles[k].getName());
						
						IPath filepath = new Path(folderpath+"/"+tmpfiles[k].getName());

						IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
						IResource tmpResource = root.getFileForLocation(filepath);
						
						tmpfiles[k].delete();
						((IResource) tmpResource).refreshLocal(IResource.FILE, null);
						
						tmpResource = null;
						root = null;
						filepath = null;
					}
				}
				tmpfiles = null;
			}
			tmpFolder = null;
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static EcamsProviderPlugin getDefault() {
		return instance;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	protected void initialiseDefaultPreferences() {
		System.out.println("INITIALISING DEFAULTS");
		new DefaultPreferences();
	}
	
	    
	public XmlStatusMgr getXmlStatusMgr() {
		return xmlStatusMgr;
	}
    
	public static String getTypeId() {
		// TODO Auto-generated method stub
		return NATURE_ID;
	}

	public boolean isManagedByEcams(IProject project){
		try{
			if (project.getPersistentProperty(new QualifiedName("Properties","useyn"))== null){
				return false;
			}
			
			if (project.getPersistentProperty(new QualifiedName("Properties","useyn")).equals("true")){
				return true;
			}
			else{
				return false;
			}
		}
		catch (CoreException e){
			return false;
		}
	}

	public void earlyStartup() {
		// TODO Auto-generated method stub
		
	}
/*
	public IPath getSourcePath(IProject project){
		boolean isjavaProject;
		IPath sourceRoot = null;
		IResource[] projectResources;
		int i;
		
		IProjectDescription description;
		
		try {
			description = project.getDescription();
	
			String[] natures = description.getNatureIds();
			
			int chgstatus;
	
	
			isjavaProject = false;
			
			for (i=0;i<natures.length;i++){
				if (natures[i].equals("org.eclipse.jdt.core.javanature")){
					isjavaProject=true;
				}
			}
			
			
			if (isjavaProject){
				IJavaProject javaProject = JavaCore.create(project); 
	
				IClasspathEntry[] classpathEntries = javaProject.getResolvedClasspath(true);
				for (int j=0;j<classpathEntries.length;j++){
					IClasspathEntry entry = classpathEntries[j];
					
					if (entry.getContentKind()==IPackageFragmentRoot.K_SOURCE){
						
						String relativePath = entry.getPath().toString();
						sourceRoot = ResourcesPlugin.getWorkspace().getRoot().findMember(relativePath).getLocation();
						//projectSrcPath = sourceRoot.toOSString();
						break;
					}
				}
			}
			else{
				sourceRoot = project.getLocation();
				//projectSrcPath = sourceRoot.toOSString();
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			return sourceRoot;
		}
		
	}	
*/
	
	private static List<IResourceStateChangeListener> listeners = new ArrayList<IResourceStateChangeListener>();

	/*
	 * @see ITeamManager#addResourceStateChangeListener(IResourceStateChangeListener)
	 */
	public static void addResourceStateChangeListener(IResourceStateChangeListener listener) {
		synchronized(listeners) {
			listeners.add(listener);
		}
	}

	/*
	 * @see ITeamManager#removeResourceStateChangeListener(IResourceStateChangeListener)
	 */
	public static void removeResourceStateChangeListener(IResourceStateChangeListener listener) {
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}
	
	public static void broadcastModificationStateChanges(final IResource[] resources) {
		IResourceStateChangeListener[] toNotify;
		synchronized(listeners) {
			toNotify = (IResourceStateChangeListener[])listeners.toArray(new IResourceStateChangeListener[listeners.size()]);
		}

		for (int i = 0; i < toNotify.length; ++i) {
			final IResourceStateChangeListener listener = toNotify[i];
			ISafeRunnable code = new ISafeRunnable() {
				public void run() throws Exception {
					listener.resourceModified(resources);
				}
				public void handleException(Throwable e) {
					// don't log the exception....it is already being logged in
					// Platform#run
				}
			};
			SafeRunner.run(code);
		}
	}
		
}
