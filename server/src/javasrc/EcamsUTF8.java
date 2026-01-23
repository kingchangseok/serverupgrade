import java.io.*;

public class EcamsUTF8 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		char flag ='T';
		final String delimeter =":";
		String sendData="";
		String source="";
		String len="";
		String str;
		try{						
			File f = new File(args[0]);
     
    		InputStreamReader infile = new InputStreamReader(new FileInputStream(f),"UTF-8");	
    		
    		BufferedReader in = new BufferedReader(infile);
    		str = new String();
    		while((str = in.readLine()) != null) {
    			source += str + "\n";     			
    		}
    		in.close();
    		infile.close();
			
			len=Integer.toString(source.getBytes().length);
		
	        byte [] receive	= null;	
	        String temp = null;
	        
	       	//System.out.println("arg[0] :"+args[0]);
	       	
			TextRW trw = new TextRW();		

			trw.StrWrite(source, args[1]);


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
