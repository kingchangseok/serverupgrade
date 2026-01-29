package com.azsoft.ecams.core.resource;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.osgi.framework.Bundle;

import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.util.xml.XmlReadWriter;

public class EcamsRepositoryProvider {

	public EcamsRepositoryProvider(){		
		
	}
	/*
	public static IProject findProject(String syscd,String jobcd){
		boolean findflag=false;
		Iterator i;
		int j,k;
        if (syscd == null){
        	return null;
        }
        
        if (syscd.equals("")){
        	return null;
        }
        
        if (jobcd == null){
        	return null;
        }
        
        if (jobcd.equals("")){
        	return null;
        }
		Bundle bundle = Platform.getBundle("com.azsoft.ecams.core");

		IPath path = Platform.getStateLocation(bundle);
		
		String fileUrl = path.toString()+"/.ecamsprojects";
		
		
		Document doc = XmlReadWriter.getDocument("ecamsprojects",fileUrl);
		if(doc == null) {
			return null;
		}
		Element rootElement = doc.getRootElement();
		if(rootElement == null){
			return null;
		}
		
		Element findElement=null;
		
		findflag = false;
		
		for (i = rootElement.elementIterator("ProjectInfo");i.hasNext();){
			findElement = (Element) i.next();
			if (findElement.attributeValue("syscd").equals(syscd)){
				if (findElement.attributeValue("setjobcd").split(":")[0].equals(jobcd)){
					findflag = true;
				}
				if (findflag){
					break;
				}
			}
		}
		
		if (findflag){
			return ResourcesPlugin.getWorkspace().getRoot().getProject(findElement.attributeValue("ProjectNM"));
		}
		else{
			return null;
		}
	}*/
	
	
	public static IProject findProject(String syscd){
		boolean findflag=false;
		Iterator i;
        if (syscd == null){
        	return null;
        }
        
        if (syscd.equals("")){
        	return null;
        }
        
		Bundle bundle = Platform.getBundle("com.azsoft.ecams.core");

		IPath path = Platform.getStateLocation(bundle);
		
		String fileUrl = path.toString()+"/.ecamsprojects";
		
		
		Document doc = XmlReadWriter.getDocument("ecamsprojects",fileUrl);
		if(doc == null) {
			return null;
		}
		Element rootElement = doc.getRootElement();
		if(rootElement == null){
			return null;
		}
		
		Element findElement=null;
		
		findflag = false;
		
		for (i = rootElement.elementIterator("ProjectInfo");i.hasNext();){
			findElement = (Element) i.next();
			if (findElement.attributeValue("syscd").equals(syscd)){
				findflag = true;
				break;
			}
		}
		
		if (findflag){
			return ResourcesPlugin.getWorkspace().getRoot().getProject(findElement.attributeValue("ProjectNM"));
		}
		else{
			return null;
		}
	}

	public static boolean isManagedByEcams(IProject project) {
		boolean findflag=false;
		Iterator i;
		
        if (project == null)
                return false;
        
		Bundle bundle = Platform.getBundle("com.azsoft.ecams.core");

		IPath path = Platform.getStateLocation(bundle);
		
		String fileUrl = path.toString()+"/.ecamsprojects";
		
		
		Document doc = XmlReadWriter.getDocument("ecamsprojects",fileUrl);
		if(doc == null) {
			return false;
		}
		Element rootElement = doc.getRootElement();
		if(rootElement == null){
			return false;
		}
		
		Element findElement;
		
		findflag = false;
		
		for (i = rootElement.elementIterator("ProjectInfo");i.hasNext();){
			findElement = (Element) i.next();
			if (findElement.attributeValue("ProjectNM").equals(project.getName())){
				findflag = true;
				break;
			}
		}		
    		
	    return findflag;
	}
	
	public static void setManagedByEcams(IProject project){
		boolean findflag=false;
		Iterator i;
		
        if (project == null){
        	return;
        }
        
    	
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

		Element findElement = null;
		
		findflag = false;
		
		String syscd=null;
		String setrsrccd=null;
		String setjobcd=null;
		String setanalyn=null;
		String ischanged=null;
		String sysinfo=null;
		try {
			syscd = project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[0];
			setrsrccd= project.getPersistentProperty(new QualifiedName("Properties","setrsrccd"));
			setjobcd= project.getPersistentProperty(new QualifiedName("Properties","setjobcd"));
			setanalyn= project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[2];
			ischanged= project.getPersistentProperty(new QualifiedName("Properties","ischanged"));
			sysinfo = project.getPersistentProperty(new QualifiedName("Properties","syscd")).split(":")[4];
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			syscd = "";
		}
		
		for (i = rootElement.elementIterator("ProjectInfo");i.hasNext();){
			findElement = (Element) i.next();
			if (!"".equals(syscd)){
				if(findElement.attributeValue("syscd").equals(syscd)){
					if (null == findElement.attributeValue("setjobcd")) {
						findflag = true;
						break;
					} else {
						if (findElement.attributeValue("setjobcd").split(":")[0].equals(setjobcd.split(":")[0])){
							findflag = true;
							break;
						}
					}
				}
			}else if (findElement.attributeValue("ProjectNM").equals(project.getName())){
				findflag = true;
				break;
			}
		}
		
	    if (!findflag){
			Element newAttribute = new DOMElement("ProjectInfo");
			newAttribute.addAttribute("ProjectNM",project.getName());
			
			newAttribute.addAttribute("setrsrccd",setrsrccd);
			newAttribute.addAttribute("syscd",syscd);
			newAttribute.addAttribute("setjobcd",setjobcd);
			newAttribute.addAttribute("setanalyn",setanalyn);
			newAttribute.addAttribute("ischanged",ischanged);
			newAttribute.addAttribute("sysinfo",sysinfo);
			
	    	rootElement.add(newAttribute);
		    doc.setRootElement(rootElement);
		    XmlReadWriter.WriteXml(doc, fileUrl);
	    }
	    else{
	    	Element newAttribute = new DOMElement("ProjectInfo");
			newAttribute.addAttribute("ProjectNM",project.getName());
			
			newAttribute.addAttribute("setrsrccd",setrsrccd);
			newAttribute.addAttribute("syscd",syscd);
			newAttribute.addAttribute("setjobcd",setjobcd);
			newAttribute.addAttribute("setanalyn",setanalyn);
			newAttribute.addAttribute("ischanged",ischanged);
			newAttribute.addAttribute("sysinfo",sysinfo);
			
			rootElement.remove(findElement);
	    	rootElement.add(newAttribute);
		    doc.setRootElement(rootElement);
		    XmlReadWriter.WriteXml(doc, fileUrl);
	    }
	}
	
	public static void unsetManagedByEcams(IProject project){
		boolean findflag=false;
		Iterator i;
		
        if (project == null) return;
        
	
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
		
		Element findElement;		
		
		findflag = false;
		
		for (i = rootElement.elementIterator("ProjectInfo");i.hasNext();){
			findElement = (Element) i.next();
			if (findElement.attributeValue("ProjectNM").equals(project.getName())){
				rootElement.remove(findElement);
				findflag = true;
				break;
			}
		}
				
	    if (findflag == true){
	    	doc.setRootElement(rootElement);
	    	XmlReadWriter.WriteXml(doc, fileUrl);
	    }
	}
	
	
	public static IResource getResourceFor(IResource parent, IPath location) {
		if (parent == null || location == null) {
			return null;
		}

		if (parent instanceof IWorkspaceRoot) {
			return null;
		}

		if (!isManagedByEcams(parent.getProject())) {
			return null;
		}

		if (!parent.getLocation().isPrefixOf(location)) {
			return null;
		}

		int segmentsToRemove = parent.getLocation().segmentCount();
		IPath fullPath = parent.getFullPath().append(location.removeFirstSegments(segmentsToRemove));

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IResource resource = root.findMember(fullPath);

		if (resource != null) {
			return resource;
		}

		if (parent instanceof IFile) {
			return null;
		}

		if (fullPath.isRoot()) {
			return root;
		} else if (fullPath.segmentCount() == 1) {
			return root.getProject(fullPath.segment(0));
		}

		if (!location.toFile().exists()) {
			if (location.toFile().getName().indexOf(".") == -1) {
				return root.getFolder(fullPath);
			}
		}

		if (location.toFile().isDirectory()) {
			return root.getFolder(fullPath);
		}

		return root.getFile(fullPath);
	}

	public static IResource getResourceFor(IResource parent, IEcamsStatus status) {
		if (status == null || status.getFile() == null) {
			return null;
		}
		return getResourceFor(parent, new Path(status.getFile().getAbsolutePath()));
	}

	public static IResource[] getResourcesFor(IPath location) {
		return getResourcesFor(location, true);
	}

	public static IResource[] getResourcesFor(IPath location, boolean includeProjects) {
		Set resources = new LinkedHashSet();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = root.getProjects();
		for (int i = 0; i < projects.length; i++) {
			IResource resource = getResourceFor(projects[i], location);
			if (resource != null) {
				resources.add(resource);
			}
			if (includeProjects && isManagedByEcams(projects[i]) && location.isPrefixOf(projects[i].getLocation())) {
				resources.add(projects[i]);
			}
		}
		return (IResource[]) resources.toArray(new IResource[resources.size()]);
	}
	
	public static boolean isLinkedResource(IResource resource) {
		return resource.isLinked(IResource.CHECK_ANCESTORS);

	}
		
}
