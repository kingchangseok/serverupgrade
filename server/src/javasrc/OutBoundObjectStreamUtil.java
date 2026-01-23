import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class OutBoundObjectStreamUtil {
	
	public OutBoundObjectStreamUtil(){
	}
	
	public Map<String, Object> execute(String targetUrl, Object data) throws Exception {
		
		// °á°ú ¹Ýȯ Map
		Map<String, Object> result = null;
		//ecamsLogger.error("## input data:[" + data + "]");
		//ecamsLogger.error("## targetUrl:[" + targetUrl + "]");

		if (!"".equals(targetUrl)) {

			if (data != null) {

				ObjectOutputStream p = null;
				ObjectInputStream q = null;

				try {
					// 1. Target URL connection(POST)
					URL url = new URL(targetUrl);
					URLConnection conn = url.openConnection();
					conn.setDoOutput(true);
					conn.setDoInput(true);
					conn.setUseCaches(false);
					conn.setAllowUserInteraction(false);

					// 2. Object Write Àü¼ÛÇϴÂ ºκÐ
					p = new ObjectOutputStream(conn.getOutputStream());
		            //p = new ObjectOutputStream(httpUrlConn.getOutputStream());
					p.writeObject(data);
					p.flush();
					p.close();
					
					// 3. Receive Object result ¼ö½ÅÇϴÂ ºκÐ
					q = new ObjectInputStream(conn.getInputStream());
					result = (Map<String, Object>) q.readObject();
					System.out.println("## RES_CODE[0]:" + result.get("RES_CODE"));
					
				}
				catch (IOException e) {
					e.printStackTrace();
					throw new Exception();
				}
				catch (ClassNotFoundException e) {
					e.printStackTrace();
					throw new Exception();
				}
				finally {
					// Close the ObjectInputStream
					try {
						if (p != null) {
							p.close();
						}
						if (q != null) {
							//ecamsLogger.error("## close before:" + q);
							// q.close();
						}
					}
					catch (Exception ex) {
						System.out.println("## close Exception:" + q);
						ex.printStackTrace();
						throw new Exception();
					}
				}

			}
			else {
				// ¿¡·¯ó¸®
				System.out.println("data is empty!!![OutBoundObjectStreamUtil]");
				throw new Exception();

			}

		}
		else {
			// ¿¡·¯ó¸®
			System.out.println("targetUrl is empty!!!");
			throw new Exception();
		}

		System.out.println("## result[1]:" + result);

		return result;

	}
}
