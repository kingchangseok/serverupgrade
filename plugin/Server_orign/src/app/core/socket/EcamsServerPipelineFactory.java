package app.core.socket;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.springframework.stereotype.Service;

import app.core.proto.ProtoEcams;

@Service
public class EcamsServerPipelineFactory implements ChannelPipelineFactory {
	private ChannelHandler handler;
	
	
	public ChannelHandler getHandler() {
		return handler;
	}

	public void setHandler(ChannelHandler handler) {
		this.handler = handler;
	}

	public EcamsServerPipelineFactory(){
		
	}
	
	public EcamsServerPipelineFactory(ChannelHandler handler){
		this.handler = handler;
		
		System.out.println("EcamsServerPipelineFactory load");
	}
	
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();
		//pipeline.addLast("frameDecoder",new LengthFieldBasedFrameDecoder(1073741824, 0, 4, 0, 4));
		pipeline.addLast("frameDecoder",new ProtobufVarint32FrameDecoder());
		pipeline.addLast("protobufDecoder", new ProtobufDecoder(ProtoEcams.EcamsMessage.getDefaultInstance()));
		//pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
		pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
		pipeline.addLast("protobufEncoder", new ProtobufEncoder());
		pipeline.addLast("handler", handler);
		return pipeline;
	}
}
