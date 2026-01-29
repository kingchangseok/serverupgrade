package com.azsoft.ecams.util.file;

import java.io.File;
import java.util.ArrayList;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class CreateSplitZipFileFromFolder {
	
	public String zipFiles(String zip_root, String zip_filename, ArrayList<String> findFiles) throws Exception {
		String zipFileName = zip_root+zip_filename+".zip";// 압축파일 경로+이름
	    
	    try {
	         ZipFile zipfile = new ZipFile(zipFileName);
	         ZipParameters parameters = new ZipParameters();
	         parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
	         parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
	         for(int i=0 ; i<findFiles.size() ; i++){
	             zipfile.addFile(new File(findFiles.get(i)), parameters);
	         }
	         zipfile.createZipFile(findFiles, parameters);
	     } catch (Exception e) {
	      // TODO: handle exception
	     }
	     
	     return zipFileName;
	}
}
