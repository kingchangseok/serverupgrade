package com.azsoft.ecams.socket;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;


public class EcamsClientPipelineFactory implements ChannelPipelineFactory {


	public ChannelPipeline getPipeline() throws Exception {

		// TODO Auto-generated method stub
		
		ChannelPipeline pipeline = pipeline();
		//pipeline.addLast("frameDecoder",new LengthFieldBasedFrameDecoder(1073741824, 0, 4, 0, 4));
		pipeline.addLast("frameDecoder",new ProtobufVarint32FrameDecoder());		
		pipeline.addLast("protobufDecoder", new ProtobufDecoder(com.azsoft.ecams.proto.ProtoEcams.ReturnMsg.getDefaultInstance()));
		//pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
		pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender()); 		
		pipeline.addLast("protobufEncoder", new ProtobufEncoder());		
		pipeline.addLast("handler", new EcamsClientHandler());
		return pipeline;
		
	}

}
