package com.azsoft.ecams.core.icommands;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.util.xml.XmlReadWriter;

public class VersionInfo extends AbstractHandler{
	private Logger logger = Logger.getLogger(this.getClass());

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		Map<String, List<String>> map = event.getParameters();
		
		Map<String, String> retMap = new HashMap<String, String>();
		
		logger.error("Versioninfo    Start");
		logger.error("PATH_LIST size:"+map.get("PATH_LIST").size());
		for(int i=0; i<map.get("PATH_LIST").size(); i++){
			//retMap.put(map.get("PATH_LIST").get(i), "1.0");

			String realVer = "0";
			String dotVer = "0";
			String filepath = map.get("PATH_LIST").get(i);
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(filepath.substring(0,filepath.split("/")[0].length()));
			
			if(project.isAccessible()){
				if(EcamsRepositoryProvider.isManagedByEcams(project)){
					filepath = project.getLocation()+filepath.substring(filepath.split("/")[0].length());
					
					File filz = new File(filepath);
					
					if (filz.exists()){
						String decoUrl = filz.getParent() + "\\.deco";
						
						Document document = XmlReadWriter.getDocument("deco", decoUrl+"\\."+filz.getName()+".ecm-meta");
						//C:\SHC\iStudio\workspace\Education\.istudiometa\service\com\edu\chap05\.deco
						if(document != null){
							Element rootElement = document.getRootElement();
							
							if (rootElement != null){
								if (rootElement.elements("fileData").size()>0){
									Element findElement=null;
									boolean findflag = false;
									Iterator j;

									for (j = rootElement.elementIterator("fileData");j.hasNext();){
										findElement = (Element) j.next();
										if (findElement.attributeValue("filename").equals(filz.getName())){
											findflag = true;
											break;
										}			
									}		
									
									if (findflag){
										Attribute childAttribute = null;
										for (j = findElement.attributeIterator();j.hasNext();){
											childAttribute = (Attribute)j.next();
											String attributeName = childAttribute.getQualifiedName();
											if (attributeName.equals("realversion")){
												realVer = childAttribute.getValue();
												//break;
											}else if (attributeName.equals("dotversion")){
												dotVer = childAttribute.getValue();
												//break;
											}
											if(!"0".equals(realVer) && !"0".equals(dotVer)) break;
										}
									}
								}
							}
						}
					}
				}
			}
			logger.debug("filepath:Version="+map.get("PATH_LIST").get(i)+":"+realVer+"."+dotVer);
			retMap.put(map.get("PATH_LIST").get(i), realVer+"."+dotVer);
		}
		logger.error("Versioninfo    End");
		return retMap;
	}

}
