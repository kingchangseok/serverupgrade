public class PFHttpCom1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		char flag ='T';
		final String delimeter =":";
		String sendData="";
		String source="";
		int len=0;
		try{
			if (args[0].equals("UPDATE"))
			{
				TextRW tr =new TextRW();
				byte [] recieve =tr.TextRead(args[11]);
				source=new String(recieve);
				len=source.length();
			}
			int size=args.length;
			if (args[0].equals("UPDATE"))
			{
				size=size-1;
			}
			for (int i=4 ; i < size; i++ )
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

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        }

  System.out.println(sendData);

		if (args[0].equals("UPDATE"))
		{
			sendData=sendData+len+delimeter+source+delimeter;
		}
		HttpCom httpCom = new HttpCom();
        byte [] receive	;
		try {
			String contextName ="pfmdevsvr/";
			if (args[0].equals("UPDATE"))
			{
			    receive = httpCom.process(args[1], args[2], contextName+args[0]+".Direct", sendData.getBytes("UTF8"));
		    }
		    else
			{
			    receive = httpCom.process(args[1], args[2], contextName+args[0]+".Direct", sendData.getBytes());
		    }
			//byte [] receive = httpCom.process(args[1], args[2], contextName+args[0]+".Direct", sendData.getBytes("UTF8"));

			String str =new String(receive,"UTF8");
		    System.out.println("# get11 : "+new String(receive,"UTF-8"));
			TextRW trw = new TextRW();
			if(args[0].equals("DOWNLOAD") || args[0].equals("DOWNLOAD_MEMBER") )
			{
				String temp=str;
				int end=temp.lastIndexOf(":");
				int middle=0;
				temp=temp.substring(0,end);
				for(int i=0 ; i< 8 ; i++)  // ¼ҽº¸¸ ºи®.
				{
					middle=middle+temp.indexOf(":")+1;
					temp=temp.substring( temp.indexOf(":")+1 );
				}
				trw.TextWrite(str.substring(0,middle), args[3]);
				trw.TextWrite(temp, args[10]);
			}
			else
			{
				trw.TextWrite(str, args[3]);
			}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}
}
