public class PFHttpCom2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final String delimeter =":";
		String sendData="";

		HttpCom httpCom = new HttpCom();
        byte [] receive	;
		try {
			System.out.println("Call httpCom.process ="+args[0] + " " +args[1] +" "+args[2]);

		    receive = httpCom.process(args[0], args[1], args[2], sendData.getBytes());

			System.out.println("Call httpCom.process Args[1]="+args[1] + ", args[2]= " +args[2] + " " + args[0]);

			String str =new String(receive);

			System.out.println("# get11 : "+str);

			TextRW trw = new TextRW();
			trw.TextWrite(str, args[3]);

			//System.out.println("# get11 : "+new String(receive));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
