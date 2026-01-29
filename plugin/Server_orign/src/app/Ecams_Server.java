package app;


import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import app.core.socket.EcamsServerSock;

public class Ecams_Server {
	private static EcamsServerSock serversock;
	
	
	public void setServersock(EcamsServerSock serversock) {
		this.serversock = serversock;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		try{

	        // load and start spring
			ApplicationContext ac = new ClassPathXmlApplicationContext("eCAMS_Server.xml");

			serversock.start();
			 
			
		}catch (Exception e) {
			e.printStackTrace();
		}
  
	}


	
}
