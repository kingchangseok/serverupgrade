import java.io.File;
public class Encryptor_Call{
	/**
	* @param args
	*/
	
	public static void main(String[] args) {
		String exitMsg = "";
		try {
			
			String	sendData="";
			int	argsLen = args.length;			
			if (argsLen != 3) {
				exitMsg = "ERRÀÎÀڸ¦ Á¤ȮÇϰÔ ÀԷÂÇϼ¼¿ä !";
			} else {
				if (!args[0].equals("D") && !args[0].equals("E")) {
					exitMsg = "ERRÀÎÀڸ¦ Á¤ȮÇϰÔ ÀԷÂÇϼ¼¿ä! Encryption[E], Decryption[D]";
				}
			}
			
			if (exitMsg.length() == 0) {
			    File outFile = new File(args[2]);
			    if (outFile.isFile()) {
			    	outFile.delete();
				}
					
				Encryptor oEncryptor = Encryptor.instance();
				
				if (args[0].equals("D")) {
					sendData = oEncryptor.strGetDecrypt(args[1]);
				} else {
					sendData = oEncryptor.strGetEncrypt(args[1]);
				}
				exitMsg = "OK" + sendData;
				
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
