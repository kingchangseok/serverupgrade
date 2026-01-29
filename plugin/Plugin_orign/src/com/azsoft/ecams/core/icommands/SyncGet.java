package com.azsoft.ecams.core.icommands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.jobs.SyncJob;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.properties.IProperty;

public class SyncGet extends AbstractHandler{
	private Logger logger = Logger.getLogger(this.getClass());

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub

		String ip = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.IP, "", null);
		String port = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PORT, "", null);
		String id = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.ID, "", null);
		String passwd = Platform.getPreferencesService().getString(EcamsProviderPlugin.ID, IProperty.PASSWD, "", null);
		logger.error("SyncGet    Start");
		if(ip == null || port == null || id == null || passwd == null){
			logger.debug("[Window>Pregerence>eCAMS Plugin] Insert ip, port, id, password");
		}else{
			Map<String, List<String>> map = event.getParameters();
			
			List resourceList = new ArrayList();
			IResource[] resources = null;
			IResource tmpResource = null;
			
			logger.debug("PATH_LIST size: "+map.get("PATH_LIST").size());
			for(int i=0; i<map.get("PATH_LIST").size(); i++){
				String filepath = map.get("PATH_LIST").get(i);
				logger.debug("PATH_LIST : "+filepath);
	
				while(filepath.indexOf("/") >=0){
					filepath = filepath.replace("/","\\");
				}
	
				while(filepath.indexOf("\\\\") >=0){
					filepath = filepath.replace("\\\\", "\\");
				}
	
				while(filepath.indexOf("\\") >=0){
					filepath = filepath.replace("\\", "/");
				}	
	
				String filename = filepath;
	
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IProject project = root.getProject(filename.split("/")[0]);
				
				if(!project.isAccessible()){
					continue;
				}else if(!EcamsRepositoryProvider.isManagedByEcams(project)){
					continue;
				}else{
					filename = project.getLocation()+filename.substring(filename.split("/")[0].length());
					IPath tmpPath = new Path(filename);
					
					logger.debug("PATH : "+filename);
					
					File file = new File(filename);

					tmpResource = null;
					if(file.isFile()){
						tmpResource = root.getFileForLocation(tmpPath);
					}else if(file.isDirectory()){
						tmpResource = root.getFolder(tmpPath);
					}else{
						tmpResource = root.getProject();
					}
					
					resourceList.add(tmpResource);
				}
			}
			
			if(resourceList.size()>0){
				resources = (IResource[])resourceList.toArray(new IResource[resourceList.size()]);
				
				if(resources != null){
					EcamsProviderPlugin.getPlugin().getJobManager().addJob(new SyncJob("eCAMS Sync",resources,"","NONE"));
				}
			}
		}
		logger.error("SyncGet    End");
		return null;
	}
}
