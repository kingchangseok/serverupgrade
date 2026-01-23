public class PFHttpCom {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final String delimeter =":";
		String sendData="";

		try{
			int	size = args.length;

			if ( args[0].equals("PUBLISH_APPROVAL")){

				for (int i=3 ; i < size-1; i++ )
				{
					if (i == 5){
						if( args[i].equals("NULL") )
						{
							sendData=sendData+"N"+delimeter+" "+delimeter+"ECAMS Publish"+delimeter;
						}
						else{
							sendData=sendData+"N"+delimeter+args[i]+delimeter+"ECAMS Publish"+delimeter;
						}
					}
					if( args[i].equals("NULL") )
					{
						sendData=sendData+" "+delimeter;
					}
					else
					{
						sendData=sendData+args[i]+delimeter;
					}
				}
			}
			else
			if ( args[0].equals("CRUD_LIST")){

				for (int i=3 ; i < size-1; i++ )
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
			}
			else
			if ( args[0].equals("LOAD_XML")){

				for (int i=3 ; i < size-1; i++ )
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
			}
			else
			if ( args[0].equals("PFM_TABLE_DATA_LIST")){

				for (int i=3 ; i < size-1; i++ )
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
			}
			else {
				for (int i=3 ; i < size-1; i++ )
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
			}
			//System.out.println(sendData);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			TextRW trw = new TextRW();
			trw.TextWrite(e.getMessage(), args[6]);
			System.exit(1);	
        }

		HttpCom httpCom = new HttpCom();
        byte [] receive	;
		try {
			String contextName ="pfmdevsvr/";

			//System.out.println("Call httpCom.process Args[1]="+args[1] + ", args[2]= " +args[2] +" ,contextName+args[0].TPDirect="+contextName+args[0]+".TPDirect" + " ,sendData= "+sendData);

		    receive = httpCom.process(args[1], args[2], contextName+args[0]+".TPDirect", sendData.getBytes());

			System.out.println("Call httpCom.process Args[1]="+args[1] + ", args[2]= " +args[2] +" ,contextName+args[0].TPDirect="+contextName+args[0]+".TPDirect" + "  ,SEND LENGTH=" + sendData.getBytes().length +" ,sendData= "+sendData);

			String str =new String(receive);

			System.out.println("# get11 : "+str);

			TextRW trw = new TextRW();
			trw.TextWrite(str, args[6]);
			//System.out.println("# get11 : "+new String(receive));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			TextRW trw = new TextRW();
			trw.TextWrite(e.getMessage(), args[6]);
			System.exit(1);
		}
		System.exit(0);
	}
}
