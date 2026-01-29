package com.azsoft.ecams.util.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileControl {
	public static void splitFile(String nFilePath, String nFileName, InputStream fi){

		try {
			int maxFileSize = 1024*1024*50;//50mb
			int readCnt = 0;
			int totCnt = 0;
			int fileIdx = 0;
			BufferedInputStream bfi = new BufferedInputStream(fi);
			byte[] readBuffer = new byte[maxFileSize];

			File nFile = new File(nFilePath + nFileName);
			FileOutputStream fo = new FileOutputStream(nFile);

			do {
				readCnt = bfi.read(readBuffer);
				if(readCnt == -1){
					break;
				}
				fo.write(readBuffer,0,readCnt);
				totCnt += readCnt;

				if(totCnt%maxFileSize==0){
					fo.flush();
					fo.close();
					File nfile = new File(nFilePath+ nFileName+(++fileIdx)+"._tmp");
					fo = new FileOutputStream(nfile);
				}
			} while (true);

			fi.close();
			fo.flush();
			fo.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("##########분할완료##########");
	}

	public static void combineFile(String oriFileName, String nFilePath) throws FileNotFoundException, IOException {
		File nFiles = new File(nFilePath);
		String[] files = nFiles.list();
		FileOutputStream nFo = new FileOutputStream(nFilePath+oriFileName);
		for(int i=0;i<files.length;i++){
			FileInputStream nFi = new FileInputStream(nFilePath+files[i]);
			byte[] buf = new byte[2048];
			int readCnt = 0;
			while((readCnt =  nFi.read(buf)) >-1){
				nFo.write(buf,0,readCnt);
			}
		}
		nFo.flush();
		nFo.close();
		System.out.println("##########합치기완료##########");
	}
}
