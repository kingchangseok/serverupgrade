
import java.io.*;
import java.util.*;

public class TextRW
{
	public TextRW()
	{
	}

	public void TextWrite(String str, String filename) 
	{
		String temp=filename;
		try{
			File file=null;
			file = new File(temp);   
			if( !(file.isFile()) )    
			{
				file.createNewFile();     
			}
			//FileOutputStream fos= new FileOutputStream(file);   
			//fos.write(str.getBytes());            
			//fos.close();                           

			OutputStreamWriter writer = new OutputStreamWriter( new FileOutputStream(filename)); 
			writer.write(str);
			writer.close();

		}catch(IOException e){       
			System.out.println(e);
		}
	}
	
	public byte[] TextRead(String filename)
	{
		ByteArrayOutputStream baosRetStream = new ByteArrayOutputStream();
		String url=filename;
		try
		{
			File file=null;
			file = new File(url);
			byte[] byteTmpBuf = new byte[8192];
			FileInputStream fis = new FileInputStream(file); 

			int i;
			String str="";
			while( (i=fis.read(byteTmpBuf)) > -1 )
			{ 
				baosRetStream.write(byteTmpBuf, 0, i);
			} 
			fis.close();
		}
		catch(IOException e)
		{    
			System.out.println(e);
		}
		return baosRetStream.toByteArray();
	}
	
	public void StrWrite(String str, String filename)  
	{
		String temp=filename;
		try{
			//System.out.println("result1");

			File file=null;
			file = new File(temp);             
			if( !(file.isFile()) )             
			{
				file.createNewFile();        
			}
			//FileOutputStream fos= new FileOutputStream(file);  
			//fos.write(str.getBytes());         
			//fos.close();                           
			
			//System.out.println("result2");

			OutputStreamWriter writer1 = new OutputStreamWriter( new FileOutputStream(file),"EUC-KR"); 

			//System.out.println("result3");
			
			String result = new String(str);
			//String result = new String(str.getBytes("UTF-8"),"EUC-KR") ;
	
			//System.out.println("result4 : "+result);

			writer1.write(result);
			writer1.close();
			
		}catch(IOException e){       
			System.out.println(e);
		}
	}
}
