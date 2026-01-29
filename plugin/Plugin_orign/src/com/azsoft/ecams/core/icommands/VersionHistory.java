package com.azsoft.ecams.core.icommands;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.team.ui.history.IHistoryView;

import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.popmenu.ShowHistoryPageSource;
import com.azsoft.ecams.ui.dialog.ShowHistoryPage;


public class VersionHistory extends AbstractHandler{
	private Logger logger = Logger.getLogger(this.getClass());

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		Map<String, String> map = event.getParameters();

		List<IResource> resourceList = new ArrayList<IResource>();
		IResource[] resources = null;
		IResource tmpResource = null;

		String filepath = null;
		filepath = map.get("PATH");
//		logger.error("filepath:"+filepath);
		logger.error("VersionHistor     Start");
		
		if(filepath != null){
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

			IProject project = root.getProject(filename.substring(0,filename.split("/")[0].length()));
			
			if(!project.isAccessible()){
				logger.debug("Poject is closed.");
			}else if(!EcamsRepositoryProvider.isManagedByEcams(project)){
				logger.debug("Is Not eCAMS Project.[ Connection Project -> Properties eCAMS PlugIn]");
			}else{
				filename = project.getLocation()+filename.substring(filename.split("/")[0].length());
				
				IPath tmpPath = new Path(filename);
		
				tmpResource = null;
				tmpResource = root.getFileForLocation(tmpPath);
				resourceList.add(tmpResource);
				
				if(resourceList.size()>0){
					resources = (IResource[])resourceList.toArray(new IResource[resourceList.size()]);
					
					if(resources != null){
						IHistoryView view = TeamUI.showHistoryFor(TeamUIPlugin.getActivePage(), resources, ShowHistoryPageSource.getInstance());
						IHistoryPage page = view.getHistoryPage();
						if (page instanceof ShowHistoryPage){
							ShowHistoryPage historyPage = (ShowHistoryPage) page;
							historyPage.setFocus();
						}
					}
				}
			}
		}else{
			logger.debug("No Parameter");
		}
		logger.error("VersionHistor     End");
		return null;
	}

}
