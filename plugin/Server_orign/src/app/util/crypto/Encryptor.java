
/******************************************************************************/
/* File Name   : Encryptor.java                                               */
/* Author      : Yeo Sang Hoon                                                */
/* Date        : 2003/01/30                                                   */
/* Description : Encode/Decode                                                */
/*                                                                            */
/* Modification Log                                                           */
/* Ver No   Date        Author           Modification                         */
/*  1.0.0   2003/01/30  Yeo Sang Hoon    Initial Version                      */
/******************************************************************************/
package app.util.crypto;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

public class Encryptor 
{	
  	private static SecretKeySpec secretKeySpec;
	private static String skey = "GISDESKY";
	private static String skey_AES = "ecams_secret_pwd";
  	

	private static Encoder encoder;
	private static Decoder decoder;

	private static Encryptor instance;
  	
  	private Encryptor()
  	{ 
  		encoder = Base64.getEncoder();
  		decoder = Base64.getDecoder();
  	} 
  	
  	private SecretKeySpec getSecretKeySpec()
  	{
  		if(secretKeySpec == null)
  		{
			secretKeySpec = new SecretKeySpec(skey.getBytes(), "DES");
  		}
  		return secretKeySpec;
  	}
  	
    /**************************************************************************/
    /* Encode                                                                 */
    /**************************************************************************/
  	public String strGetEncrypt(String str) throws Exception
  	{
		Cipher cipher = Cipher.getInstance("DES"); 
		cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec()); 
		byte[] encrypted = cipher.doFinal(str.getBytes()); 
		
		
		
		return new String(Base64Coder.encode(encrypted));  	   		
  	}

	public byte[] byteGetEncrypt(String str) throws Exception
	{
		Cipher cipher = Cipher.getInstance("DES"); 
		cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec()); 
		return  cipher.doFinal(str.getBytes());		
	}  	
	
    /**************************************************************************/
    /* Decode                                                                 */
    /**************************************************************************/
	public String strGetDecrypt(String str) throws Exception
	{ 
		byte[] encrypted = Base64Coder.decode(str);//str.getBytes();
 
		Cipher cipher = Cipher.getInstance("DES"); 
		cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec()); 
		byte[] decrypted = cipher.doFinal(encrypted); 
 
		return new String(decrypted);
	}  	
	
  	public String strGetDecrypt(byte[] encrypted) throws Exception
  	{
		Cipher cipher = Cipher.getInstance("DES"); 
		cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec()); 
		byte[] decrypted = cipher.doFinal(encrypted); 

		return new String(decrypted);		
  	}
  	

	public String SHA256(String str){
		String SHA = "";
		try{
			MessageDigest sh = MessageDigest.getInstance("SHA-256");
			sh.update(str.getBytes());
			byte byteData[] = sh.digest();
			StringBuffer sb = new StringBuffer();
			
			for(int i = 0 ; i < byteData.length ; i++){
				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			SHA = sb.toString();
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace();
			SHA = null;
		}
		return SHA;
	}

  	public String strGetEncrypt_AES256(String str) throws NoSuchAlgorithmException,GeneralSecurityException,UnsupportedEncodingException
  	{
		String iv;
		Key keySpec;
		
		iv = skey_AES.substring(0,16);
  		byte[] keyBytes = new byte[16];
  		byte[] b = skey_AES.getBytes("UTF-8");
  		int len = b.length;
  		if (len > keyBytes.length) {
  			len = keyBytes.length;
  		}
  		System.arraycopy(b,0,keyBytes,0,len);
  		SecretKey key = new SecretKeySpec(keyBytes, "AES");
  		
  		keySpec = key;
  		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
		byte[] encrypted = cipher.doFinal(str.getBytes("UTF-8"));
		String enStr = new String(encoder.encode(encrypted));  			

        return enStr;
  	}
  	public String strGetDecrypt_AES256(String str) throws NoSuchAlgorithmException,GeneralSecurityException,UnsupportedEncodingException
	{  		
		String iv;
		Key keySpec;
		
		iv = skey_AES.substring(0,16);
  		byte[] keyBytes = new byte[16];
  		byte[] b = skey_AES.getBytes("UTF-8");
  		int len = b.length;
  		if (len > keyBytes.length) {
  			len = keyBytes.length;
  		}
  		System.arraycopy(b,0,keyBytes,0,len);
  		SecretKey key = new SecretKeySpec(keyBytes, "AES");
  		
  		keySpec = key;
  		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
		byte[] byteStr = decoder.decode(str.getBytes());			

        return new String(cipher.doFinal(byteStr),"UTF-8");
	}
}
