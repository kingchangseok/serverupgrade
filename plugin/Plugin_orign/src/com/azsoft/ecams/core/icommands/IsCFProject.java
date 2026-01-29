package com.azsoft.ecams.core.icommands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;

public class IsCFProject extends AbstractHandler{
	private Logger logger = Logger.getLogger(this.getClass());

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Boolean> execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		Map<String, List<String>> map = event.getParameters();
		
		Map<String, Boolean> retMap = new HashMap<String, Boolean>();
		boolean prjFlg = false;
		logger.error("IsCFProject    Start");
		//logger.error("PROJECT_LIST size:"+map.get("PROJECT_LIST").size());
		
		for(int i=0; i<map.get("PROJECT_LIST").size(); i++){
			prjFlg = false;
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(map.get("PROJECT_LIST").get(i));
			logger.debug("IsCFProject PROJECT_LIST"+project.getName());
			System.out.println("IsCFProject PROJECT_LIST"+project.getName());
			if(project.isAccessible()){
				/*MessageBox messageBox = new MessageBox(new Shell());
				messageBox.setMessage("Poject is closed.");
				messageBox.setText("Commands ERROR");
				messageBox.open();
			}else{*/
				if(EcamsRepositoryProvider.isManagedByEcams(project)){
					prjFlg = true;
				}
				//retMap.put(map.get("PROJECT_LIST").get(i), prjFlg);
			}
			retMap.put(project.getName(), prjFlg);
			//logger.error("NAME:FLAG="+project.getName()+":"+prjFlg);
		}
		logger.error("IsCFProject    End");
		return retMap;
	}

}