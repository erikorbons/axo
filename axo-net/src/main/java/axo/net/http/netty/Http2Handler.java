package axo.net.http.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DefaultHttp2FrameReader;
import io.netty.handler.codec.http2.DefaultHttp2FrameWriter;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2FrameAdapter;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2FrameReader;
import io.netty.handler.codec.http2.Http2FrameWriter;
import io.netty.handler.codec.http2.Http2InboundFrameLogger;
import io.netty.handler.codec.http2.Http2OutboundFrameLogger;
import io.netty.util.internal.logging.InternalLogLevel;

public class Http2Handler extends Http2ConnectionHandler {

	private final static Http2FrameLogger logger = new Http2FrameLogger (InternalLogLevel.INFO);
	
	public Http2Handler () {
		this(
			new DefaultHttp2Connection (true), 
			new Http2InboundFrameLogger (
				new DefaultHttp2FrameReader(), 
				logger
			), 
			new Http2OutboundFrameLogger (
				new DefaultHttp2FrameWriter(), 
				logger
			), 
			new SimpleHttp2FrameListener()
		);
	}
	
	private Http2Handler (final Http2Connection connection, 
			final Http2FrameReader frameReader, final Http2FrameWriter frameWriter, 
			final SimpleHttp2FrameListener listener) {
		super(connection, frameReader, frameWriter, listener);
		listener.encoder(encoder());
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		// TODO Auto-generated method stub
		super.userEventTriggered(ctx, evt);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}
	
	private static class SimpleHttp2FrameListener extends Http2FrameAdapter {
		private Http2ConnectionEncoder encoder;
		
		public void encoder (final Http2ConnectionEncoder encoder) {
			this.encoder = encoder;
		}
	}
}
