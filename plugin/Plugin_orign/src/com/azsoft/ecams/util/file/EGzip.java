package com.azsoft.ecams.util.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.core.resources.IResource;

import com.azsoft.ecams.ui.compare.Utilities;

public class EGzip {
	public static byte[] getDecompressedByte(byte[] data){
		
		GZIPInputStream gzipInputStream = null;
		ByteArrayInputStream bis = null;
		ByteArrayOutputStream bos = null;
		int len=0;
		byte[] rtbyte = null;
		byte[] buf = new byte[1024];
		
		try {
			bis = new ByteArrayInputStream(data);
			gzipInputStream = new GZIPInputStream(bis);
			bos = new ByteArrayOutputStream();
			
			while ((len = gzipInputStream.read(buf)) > 0) {
				bos.write(buf, 0, len);
			}
			
			gzipInputStream.close();
			bis.close();
			
			//String localCharset = Utilities.getCharset(tmpResource);
			
			rtbyte = bos.toByteArray();
			
			bos.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rtbyte = null;
		}
		finally{
			try {
				gzipInputStream.close();
				bis.close();
				bos.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				rtbyte = null;
			}
		}
		
		//return rtbyte;
		return rtbyte;
	}
	
	public static byte[] getFileByte(byte[] data, String charset){//, IResource tmpResource
		
		GZIPInputStream gzipInputStream = null;
		ByteArrayInputStream bis = null;
		ByteArrayOutputStream bos = null;
		int len=0;
		String rtbyte = "";
		byte[] buf = new byte[1024];
		
		try {
			bis = new ByteArrayInputStream(data);
			gzipInputStream = new GZIPInputStream(bis);
			bos = new ByteArrayOutputStream();
			
			while ((len = gzipInputStream.read(buf)) > 0) {
				bos.write(buf, 0, len);
			}
			
			gzipInputStream.close();
			bis.close();
			
			//String localCharset = Utilities.getCharset(tmpResource);
			
			rtbyte = bos.toString(charset);
			
			bos.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rtbyte = null;
		}
		finally{
			try {
				gzipInputStream.close();
				bis.close();
				bos.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				rtbyte = null;
			}
		}
		
		return rtbyte.getBytes();
	}

	public static byte[] getCompressedByte(byte[] data){
		GZIPOutputStream gzipOutputStream = null;
		ByteArrayInputStream bis = null;
		ByteArrayOutputStream bos = null;
		int len=0;
		byte[] rtbyte = null;
		byte[] buf = new byte[1024];
		
		try {
			bis = new ByteArrayInputStream(data);
			bos = new ByteArrayOutputStream();
			gzipOutputStream = new GZIPOutputStream(bos);
			
			
			while ((len = bis.read(buf)) > 0) {
				gzipOutputStream.write(buf, 0, len);
			}
			
			gzipOutputStream.close();
			bis.close();
			
			rtbyte = bos.toByteArray();
			
			bos.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rtbyte = null;
		}
		finally{
			try {
				gzipOutputStream.close();
				bis.close();
				bos.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				rtbyte = null;
			}
		}
		
		return rtbyte;
	}
}
