import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//import com.tmax.proframe.devframe.common.PfmMessage;
//import com.tmax.proframe.devframe.log.Logger;

public class HttpCom {


	private static String sName = "[WasCommDriver] ";

	public byte[] process(String psSvrIp, String psSvrPort, String psAct, byte[] pbyteData) throws Exception 
	{
		HttpURLConnection htpConn = null;
		byte[] byteData = null;

		try
		{
			htpConn = open(psSvrIp, psSvrPort, psAct);
			send(htpConn, pbyteData);
			byteData = receive(htpConn);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			//Logger.error(sName + e.getMessage());
			throw new Exception(e.getMessage());
		}
		finally
		{
			close(htpConn);
		}

		return byteData;
	}

	private HttpURLConnection open(String psSvrIp, String psSvrPort, String psAct) throws Exception
	{
		String sSvrUrl = "http://" + psSvrIp + ":" + psSvrPort + "/" + psAct;
		HttpURLConnection htpConn = null;
		//Logger.debug("WasCommDriver request timer param : 24 Hours");
		System.setProperty("sun.net.client.defaultConnectTimeout","86400000");
		System.setProperty("sun.net.client.defaultReadTimeout","86400000");

		//System.out.println(sSvrUrl);
		//Logger.debug(sName + "URL " + sSvrUrl);
		
		try	{ htpConn = (HttpURLConnection) new URL(sSvrUrl).openConnection(); }
		catch (Exception e)	{ 
			//throw new Exception(PfmMessage.get("COMM-E0003")); 
		}
		htpConn.setRequestMethod("POST");
		htpConn.setDoOutput(true);
		htpConn.setDoInput(true);
		htpConn.setUseCaches(false);
		htpConn.setDefaultUseCaches(false);
		
//		Logger.debug(sName  + "open ");

		return htpConn;
	}

	private void close(HttpURLConnection phtpConn) 
	{
		phtpConn.disconnect();
//		Logger.debug(sName + "close");
	}

	private void send(HttpURLConnection phtpConn, byte[] pbyteData) throws Exception
	{
//		Logger.debug(new String(pbyteData));
		//System.out.println("# send start");
		OutputStream outStream = phtpConn.getOutputStream();
		outStream.write(pbyteData);
		outStream.flush();
		outStream.close();
		//System.out.println("# send end");
//		Logger.debug(sName + "send data");
	}

	public byte[] receive(HttpURLConnection phtpConn) throws Exception 
	{
		byte[] byteTmpBuf = new byte[8192];
		int iLen = 0;
		//System.out.println("# get start");
		InputStream isResStream = phtpConn.getInputStream();
    	ByteArrayOutputStream baosRetStream = new ByteArrayOutputStream();
    	//System.out.println("# get start2");
		while ((iLen = isResStream.read(byteTmpBuf)) != -1)
			baosRetStream.write(byteTmpBuf, 0, iLen);

		baosRetStream.flush();
		baosRetStream.close();
		
//		Logger.debug(sName + "receive data ");
//		Logger.debug(new String(baosRetStream.toByteArray()));
		//System.out.println("# get end");
		return baosRetStream.toByteArray();
	}	

}
