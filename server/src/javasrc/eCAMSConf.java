import java.io.File;
import java.util.Properties;
import java.io.InputStream;
import java.math.BigInteger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class eCAMSConf{
	/**
	* @param args
	*/
	public static void main(String[] args) {
		String exitMsg = "";
		String strDBConn = "";
		String strDBUser = "";
		String strDBPass = "";
		try {
			
			String	sendData="";
			int	argsLen = args.length;			
			if (argsLen != 3) {
				exitMsg = "ERRParameter miss !";
			} else {
				if (!args[0].equals("D") && !args[0].equals("E")) {
					exitMsg = "ERRParameter miss! Encryption[E], Decryption[D]";
				}
			}
			
			if (exitMsg.length() == 0) {
			    File outFile = new File(args[2]);
			    if (outFile.isFile()) {
			    	outFile.delete();
				}
			    InputStream fip = null;
				String strName = args[1];
		    	Properties props = new Properties(); 
		    	
	        	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	            if( cl == null ){
	                cl = ClassLoader.getSystemClassLoader();
	            }
	            fip = cl.getResourceAsStream(strName);
				props.load(fip);
	        	strDBConn = props.getProperty("DBCONN");
	        	strDBUser = props.getProperty("DBUSER");
	        	strDBPass = props.getProperty("DBPASS");
	        	
	        	if (null != props.getProperty("SECU") && "true".equals(props.getProperty("SECU"))) {
	        	
					//Encryptor oEncryptor = Encryptor.instance();
					String skey = "ecams_secret_pwd";
					byte[] secretKey = skey.getBytes();
					byte[] iv = skey.getBytes();
					
					SecretKey key = new SecretKeySpec(secretKey, "AES");
					Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
					if (args[0].equals("D")) {
						cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
					} else {
						cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
					}
					sendData = "";
					if (args[0].equals("D")) {
						//sendData = oEncryptor.strGetDecrypt(strDBUser);
						sendData = new String((byte[])cipher.doFinal(new BigInteger(strDBUser,16).toByteArray()));
					} else {
						//sendData = oEncryptor.strGetEncrypt(strDBUser);
						sendData = new BigInteger((byte[])cipher.doFinal(strDBUser.getBytes())).toString(16);
					}
					if (sendData.length() == 0) {
						exitMsg = "ERRDBUSER Decryt fail";
					} else {
						exitMsg = "OK"+sendData;
						sendData = "";
						if (args[0].equals("D")) {
							//sendData = oEncryptor.strGetDecrypt(strDBPass);
							sendData = new String((byte[])cipher.doFinal(new BigInteger(strDBPass,16).toByteArray()));
						} else {
							//sendData = oEncryptor.strGetEncrypt(strDBPass);
							sendData = new BigInteger((byte[])cipher.doFinal(strDBPass.getBytes())).toString(16);
						}
						if (sendData.length() == 0) {
							exitMsg = "ERRDBPASS Decrypt Fail";
						} else {
							exitMsg = exitMsg + "/" + sendData;
							sendData = "";
							if (args[0].equals("D")) {
								//sendData = oEncryptor.strGetDecrypt(strDBConn);
								sendData = new String((byte[])cipher.doFinal(new BigInteger(strDBConn,16).toByteArray()));
							} else {
								//sendData = oEncryptor.strGetEncrypt(strDBConn);
								sendData = new BigInteger((byte[])cipher.doFinal(strDBConn.getBytes())).toString(16);
							}
							if (sendData.length() == 0) {
								exitMsg = "ERRDBCONN Decrypt Fail";
							} else {
								exitMsg = exitMsg + "@" + sendData;
							}
						}
					}
					cipher = null;
					key = null;
					iv =  null;
					secretKey = null;
					skey = null;
					
				} else {
					exitMsg = "OK"+strDBUser + "/" + strDBPass + "@" + strDBConn;
				}
				
				
				TextRW trw = new TextRW();
				trw.TextWrite(exitMsg, args[2]);
				System.exit(0);	    
			} else {
				System.exit(1);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Exception Message=["+e.getMessage()+"]");
			System.exit(1);
		} finally{			
			System.out.println(exitMsg);
			System.exit(1);
			//System.exit(exitMsg);
		}	
	}
	
}
