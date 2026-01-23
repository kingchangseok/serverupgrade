import java.io.File;
import java.util.Properties;
import java.io.InputStream;

public class eCAMSConf1{
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
	        	
				Encryptor oEncryptor = Encryptor.instance();
				
				sendData = "";
				if (args[0].equals("D")) {
					sendData = oEncryptor.strGetDecrypt(strDBUser);
				} else {
					sendData = oEncryptor.strGetEncrypt(strDBUser);
				}
				if (sendData.length() == 0) {
					exitMsg = "ERRDBUSER Decryt fail";
				} else {
					exitMsg = "OK"+sendData;
					sendData = "";
					if (args[0].equals("D")) {
						sendData = oEncryptor.strGetDecrypt(strDBPass);
					} else {
						sendData = oEncryptor.strGetEncrypt(strDBPass);
					}
					if (sendData.length() == 0) {
						exitMsg = "ERRDBPASS Decrypt Fail";
					} else {
						exitMsg = exitMsg + "/" + sendData;
						sendData = "";
						if (args[0].equals("D")) {
							sendData = oEncryptor.strGetDecrypt(strDBConn);
						} else {
							sendData = oEncryptor.strGetEncrypt(strDBConn);
						}
						if (sendData.length() == 0) {
							exitMsg = "ERRDBCONN Decrypt Fail";
						} else {
							exitMsg = exitMsg + "@" + sendData;							
						}
					}
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
