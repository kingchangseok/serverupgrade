package com.azsoft.ecams.core.status;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.w3c.dom.DOMException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;             // List�뺥깭濡�媛믪쓣 �댁쓣 ���ъ슜
import java.util.Set;
import java.util.HashMap;

import com.azsoft.ecams.core.EcamsProviderPlugin;
import com.azsoft.ecams.core.IEcamsStatus;
import com.azsoft.ecams.core.resource.EcamsRepositoryProvider;
import com.azsoft.ecams.core.resource.EcamsResourceStatus;
import com.azsoft.ecams.properties.IProperty;
import com.azsoft.ecams.util.checksum.CheckSum;
import com.azsoft.ecams.util.file.EFileToByteArray;
import com.azsoft.ecams.util.xml.XmlReadWriter;



/**
 * <PRE>
 * 1. ClassName	    : XmlStatusMgr
 * 2. FileName		: XmlStatusMgr.java
 * 3. Package		: com.azsoft.ecams.core.status
 * 4. Commnet		: xml parser
 * 5. �묒꽦��		: Administrator
 * 6. �묒꽦��		: 2010. 9. 28. �ㅼ쟾 10:00:09
 * </PRE>
 */
public class XmlStatusMgr{
	private Logger logger = Logger.getLogger(this.getClass());
	/**
	 * <PRE>
	 * 1. MethodName	: addStatus
	 * 2. ClassName		: XmlStatusMgr
	 * 3. Commnet		: resource紐��섍꺼諛쏆븘���곹깭媛믪쓣 .ecm(xml)���묒꽦�섍린
	 *                    Add a status for its resource (which does not need to exist)
	 * 4. �묒꽦��		: Administrator
	 * 5. �묒꽦��		: 2010. 9. 28. �ㅼ쟾 10:03:51
	 * </PRE>
	 * 		@param resource
	 * 		@param status
	 * 		@return
	 */

	public boolean updateStatus(IResource resource){
		IEcamsStatus fileStatus = getStatus(resource);
		
		if (fileStatus == null){
			return false;
		}
		
		if (!CheckSum.MD5SumVal(fileStatus.getFile()).equals(fileStatus.getMd5sum())){
			try {
				resource.getProject().setPersistentProperty(new QualifiedName("Properties", "ischanged"), "1");
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileStatus.setChanged(true);
		}
		else{
			try {
				resource.getProject().setPersistentProperty(new QualifiedName("Properties", "ischanged"), "0");
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileStatus.setChanged(false);
		}
		EcamsRepositoryProvider.setManagedByEcams(resource.getProject());
				    			
		return updateStatus((EcamsResourceStatus)fileStatus);		
	}
	
	public boolean updateStatus(EcamsResourceStatus status){
		try{
			boolean findflag=false;
			boolean statusChanged=false;
			boolean decostatus=false;
			Element findElement=null;
			boolean chgstatus=false;
			Iterator i;
			
			if (status == null) return false;
			//�좉퇋�뚯씪��.ecm�묒꽦���꾪빐 二쇱꽍泥섎━
			if (!status.getFile().exists()){
				return false;
			}

			if(status.getName().equals(status.getFile().getParent())) {
				System.out.println("updateStatus1:"+status.getName());
				System.out.println("updateStatus1:"+status.getFile().getParent());
				return false;
			}
			
			String decoUrl = status.getFile().getParent() + "\\.deco";

			String filepath = decoUrl;
			while(filepath.indexOf("/") >=0){
				filepath = filepath.replace("/","\\");
			}
			while(filepath.indexOf("\\\\") >=0){
				filepath = filepath.replace("\\\\", "\\");
			}
			while(filepath.indexOf("\\") >=0){
				filepath = filepath.replace("\\", "/");
			}

			File nfolder = new File(filepath);
			if (!nfolder.exists()) {
				nfolder.mkdir();
				if (!nfolder.isHidden()){
					String hiddencommand = "ATTRIB.EXE +H \""+nfolder+"\"";
					try {
						Runtime.getRuntime().exec(hiddencommand);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
			}
			
			Document document = XmlReadWriter.getDocument("deco", decoUrl+"\\."+status.getName()+".ecm-meta");

			if(document == null){
				return false;
			}
			
			Element rootElement = document.getRootElement();
	
			if (rootElement == null){
				return false;
			}
			
			for (i = rootElement.elementIterator("fileData");i.hasNext();){
				findElement = (Element) i.next();
				if (findElement.attributeValue("filename").equals(status.getName())){
					findflag = true;
					break;
				}			
			}
			
			
			if ( !findflag || findElement == null){
				return false;
			}
			
			if (!findElement.attributeValue("lastversion").equals(Integer.toString(status.getLastVer()))){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("tstversion").equals(Integer.toString(status.getTstVer()))){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("ecmMd5sum").equals(status.getMd5sum())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("ecmTstmd5sum").equals(status.getTstmd5sum())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("filepath").equals(status.getRelativitePath())){
				chgstatus = true;
			}			
			else if (!findElement.attributeValue("lastChangedDate").equals(Long.toString(((Date)status.getLastChangedDate()).getTime()))){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("editor").equals(status.getEditor())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("lastUser").equals(status.getLastUser())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("filestatus").equals(status.getFileStatus())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("islock").equals(status.isLocked()?"1":"0")){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("isAuthority").equals(status.isAuthority()?"1":"0")){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("ischanged").equals(status.isChanged()?"1":"0")){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("rsrccd").equals(status.getRsrccd())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("rsrccdcodename").equals(status.getRsrccodename())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("rsrcinfo").equals(status.getRsrcinfo())){
				
			}
			else if (!findElement.attributeValue("jobcd").equals(status.getJobcd())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("jobname").equals(status.getJobname())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("syscd").equals(status.getSyscd())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("sysmsg").equals(status.getSysmsg())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("srid").equals(status.getSRId())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("viewver").equals(status.getViewver())){
				chgstatus = true;
			}
			
			if (!chgstatus){
				return true;
			}
			
			//Element newAttribute = null;
			
			//findElement = new DOMElement("fileData");
			findElement.setAttributeValue("filename",status.getName());
			findElement.setAttributeValue("filepath",status.getRelativitePath());
			findElement.setAttributeValue("lastversion",Integer.toString(status.getLastVer()));
			findElement.setAttributeValue("tstversion",Integer.toString(status.getTstVer()));
			findElement.setAttributeValue("itemid",status.getItemid());
			findElement.setAttributeValue("ecmMd5sum",status.getMd5sum());
			findElement.setAttributeValue("ecmTstmd5sum",status.getTstmd5sum());
			findElement.setAttributeValue("lastChangedDate",Long.toString(((Date)status.getLastChangedDate()).getTime()));
			findElement.setAttributeValue("editor",status.getEditor());
			findElement.setAttributeValue("lastUser",status.getLastUser());
			findElement.setAttributeValue("filestatus",status.getFileStatus());
			
			if (status.isLocked()){
				findElement.setAttributeValue("islock","1");
			}
			else{
				findElement.setAttributeValue("islock","0");
			}
	
			if (status.isAuthority()){
				findElement.setAttributeValue("isAuthority","1");
			}
			else{
				findElement.setAttributeValue("isAuthority","0");
			}
			if (status.isChanged()){
				findElement.setAttributeValue("ischanged","1");
			}
			else{
				findElement.setAttributeValue("ischanged","0");
			}		
			findElement.setAttributeValue("rsrccd",status.getRsrccd());
			findElement.setAttributeValue("rsrccdcodename",status.getRsrccodename());
			findElement.setAttributeValue("rsrcinfo",status.getRsrcinfo());
			findElement.setAttributeValue("jobcd",status.getJobcd());
			findElement.setAttributeValue("jobname",status.getJobname());
			findElement.setAttributeValue("syscd",status.getSyscd());
			findElement.setAttributeValue("sysmsg",status.getSysmsg());
			findElement.setAttributeValue("srid",status.getSRId());
			findElement.setAttributeValue("viewver",status.getViewver());

			rootElement.setData(findElement);
		    document.setRootElement(rootElement);
			XmlReadWriter.WriteXml(document, decoUrl+"\\."+status.getName()+".ecm-meta"); //�쇰꺼�뺣낫�뚯씪 �앹꽦
		    
		    //xml�뚯씪留뚮뱾湲�
		    return true;
		    
		}catch (DOMException e){
			return false;
		}
	}
	
	public boolean new_updateStatus(EcamsResourceStatus status, IProject myproject, String gbn) {
		try{
			boolean findflag=false;
			Element findElement=null;
			boolean chgstatus=false;
			Iterator i;
			
			if (status == null) return false;
			if(status.getName().equals(myproject.getName())) {
				System.out.println("new_updateStatus1:"+status.getName());
				System.out.println("new_updateStatus2:"+myproject.getName());
				return false;
			}
			
			String decoUrl = status.getFile().getParent() + "\\.deco";

			String filepath = decoUrl;
			while(filepath.indexOf("/") >=0){
				filepath = filepath.replace("/","\\");
			}
			while(filepath.indexOf("\\\\") >=0){
				filepath = filepath.replace("\\\\", "\\");
			}
			while(filepath.indexOf("\\") >=0){
				filepath = filepath.replace("\\", "/");
			}

			File nfolder = new File(filepath);
			if (!nfolder.exists()) {
				nfolder.mkdir();
				if (!nfolder.isHidden()){
					String hiddencommand = "ATTRIB.EXE +H \""+nfolder+"\"";
					try {
						Runtime.getRuntime().exec(hiddencommand);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
			}
			
			nfolder = new File(decoUrl);
			if (!nfolder.exists()) {
				return false;
			}
			
			Document document = XmlReadWriter.getDocument("deco", decoUrl+"\\."+status.getName()+".ecm-meta");
			
			if(document == null){
				return false;
			}
			
			Element rootElement = document.getRootElement();
	
			if (rootElement == null){
				return false;
			}
			
			for (i = rootElement.elementIterator("fileData");i.hasNext();){
				findElement = (Element) i.next();
				if (findElement.attributeValue("filename").equals(status.getName())){
					findflag = true;
					break;
				}			
			}
			
			//if(gbn.equals("A")){
				if (status.getFile().exists()) {
					if (!CheckSum.MD5SumVal(status.getFile()).equals(status.getMd5sum())){
						status.setChanged(true);
					}
					else{
						status.setChanged(false);
					}
				} else{
					status.setChanged(false);
				}
			//}
			
			if ( !findflag || findElement == null){
				Element newAttribute = new DOMElement("fileData");
				newAttribute.addAttribute("filename",status.getName());
				newAttribute.addAttribute("filepath",status.getRelativitePath());
				if (status.isChanged()){
					newAttribute.addAttribute("ischanged","1");
				}else{
					newAttribute.addAttribute("ischanged","0");
				}
				newAttribute.addAttribute("lastversion",Integer.toString(status.getLastVer()));
				newAttribute.addAttribute("tstversion",Integer.toString(status.getTstVer()));
				newAttribute.addAttribute("itemid",status.getItemid());
				newAttribute.addAttribute("ecmMd5sum",status.getMd5sum());
				newAttribute.addAttribute("ecmTstmd5sum",status.getTstmd5sum());
				newAttribute.addAttribute("lastChangedDate",Long.toString(((Date)status.getLastChangedDate()).getTime()));
				newAttribute.addAttribute("editor",status.getEditor());
				newAttribute.addAttribute("lastUser",status.getLastUser());
				newAttribute.addAttribute("filestatus",status.getFileStatus());
				if (status.isLocked()){
					newAttribute.addAttribute("islock","1");
				}else{
					newAttribute.addAttribute("islock","0");
				}

				boolean tf = false;
				/*String jobcd = "";
				try {
					jobcd = myproject.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split(":")[0];
					if(status.getJobcd().equals(jobcd)) tf = true;
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				
				tf = false;
				try {
					String[] jobary;
					jobary = myproject.getPersistentProperty(new QualifiedName("Properties","setjobcd")).split("/");

					for (int j=0;j<jobary.length;j++){
						if(status.getJobcd().equals(jobary[j].split(":")[0])){
							tf=true;
							break;
						}
					}
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (tf){
					newAttribute.addAttribute("isAuthority","1");
				}
				else{
					newAttribute.addAttribute("isAuthority","0");
				}
				newAttribute.addAttribute("rsrccd",status.getRsrccd());
				newAttribute.addAttribute("rsrccdcodename",status.getRsrccodename());
				newAttribute.addAttribute("rsrcinfo",status.getRsrcinfo());
				newAttribute.addAttribute("jobcd",status.getJobcd());
				newAttribute.addAttribute("jobname",status.getJobname());
				newAttribute.addAttribute("syscd",status.getSyscd());
				newAttribute.addAttribute("sysmsg",status.getSysmsg());	
				newAttribute.addAttribute("srid",status.getSRId());
				newAttribute.addAttribute("viewver",status.getViewver());

				rootElement.add(newAttribute);
				document.setRootElement(rootElement);
				XmlReadWriter.WriteXml(document, decoUrl+"\\."+status.getName()+".ecm-meta"); //�쇰꺼�뺣낫�뚯씪 �앹꽦
			    return true;
			}
			
			if(gbn.equals("A")){
				if (!findElement.attributeValue("ischanged").equals(status.isChanged()?"1":"0")){
					chgstatus = true;
				}
			}
			/*if (!findElement.attributeValue("ischanged").equals(status.isChanged()?"1":"0")){
				chgstatus = true;
			}
			else*/ if (!findElement.attributeValue("lastversion").equals(Integer.toString(status.getLastVer()))){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("tstversion").equals(Integer.toString(status.getTstVer()))){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("ecmMd5sum").equals(status.getMd5sum())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("ecmTstmd5sum").equals(status.getTstmd5sum())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("filepath").equals(status.getRelativitePath())){
				chgstatus = true;
			}			
			else if (!findElement.attributeValue("lastChangedDate").equals(Long.toString(((Date)status.getLastChangedDate()).getTime()))){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("editor").equals(status.getEditor())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("lastUser").equals(status.getLastUser())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("filestatus").equals(status.getFileStatus())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("islock").equals(status.isLocked()?"1":"0")){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("isAuthority").equals(status.isAuthority()?"1":"0")){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("rsrccd").equals(status.getRsrccd())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("rsrccdcodename").equals(status.getRsrccodename())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("rsrcinfo").equals(status.getRsrcinfo())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("jobcd").equals(status.getJobcd())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("jobname").equals(status.getJobname())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("syscd").equals(status.getSyscd())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("sysmsg").equals(status.getSysmsg())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("srid").equals(status.getSRId())){
				chgstatus = true;
			}
			else if (!findElement.attributeValue("viewver").equals(status.getViewver())){
				chgstatus = true;
			}
			
			if (!chgstatus){
				return true;
			}
			
			findElement.setAttributeValue("filename",status.getName());
			findElement.setAttributeValue("filepath",status.getRelativitePath());

			if (status.isChanged()){
				findElement.setAttributeValue("ischanged","1");
			}
			else{
				findElement.setAttributeValue("ischanged","0");
			}
			findElement.setAttributeValue("lastversion",Integer.toString(status.getLastVer()));
			findElement.setAttributeValue("tstversion",Integer.toString(status.getTstVer()));
			findElement.setAttributeValue("itemid",status.getItemid());
			findElement.setAttributeValue("ecmMd5sum",status.getMd5sum());
			findElement.setAttributeValue("ecmTstmd5sum",status.getTstmd5sum());
			findElement.setAttributeValue("lastChangedDate",Long.toString(((Date)status.getLastChangedDate()).getTime()));
			findElement.setAttributeValue("editor",status.getEditor());
			findElement.setAttributeValue("lastUser",status.getLastUser());
			findElement.setAttributeValue("filestatus",status.getFileStatus());
			if (status.isLocked()){
				findElement.setAttributeValue("islock","1");
			}
			else{
				findElement.setAttributeValue("islock","0");
			}
	
			if (status.isAuthority()){
				findElement.setAttributeValue("isAuthority","1");
			}
			else{
				findElement.setAttributeValue("isAuthority","0");
			}		
			findElement.setAttributeValue("rsrccd",status.getRsrccd());
			findElement.setAttributeValue("rsrccdcodename",status.getRsrccodename());
			findElement.setAttributeValue("rsrcinfo",status.getRsrcinfo());
			findElement.setAttributeValue("jobcd",status.getJobcd());
			findElement.setAttributeValue("jobname",status.getJobname());
			findElement.setAttributeValue("syscd",status.getSyscd());
			findElement.setAttributeValue("sysmsg",status.getSysmsg());
			findElement.setAttributeValue("srid",status.getSRId());
			findElement.setAttributeValue("viewver",status.getViewver());
			
			rootElement.setData(findElement);
		    document.setRootElement(rootElement);
			XmlReadWriter.WriteXml(document, decoUrl+"\\."+status.getName()+".ecm-meta"); //�쇰꺼�뺣낫�뚯씪 �앹꽦
			
		    //xml�뚯씪留뚮뱾湲�
		    return true;
		    
		}catch (DOMException e){
			return false;
		}
	}
	
	/**
	 * <PRE>
	 * 1. MethodName	: getStatus
	 * 2. ClassName		: XmlStatusMgr
	 * 3. Commnet		: Get the status of the given resource (which does not need to exist)
	 * 4. �묒꽦��		: Administrator
	 * 5. �묒꽦��		: 2010. 9. 28. �ㅼ쟾 10:08:31
	 * </PRE>
	 * 		@param resource
	 * 		@return
	 */
	public IEcamsStatus getStatus(IResource resource) {
		boolean findflag;
		Iterator i;

		if (resource == null) return null;
		
		if (!(resource.exists())){
			return null;
		}

		if(resource.getName().equals(resource.getProject().getName())) {
			System.out.println("getStatus1:"+resource.getName());
			System.out.println("getStatus2:"+resource.getProject().getName());
			return null;
		}
		
		String decoUrl = resource.getLocation().toFile().getParent() + "\\.deco";
		
		String filepath = decoUrl;
		while(filepath.indexOf("/") >=0){
			filepath = filepath.replace("/","\\");
		}
		while(filepath.indexOf("\\\\") >=0){
			filepath = filepath.replace("\\\\", "\\");
		}
		while(filepath.indexOf("\\") >=0){
			filepath = filepath.replace("\\", "/");
		}
		
		File nfolder = new File(filepath);
		if(!nfolder.exists()){
			return null;
		}
		
		Document document = XmlReadWriter.getDocument("deco", decoUrl+"\\."+resource.getName()+".ecm-meta");

		if(document == null){
			return null;
		}
		
		Element rootElement = document.getRootElement();
		
		if (rootElement == null){
			return null;
		}
		
		if (rootElement.elements("fileData").size()==0){
			File delFile = new File(filepath+"/."+resource.getName());
			delFile.delete();
			return null;
		}

		
		Element findElement=null;
		
		findflag = false;
		
		for (i = rootElement.elementIterator("fileData");i.hasNext();){
			findElement = (Element) i.next();
			if (findElement.attributeValue("filename").equals(resource.getName())){
				findflag = true;
				break;
			}			
		}		
		
		Attribute childAttribute = null;
		EcamsResourceStatus fileStatus = null;
		
		if (findflag){
			if (!resource.getLocation().toFile().exists()){
				removeStatus(resource);
				return null;
			}
			
			fileStatus = new EcamsResourceStatus();
			for (i = findElement.attributeIterator();i.hasNext();){
				childAttribute = (Attribute)i.next();
				String attributeName = childAttribute.getQualifiedName();
				String attributeValue = childAttribute.getValue();
				
				if (attributeName.equals("filename")){
					fileStatus.setFile(resource.getLocation().toFile());
				}			
				else if (attributeName.equals("filepath")){
					fileStatus.setRelativitePath(attributeValue);
				}
				else if (attributeName.equals("ischanged")){
					if (Integer.parseInt(attributeValue) == 0){
						fileStatus.setChanged(false);
					}
					else{
						fileStatus.setChanged(true);
					}
				}
				else if (attributeName.equals("lastversion")){
					fileStatus.setLastVer(Integer.parseInt(attributeValue));
				}
				else if (attributeName.equals("tstversion")){
					fileStatus.setTstVer(Integer.parseInt(attributeValue));
				}
				else if (attributeName.equals("itemid")){
					fileStatus.setItemid(attributeValue);
				}
				else if (attributeName.equals("ecmMd5sum")){
					fileStatus.setMd5sum(attributeValue);
				}
				else if (attributeName.equals("ecmTstmd5sum")){
					fileStatus.setTstmd5sum(attributeValue);
				}
				else if (attributeName.equals("lastChangedDate")){
					fileStatus.setLastChangedDate(Long.parseLong(attributeValue));
				}
				else if (attributeName.equals("editor")){
					fileStatus.setEditor(attributeValue);
				}
				else if (attributeName.equals("lastUser")){
					fileStatus.setLastUser(attributeValue);
				}
				else if (attributeName.equals("filestatus")){
					fileStatus.setFilestatus(attributeValue);
				}
				else if (attributeName.equals("islock")){
					if (Integer.parseInt(attributeValue) == 0){
						fileStatus.setLock(false);
					}
					else{
						fileStatus.setLock(true);
					}
				}
				else if (attributeName.equals("isAuthority")){
					if (Integer.parseInt(attributeValue) == 0){
						fileStatus.setAuthority(false);
					}
					else{
						fileStatus.setAuthority(true);
					}
				}
				else if (attributeName.equals("rsrccd")){
					fileStatus.setRsrccd(attributeValue);
				}
				else if (attributeName.equals("rsrccdcodename")){
					fileStatus.setRsrccodename(attributeValue);
				}
				else if (attributeName.equals("rsrcinfo")){
					fileStatus.setRsrcinfo(attributeValue);
				}
				else if (attributeName.equals("jobcd")){
					fileStatus.setJobcd(attributeValue);
				}
				else if (attributeName.equals("jobname")){
					fileStatus.setJobname(attributeValue);
				}
				else if (attributeName.equals("syscd")){
					fileStatus.setSyscd(attributeValue);
				}
				else if (attributeName.equals("sysmsg")){
					fileStatus.setSysmsg(attributeValue);
				}else if (attributeName.equals("srid")){
					fileStatus.setSRId(attributeValue);
				}else if (attributeName.equals("viewver")){
					fileStatus.setViewver(attributeValue);
				}
				
			}
			return fileStatus;	
		}else{
			return null;
		}
	}
	
	public IEcamsStatus[] getStatuses(IResource[] resources){
		int resourcesSize = resources.length;
		List statusList = new ArrayList();
		
		for(int i=0;i<resourcesSize;i++){
			IEcamsStatus childStatus = getStatus(resources[i]);
			if (!childStatus.getFileStatus().equals("U")){
				statusList.add(childStatus);
			}
		}
		return (IEcamsStatus[])statusList.toArray(new IEcamsStatus[statusList.size()]);
	}

	
	public IEcamsStatus[] getStatuses_regist(IResource[] resources){
		int resourcesSize = resources.length;
		List statusList = new ArrayList();
		
		for(int i=0;i<resourcesSize;i++){
			IEcamsStatus childStatus = getStatus(resources[i]);
			if(childStatus != null){
				if (!childStatus.getFileStatus().equals("U")){
					statusList.add(childStatus);
				}
			}else{
				 childStatus = getStatus_regist(resources[i]);
				 statusList.add(childStatus);
			}
		}
		return (IEcamsStatus[])statusList.toArray(new IEcamsStatus[statusList.size()]);
	}
	
	public IEcamsStatus getStatus_regist(IResource resource) {
		if (resource == null) return null;
		
		if (!(resource.exists())){
			return null;
		}
		if (!resource.getLocation().toFile().exists()){
			removeStatus(resource);
			return null;
		}
		EcamsResourceStatus fileStatus = new EcamsResourceStatus();
		fileStatus.setFile(resource.getLocation().toFile());
		
		return fileStatus;	
	}
	
	public boolean deleteDeco(IResource resource) {
		boolean findflag;
		Iterator i;
		
		if (resource == null) return false;
		
		if (!(resource.exists() || resource.isPhantom())){
			return false;
		}
		if(resource.getName().equals(resource.getProject().getName())) {
			System.out.println("removeStatus1:"+resource.getName());
			System.out.println("removeStatus2:"+resource.getProject().getName());
			return false;
		}
		
		String fileUrl = null;
		if (resource instanceof IFile){
			fileUrl = resource.getParent().getLocation() + "\\.deco";
		}else{
			fileUrl = resource.getLocation() + "\\.deco";
		}		

		File decoFile = new File(fileUrl+"\\."+resource.getName()+".ecm-meta");
		//System.out.println("+++++1[XmlStatusMgr]ecm-meta delete+++++"+decoFile);
		if (decoFile.exists()) {
			//System.out.println("+++++2[XmlStatusMgr]ecm-meta delete+++++"+decoFile);
			decoFile.delete();
			
		}
		decoFile = null;
		return true;
	}
	
	public boolean removeStatus(IResource resource) {
		boolean findflag;
		Iterator i;
		
		if (resource == null) return false;
		
		if (!(resource.exists() || resource.isPhantom())){
			return false;
		}

		if(resource.getName().equals(resource.getProject().getName())) {
			System.out.println("removeStatus1:"+resource.getName());
			System.out.println("removeStatus2:"+resource.getProject().getName());
			return false;
		}
		
		String fileUrl = null;
		if (resource instanceof IFile){
			fileUrl = resource.getParent().getLocation() + "\\.deco";
		}else{
			fileUrl = resource.getLocation() + "\\.deco";
		}		

		Document document = XmlReadWriter.getDocument("deco", fileUrl+"\\."+resource.getName()+".ecm-meta");

		if(document == null){
			return false;
		}
		
		Element rootElement = document.getRootElement();

		if (rootElement == null){
			return false;
		}

		Element findElement=null;
		
		findflag = false;
		
		for (i = rootElement.elementIterator("fileData");i.hasNext();){
			findElement = (Element) i.next();
			if (findElement.attributeValue("filename").equals(resource.getName())){
				findflag = true;
				break;
			}			
		}
		
		if (!findflag){
			return false;
		}
		
		rootElement.remove(findElement);
		
	    document.setRootElement(rootElement);
		
	    //xml�뚯씪留뚮뱾湲�    
		
		return XmlReadWriter.WriteXml(document, fileUrl+"\\."+resource.getName()+".ecm-meta");
	}
	
public String getChanged(EcamsResourceStatus status, IProject myproject) {
		
		boolean findflag=false;
		Element findElement=null;
		boolean chgstatus=false;
		Iterator i;
		
		if (status == null) return null;
		
		if("9".equals(status.getFileStatus().split(":")[1])){
			return "9";
		}

		if(status.getName().equals(myproject.getName())) {
			return null;
		}
		
		String decoUrl = status.getFile().getParent() + "\\.deco";

		String filepath = decoUrl;
		while(filepath.indexOf("/") >=0){
			filepath = filepath.replace("/","\\");
		}
		while(filepath.indexOf("\\\\") >=0){
			filepath = filepath.replace("\\\\", "\\");
		}
		while(filepath.indexOf("\\") >=0){
			filepath = filepath.replace("\\", "/");
		}

		File nfolder = new File(filepath);
		if (!nfolder.exists()) {
			nfolder.mkdir();
			if (!nfolder.isHidden()){
				String hiddencommand = "ATTRIB.EXE +H \""+nfolder+"\"";
				try {
					Runtime.getRuntime().exec(hiddencommand);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		}
		if(filepath.indexOf("classes/") > 0){
			return null;
		}
		Document document = XmlReadWriter.getDocument("deco", decoUrl+"\\."+status.getName()+".ecm-meta");
		
		if(document == null){
			return null;
		}
		
		Element rootElement = document.getRootElement();

		if (rootElement == null){
			return null;
		}
		
		for (i = rootElement.elementIterator("fileData");i.hasNext();){
			findElement = (Element) i.next();
			if (findElement.attributeValue("filename").equals(status.getName())){
				findflag = true;
				break;
			}			
		}
		
		if (status.getFile().exists()) {
			boolean chkMD5 = false;
			/*2016.07.13
			if(status.getFileStatus().split(":")[1].equals("3")){//신규
				chkMD5 = true;
			}else if(status.getFileStatus().split(":")[1].equals("0") && !CheckSum.MD5SumVal(status.getFile()).equals(status.getMd5sum())){
				chkMD5 = true;
			}else if(!status.getFileStatus().split(":")[1].equals("0") && !CheckSum.MD5SumVal(status.getFile()).equals(status.getTstmd5sum())){
				chkMD5 = true;
			}
			*/
			if(status.getFileStatus().split(":")[1].equals("3")){//신규
				chkMD5 = true;
			}else if(!CheckSum.MD5SumVal(status.getFile()).equals(status.getMd5sum())){
				chkMD5 = true;
			}
			
			if (chkMD5){
				status.setChanged(true);
			}
			else{
				status.setChanged(false);
			}
			
		} else{
			status.setChanged(false);
		}
		
		if ( !findflag || findElement == null){
			return "0";
		}

		if (!findElement.attributeValue("ischanged").equals(status.isChanged()?"1":"0")){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("lastversion").equals(Integer.toString(status.getLastVer()))){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("tstversion").equals(Integer.toString(status.getTstVer()))){
			chgstatus = true;
		}
		else if (null != findElement.attributeValue("viewver") && !status.getViewver().equals(findElement.attributeValue("viewver"))){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("ecmMd5sum").equals(status.getMd5sum())){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("ecmTstmd5sum").equals(status.getTstmd5sum())){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("filepath").equals(status.getRelativitePath())){
			chgstatus = true;
		}			
		else if (!findElement.attributeValue("lastChangedDate").equals(Long.toString(((Date)status.getLastChangedDate()).getTime()))){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("editor").equals(status.getEditor())){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("lastUser").equals(status.getLastUser())){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("filestatus").equals(status.getFileStatus())){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("islock").equals(status.isLocked()?"1":"0")){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("isAuthority").equals(status.isAuthority()?"1":"0")){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("rsrccd").equals(status.getRsrccd())){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("rsrccdcodename").equals(status.getRsrccodename())){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("rsrcinfo").equals(status.getRsrcinfo())){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("jobcd").equals(status.getJobcd())){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("jobname").equals(status.getJobname())){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("syscd").equals(status.getSyscd())){
			chgstatus = true;
		}
		else if (!findElement.attributeValue("sysmsg").equals(status.getSysmsg())){
			chgstatus = true;
		}
		
		if(!chgstatus){
			return null;
		}else{
			return "0";
		}
	}
}
