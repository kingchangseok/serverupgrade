package app.core.socket;

import java.net.InetSocketAddress;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.AdaptiveReceiveBufferSizePredictor;
import org.springframework.beans.BeanInstantiationException;


public class EcamsServerSock {
	private ServerBootstrap bootstrap;
	private Logger logger = Logger.getLogger(this.getClass());
	
	
	
	public void setBootstrap(ServerBootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public EcamsServerSock(){
		
	}
	
	public void start(){
		try{
			
			
			String initport = System.getProperty("ecamsjv.port");
			
			if (initport == null){
				initport = "13202";
			}
			
			if (initport.length() < 1){
				initport = "13202";
			}

	        //bootstrap.setOption("connectTimeoutMillis", 10000);
	        bootstrap.setOption("tcpNoDelay",true);
			bootstrap.setOption("keepAlive",true);			
	        bootstrap.setOption("reuseAddress",true);
	        bootstrap.setOption("soLinger",0);
	        bootstrap.setOption("child.receiveBufferSizePredictor", new AdaptiveReceiveBufferSizePredictor(2097152,5242880,10485760));
	        
	        System.out.println("Initializing application bind port:"+initport);
	        bootstrap.bind(new InetSocketAddress(Integer.parseInt(initport)));
	        
		}catch (BeanInstantiationException e) {
			e.printStackTrace();
		}
  
	    System.out.println("Done");
	}
}
