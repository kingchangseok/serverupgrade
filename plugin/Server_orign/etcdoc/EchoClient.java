package test.chat.nio;
import java.io.*;
import java.net.*;

class EchoClient
{
	public static void main(String[] args) throws IOException
	{
		Socket sock = null;
		try
		{
			sock = new Socket("211.255.17.35", 9999);
			System.out.println(sock + ": 연결됨");
			OutputStream toServer = sock.getOutputStream();
			InputStream fromServer = sock.getInputStream();
			byte[] buf = new byte[1024];
			int count;
			while ((count = System.in.read(buf)) != -1)
			{
				toServer.write(buf, 0, count);
				count = fromServer.read(buf);
				System.out.write(buf, 0, count);
			}
			toServer.close();
			while ((count = fromServer.read(buf)) != -1)
				System.out.write(buf, 0, count);
			System.out.close();
			System.out.println(sock + ": 연결 종료");
		}
		catch (IOException ex)
		{
			System.out.println("연결 종료 (" + ex + ")");
		}
		finally
		{
			try
			{
				if (sock != null)
					sock.close();
			}
			catch (IOException ex)
			{
			}
		}
	}
}
