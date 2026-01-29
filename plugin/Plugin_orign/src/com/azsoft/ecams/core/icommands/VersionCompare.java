package com.azsoft.ecams.core.icommands;

import java.io.File;
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

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.ui.dialog.SourceDiffDlg;


public class VersionCompare extends AbstractHandler{
	private Logger logger = Logger.getLogger(this.getClass());

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		Map<String, String> map = event.getParameters();

		IResource tmpResource = null;

		String filepath = null;
		filepath = map.get("PATH");
		logger.error("filepath:"+filepath);
		logger.error("VersionCompare    Start");
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
			logger.debug("filename:"+filename);
	
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

			IProject project = root.getProject(filename.substring(0,filename.split("/")[0].length()));
			logger.debug("project:"+project);
			
			if(!project.isAccessible()){
				logger.debug("Poject is closed.");
			}else if(!EcamsRepositoryProvider.isManagedByEcams(project)){
				logger.debug("Is Not eCAMS Project.[ Connection Project -> Properties eCAMS PlugIn]");
			}else{
				filename = project.getLocation()+filename.substring(filename.split("/")[0].length());
				logger.debug("filename2:"+filename);
				IPath tmpPath = new Path(filename);
				logger.debug("tmpPath:"+tmpPath);

				File file = new File(filename);

				tmpResource = null;
				if(file.isFile()){
					tmpResource = root.getFileForLocation(tmpPath);
					logger.debug("tmpResource:"+tmpResource);
				}
				
				if(null != tmpResource){
					IEcamsStatus ecmStatus = EcamsProviderPlugin.getPlugin().getXmlStatusMgr().getStatus(tmpResource);
					
					if(null == ecmStatus){
						logger.debug("No data found \n \ud30c\uc77c\uc815\ubcf4\uac00 \uc5c6\uc2b5\ub2c8\ub2e4.");
					}else{
						logger.debug(ecmStatus.getName()+","+ecmStatus.getItemid()+","+ecmStatus.getRsrcinfo());
						SourceDiffDlg sourcediffdlg = new SourceDiffDlg(new Shell(), ecmStatus.getName(), ecmStatus.getItemid(), ecmStatus.getRsrcinfo(), ecmStatus.getPath(), tmpResource.getProject());
						sourcediffdlg.open();
					}
				}else{
					logger.debug("\ud30c\uc77c\uc120\ud0dd \n \ud558\ub098\uc758 \ud30c\uc77c\uc744 \uc120\ud0dd\ud558\uc138\uc694");
				}
			}
		}else{
			logger.debug("No Parameter");
		}
		logger.error("VersionCompare    End");
		return null;
	}

}
