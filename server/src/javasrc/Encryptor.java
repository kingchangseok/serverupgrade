
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
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Encryptor 
{	
  	private static SecretKeySpec secretKeySpec;
	private static String skey = "GISDESKY";
  	
	private static BASE64Encoder encoder;
	private static BASE64Decoder decoder;
  	
	private static Encryptor instance;
  	
  	private Encryptor()
  	{ 
  		encoder = new BASE64Encoder();
  		decoder = new BASE64Decoder();		
  	}    
  	public static Encryptor instance()
  	{
  		if(instance == null)
  		{
  			instance = new Encryptor();	
  		}
  		return instance;
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
		
		return encoder.encode(encrypted);  	   		
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
		byte[] encrypted = decoder.decodeBuffer(str);//str.getBytes();
 
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
  	
  	public static void main(String[] args)
  	{
  		try
  		{
  			String password = "12345";
  			
  			String en = Encryptor.instance().strGetEncrypt(password);  			
  			
			String de = Encryptor.instance().strGetDecrypt(en);
  			
  			System.out.println(password);
			System.out.println(en);
  			System.out.println(de);
  			
  		}catch(Exception e)
  		{}
  	}
}
