package com.azsoft.ecams.socket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.azsoft.ecams.proto.ProtoEcams.EcamsMessage;
import com.azsoft.ecams.proto.ProtoEcams.ReturnMsg;

@ChannelPipelineCoverage("all")
public class EcamsClientHandler extends SimpleChannelHandler {
	private Logger logger = Logger.getLogger(this.getClass());
	private volatile Channel channel;
	private final BlockingQueue<ReturnMsg> answer = new LinkedBlockingQueue<ReturnMsg>();

	
	public ReturnMsg checkServer(EcamsMessage ecamsmsg) {
				
		channel.write(ecamsmsg);

		ReturnMsg returnMsg = null;
		
		boolean interrupted = false;
		for (;;) {
			if (interrupted){
				break;
			}
			try {
				returnMsg = answer.take();
				break;
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}

		if (interrupted) {
			Thread.currentThread().interrupt();
		}

		//logger.error("returnVal= "+returnMsg.getReturnval());
		//logger.error("returnStr= "+returnMsg.getReturnStr());

		return returnMsg;
	}


	@Override 
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		channel = e.getChannel();
		super.channelOpen(ctx, e);
	}


	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		//logger.log(Level.ERROR,"messageReceived:"+e.getMessage());
		boolean offered = answer.offer((ReturnMsg) e.getMessage());
		assert offered;
		
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		// TODO Auto-generated method stub
		logger.error(e);
		e.getChannel().disconnect();
		e.getChannel().close();
		ReturnMsg.Builder returnMsg_builder = ReturnMsg.newBuilder();
		returnMsg_builder.setReturnval(1);
		returnMsg_builder.setReturnStr("SOCKERR"+e.getCause().getLocalizedMessage());
		
		super.exceptionCaught(ctx, e);
		
		boolean offered = answer.offer(returnMsg_builder.build());
		assert offered;
		
	}

}
