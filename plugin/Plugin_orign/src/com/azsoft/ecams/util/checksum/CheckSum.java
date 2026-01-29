package com.azsoft.ecams.util.checksum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public final class CheckSum {
	
	public static String MD5SumVal(String fileName){
		String returnMd5sum = null;
		byte[] buffer = new byte[8192];
		byte[] md5sum;
		BigInteger bigInt;
		int read = 0;
		MessageDigest digest;
		InputStream is = null;
		
		try{
			digest = MessageDigest.getInstance("MD5");
			is = new FileInputStream(new File(fileName));
			
			read = 0;
			while( (read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			
			md5sum = digest.digest();
			
			bigInt = new BigInteger(1, md5sum);
			returnMd5sum = bigInt.toString(16);
			while(returnMd5sum.length()<32){
				returnMd5sum = "0"+returnMd5sum;
			}
			//System.out.println("MD5: " + returnMd5sum);			
		}
		catch (NoSuchAlgorithmException e){
			returnMd5sum = null;			
		}
		catch (FileNotFoundException e){
			returnMd5sum = null;
		}
		catch (IOException e){
			returnMd5sum = null;
		}
		catch (Exception e){
			returnMd5sum = null;
		}
		finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return returnMd5sum;
	}

	public static String MD5SumVal(File file){
		String returnMd5sum = null;
		byte[] buffer = new byte[8192];
		byte[] md5sum;
		BigInteger bigInt;
		int read = 0;
		MessageDigest digest;
		InputStream is = null;
		
		try{
			digest = MessageDigest.getInstance("MD5");
			is = new FileInputStream(file);
			
			read = 0;
			while( (read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			
			md5sum = digest.digest();
			
			bigInt = new BigInteger(1, md5sum);
			returnMd5sum = bigInt.toString(16);
			while(returnMd5sum.length()<32){
				returnMd5sum = "0"+returnMd5sum;
			}
			//System.out.println("MD5: " + returnMd5sum);			
		}
		catch (NoSuchAlgorithmException e){
			returnMd5sum = null;			
		}
		catch (FileNotFoundException e){
			returnMd5sum = null;
		}
		catch (IOException e){
			returnMd5sum = null;
		}
		catch (Exception e){
			returnMd5sum = null;
		}
		finally {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return returnMd5sum;
	}

	public static String MD5SumVal(byte[] filebytes){
		String returnMd5sum = null; //test
		byte[] md5sum;
		BigInteger bigInt;
		MessageDigest digest;
		//InputStream is = null;
		
		try{
			digest = MessageDigest.getInstance("MD5");
			
			digest.update(filebytes, 0, filebytes.length);
			
			md5sum = digest.digest();
			
			bigInt = new BigInteger(1, md5sum);
			returnMd5sum = bigInt.toString(16);
			while(returnMd5sum.length()<32){
				returnMd5sum = "0"+returnMd5sum;
			}
			//System.out.println("MD5: " + returnMd5sum);			
		}
		catch (NoSuchAlgorithmException e){
			returnMd5sum = null;			
		}
		catch (Exception e){
			returnMd5sum = null;
		}
		finally {
//			try {
//				//is.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		return returnMd5sum;
	}
	
}
