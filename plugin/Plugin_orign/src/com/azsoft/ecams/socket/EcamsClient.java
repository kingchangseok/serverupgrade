package com.azsoft.ecams.socket;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.AdaptiveReceiveBufferSizePredictor;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;
import com.azsoft.ecams.proto.ProtoEcams.UserInfo;


public class EcamsClient {
	private String ip;
	private int port;
	
	
	public EcamsClient(String ip,String port){
		this.ip = ip;
		this.port = Integer.parseInt(port);
	}	
	
	
	public ReturnMsg sendMsg(EcamsMessage ecamsmsg){
		ReturnMsg returnMsg = null;
		
        // Configure the client.

        ClientBootstrap bootstrap = new ClientBootstrap(
                new NioClientSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
        
        bootstrap.setOption("connectTimeoutMillis", 5000);
        bootstrap.setOption("tcpNoDelay",true);
        bootstrap.setOption("keepAlive",true);
        bootstrap.setOption("reuseAddress",true);
        bootstrap.setOption("soLinger",0);
        bootstrap.setOption("child.receiveBufferSizePredictor", new AdaptiveReceiveBufferSizePredictor(2097152,5242880,10485760));
        // Configure the event pipeline factory.
        bootstrap.setPipelineFactory(new EcamsClientPipelineFactory());

        // Make a new connection.
        ChannelFuture connectFuture =
        	bootstrap.connect(new InetSocketAddress(ip, port));
        

        Channel channel = connectFuture.awaitUninterruptibly().getChannel();

    
        EcamsClientHandler handler = channel.getPipeline().get(EcamsClientHandler.class);
        
        if (ecamsmsg.getUserinfo().getId() != null) {
        	UserInfo.Builder userinfo_builder = UserInfo.newBuilder();
        	userinfo_builder.setId(ecamsmsg.getUserinfo().getId());
        	userinfo_builder.setPasswd(ecamsmsg.getUserinfo().getPasswd());
        	userinfo_builder.setName("1");
        	
        	EcamsMessage.Builder ecamsmsg_builder = ecamsmsg.toBuilder();
        	ecamsmsg_builder.setUserinfo(userinfo_builder.build());
        	ecamsmsg = ecamsmsg_builder.build();
        }
        
        returnMsg = handler.checkServer(ecamsmsg);
        
        if (ecamsmsg.getMsgtype().equals("FILETRANS")) {
        	System.out.println("asdfasdfasdf");
        }
        /*
        if (ecamsmsg.getMsgtype().equals("SYNC_PROJECT_GETLIST")){
        	if (returnMsg.getReturnval()==0){
        		ReturnMsg.Builder returnMsg_builder = returnMsg.toBuilder();
        		EcamsMessage.Builder ecamsmsg_build = ecamsmsg.toBuilder();
        	
        	
        		int totpage = returnMsg.getEcamsmsg().getTotpage();
        		int pagenum = returnMsg.getEcamsmsg().getPagenum();
        	        	
        		pagenum++;
        		for (;pagenum<totpage;){
	        		ecamsmsg_build.setTotpage(totpage);
	            	ecamsmsg_build.setPagenum(pagenum++);
	            	
	        		returnMsg = handler.checkServer(ecamsmsg_build.build());
	        		
	        		if (returnMsg.getReturnval()!=0){
		        		returnMsg_builder.setReturnStr(returnMsg.getReturnStr());
		        		returnMsg_builder.setReturnval(returnMsg.getReturnval());	        			
	        			break;
	        		}
	        		
	        		EcamsMessage.Builder ecamsmsgtmp_builder = returnMsg_builder.getEcamsmsgBuilder();
	        		FileDataList.Builder filedatalist_builder = ecamsmsgtmp_builder.getFiledatalistBuilder();
	        		filedatalist_builder.addAllFiledatas(returnMsg.getEcamsmsg().getFiledatalist().getFiledatasList());
	        		ecamsmsgtmp_builder.setFiledatalist(filedatalist_builder.build());
	        		returnMsg_builder.setEcamsmsg(ecamsmsgtmp_builder.build());
	        		returnMsg_builder.setReturnStr(returnMsg.getReturnStr());
	        		returnMsg_builder.setReturnval(returnMsg.getReturnval());
	        		
	        		
        		}
        		
        		returnMsg = returnMsg_builder.build();
        	}
        }
        */

        channel.close().awaitUninterruptibly();
        
        bootstrap.releaseExternalResources();
        
        /*
        if (returnMsg.getReturnStr().startsWith("SOCKERR")){
        	final String returnStr = returnMsg.getReturnStr().substring(7);
        	if(null != returnStr && !"null".equals(returnStr)){
	        	Display.getDefault().asyncExec(new Runnable() {
					public void run() {				
						MessageBox messageBox = new MessageBox(new Shell());
						messageBox.setMessage(returnStr);
						messageBox.setText("\uc18c\ucf13\uc5d0\ub7ec");
						messageBox.open();
					}
				});
        	}
        }
		*/
        
        return returnMsg;
        
	}
	
	
}
