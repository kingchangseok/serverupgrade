import java.io.File;
import java.util.Properties;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class aaa
{
	/**
	* @param args
	*/
	public static void main(String[] args) {
		String exitMsg = "";
		String strDBConn = "";
		String strDBUser = "";
		String strDBPass = "";
		
		SimpleDateFormat formatter = null;
		Date currentTime = null;
		String	nowDt= null;
		
		try {
			
			formatter = new SimpleDateFormat("yyyyMMddhhmmss",Locale.KOREA);
			currentTime = new Date();
			nowDt = formatter.format(currentTime);
			currentTime = null;
			formatter = null;
			
			System.err.println("    1    " + nowDt);
			
			String	sendData="";
			int	argsLen = args.length;			
			if (argsLen != 3) {
				exitMsg = "ERRParameter miss !";
			} else {
				if (!args[0].equals("D") && !args[0].equals("E")) {
					exitMsg = "ERRParameter miss! Encryption[E], Decryption[D]";
				}
			}
			System.err.println("    2    " + nowDt);
			
			if (exitMsg.length() == 0) {
			    File outFile = new File(args[2]);
			    if (outFile.isFile()) {
			    	outFile.delete();
				}
				System.err.println("    3    " + nowDt);
			    InputStream fip = null;
				String strName = args[1];
		    	Properties props = new Properties(); 
		    System.err.println("    4    " + nowDt);	
	        	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	            if( cl == null ){
	                cl = ClassLoader.getSystemClassLoader();
	            }
	            System.err.println("    5    " + nowDt);
	            fip = cl.getResourceAsStream(strName);
	            System.err.println("    6    " + nowDt);
				props.load(fip);
				System.err.println("    7    " + nowDt);
	        	strDBConn = props.getProperty("DBCONN");
	        	System.err.println("    8    " + nowDt);
	        	strDBUser = props.getProperty("DBUSER");
	        	System.err.println("    9    " + nowDt);
	        	strDBPass = props.getProperty("DBPASS");
	        	System.err.println("    10    " + nowDt);
				Encryptor oEncryptor = Encryptor.instance();
				System.err.println("    11    " + nowDt);
				sendData = "";
				if (args[0].equals("D")) {
			/*		sendData = oEncryptor.strGetDecrypt(strDBUser); */
					System.err.println("    12    " + nowDt);
				} else {
					sendData = oEncryptor.strGetEncrypt(strDBUser);
					System.err.println("    13    " + nowDt);
				}
				System.err.println("    14    " + nowDt);
				if (sendData.length() == 0) {
					exitMsg = "ERRDBUSER Decryt fail";
				} else {
					exitMsg = "OK"+sendData;
					sendData = "";
					if (args[0].equals("D")) {
						sendData = oEncryptor.strGetDecrypt(strDBPass);
						System.err.println("    15    " + nowDt);
					} else {
						sendData = oEncryptor.strGetEncrypt(strDBPass);
						System.err.println("    16    " + nowDt);
					}
					if (sendData.length() == 0) {
						exitMsg = "ERRDBPASS Decrypt Fail";
					} else {
						exitMsg = exitMsg + "/" + sendData;
						sendData = "";
						if (args[0].equals("D")) {
							sendData = oEncryptor.strGetDecrypt(strDBConn);
							System.err.println("    17    " + nowDt);
						} else {
							sendData = oEncryptor.strGetEncrypt(strDBConn);
							System.err.println("    18    " + nowDt);
						}
						if (sendData.length() == 0) {
							exitMsg = "ERRDBCONN Decrypt Fail";
						} else {
							exitMsg = exitMsg + "@" + sendData;							
						}
					}
				}
				
				System.err.println("    19    " + nowDt);
				TextRW trw = new TextRW();
				System.err.println("    20    " + nowDt);
				trw.TextWrite(exitMsg, args[2]);
				System.err.println("    21    " + nowDt);
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
