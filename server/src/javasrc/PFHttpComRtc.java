public class PFHttpComRtc{

	/**
	* @param args
	*/
	public static void main(String[] args) {
		
/*
 final String delimeter =":";

  String userId = "pangsha";                             //ÇÁ·ÎÇÁ·¹ÀӰèÁ¤
  String passWd = "12345";                            //ºñ¹йøȣ
  String physicalName = "dev_user_info_pf04";    //¹°¸®¸í

  String sendData =
   userId + delimeter +
   passWd + delimeter +
  physicalName + delimeter ;

0- type
1- ip
2- port
3- id
4- passwd
5- physicalName
*/
		
		// TODO Auto-generated method stub
		final String delimeter =":";
		String	sendData="";
		
		int	argsLen = args.length;
		
		int exitcode=0;
		
		
		
		for (int i=3 ; i < argsLen; i++ )
		{
			if( args[i].equals("NULL") )
			{
				sendData=sendData+" "+delimeter;
			}
			else
			{
				sendData=sendData+args[i]+delimeter;
			}
		}
		
		
		HttpCom httpCom = new HttpCom();
		byte [] receive ;
		try {
			
			
			String contextName ="pfmdevsvr/";
			
			receive = httpCom.process(args[1], args[2], contextName+args[0]+".TPDirect", sendData.getBytes());

			
			
			System.out.println("sendData=["+sendData+"]");
			System.out.println("httpCom.process("+args[1]+","+args[2]+","+contextName+args[0]+".TPDirect"+", sendData.getBytes())");
			
			
			System.out.println("receive Message=["+new String(receive)+"] receive length=["+receive.length+"]");
			
			
			if (receive.length == 0){
				System.err.println("Error receive Message=["+new String(receive)+"] receive length=["+receive.length+"]");
				exitcode = 1;
			}
			else{
				String[] rtArray =(new String(receive)).split(":");
				

				exitcode = Integer.parseInt(rtArray[0]);
			
						
				if (exitcode != 0){				
					System.err.println("Error Code=["+rtArray[0]+"] Error Message=["+rtArray[1]+"]");
				}
				else{
					System.out.println("httpCom.process Call Success. returnMessage=["+rtArray[1]+"]");
				}
			}
			
			
			

			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Exception Message=["+e.getMessage()+"]");
			exitcode = 1;
		} finally{
			System.exit(exitcode);
		}	
		
	}
}
