package com.azsoft.ecams.util.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;


public class XmlReadWriter {
	private static Logger logger = Logger.getLogger(XmlReadWriter.class);
	public static Document getDocument(String rootname,String file){
		Document doc = null;
		//boolean readflag=false;
		//do{
			try{
				File chfile = new File(file);
				if (chfile.exists()){
					SAXReader reader = new SAXReader();
					reader.setStripWhitespaceText(true);
					doc = reader.read(chfile);
				}
				else{
					chfile.createNewFile();
					doc = DocumentHelper.createDocument();
				    Element rootElement = new DOMElement(rootname);
				    doc.setRootElement(rootElement);
				    
				    XmlReadWriter.WriteXml(doc, file);
				}
				
				//readflag = true;
			} catch (IOException e) {
				logger.error("getDocument error:",e);
				//System.out.println("getDocument43:"+file);
				//readflag = false;
			} catch (DocumentException e) {
				logger.error("getDocument error:",e);
				//System.out.println("getDocument47:"+file);
				// TODO Auto-generated catch block
				//readflag = false;
			}
		//}while(!readflag);		

		return doc;
		
	}
	
	public static synchronized boolean WriteXml(Document doc,String file){
		boolean returnvalue=false;
		try{
			// 기본 포맷 형태를 불러와 수정한다.
			OutputFormat fm = OutputFormat.createPrettyPrint();
		   
		    // encoding 형태를 한글로 변경한다.
		    fm.setEncoding("utf-8");

		    // 부모 자식 태그를 구별하기 위한 탭 범위를 정한다.
		    fm.setIndent("\t");
		    fm.setIndentSize(4);
		    fm.setTrimText(true);
		    
		    File writeFile = new File(file);
		    
		    /*if (!writeFile.isHidden()){
				String hiddencommand = "ATTRIB.EXE +H \""+file+"\"";
				Runtime.getRuntime().exec(hiddencommand);
		    }*/
		    	    
		    
		    FileOutputStream fw = new FileOutputStream(writeFile);
		    
		    XMLWriter outp = new XMLWriter(fw,fm);
	    
		    outp.write(doc);
		    outp.close();
		    fw.close();
		    returnvalue=true;
		    
		}catch (IOException e) {
			logger.error("WriteXml error:",e);
			returnvalue=false;
		}finally{
		}
		return returnvalue;
	}
	
}
