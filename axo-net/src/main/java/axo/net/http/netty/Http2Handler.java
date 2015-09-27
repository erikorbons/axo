package axo.net.http.netty;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DefaultHttp2FrameReader;
import io.netty.handler.codec.http2.DefaultHttp2FrameWriter;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameAdapter;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2FrameReader;
import io.netty.handler.codec.http2.Http2FrameWriter;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2InboundFrameLogger;
import io.netty.handler.codec.http2.Http2OutboundFrameLogger;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogLevel;

public class Http2Handler extends Http2ConnectionHandler {

	private final static Http2FrameLogger logger = new Http2FrameLogger (InternalLogLevel.INFO);
	final static ByteBuf RESPONSE_BYTES = unreleasableBuffer(copiedBuffer("Hello World", CharsetUtil.UTF_8));	
	private static final String UPGRADE_RESPONSE_HEADER = "Http-To-Http2-Upgrade";
	
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
	public void userEventTriggered (final ChannelHandlerContext ctx, final Object evt)
			throws Exception {
		
		System.out.println (evt);
		
		 if (evt instanceof HttpServerUpgradeHandler.UpgradeEvent) {
			// Write an HTTP/2 response to the upgrade request
			final Http2Headers headers =
					new DefaultHttp2Headers ().status (OK.codeAsText ())
					.set (new AsciiString (UPGRADE_RESPONSE_HEADER), new AsciiString ("true"));
			
			encoder ().writeHeaders (ctx, 1, headers, 0, true, ctx.newPromise ());
		}
		 
		super.userEventTriggered(ctx, evt);	
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		System.out.println (cause);
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}
	
	private static class SimpleHttp2FrameListener extends Http2FrameAdapter {
		private Http2ConnectionEncoder encoder;
		
		public void encoder (final Http2ConnectionEncoder encoder) {
			this.encoder = encoder;
		}
		
		/**
		* If receive a frame with end-of-stream set, send a pre-canned response.
		*/
		@Override
		public int onDataRead (final ChannelHandlerContext ctx, 
				final int streamId, final ByteBuf data, final int padding,
				final boolean endOfStream) throws Http2Exception {
			int processed = data.readableBytes () + padding;
			
			if (endOfStream) {
				sendResponse(ctx, streamId, data.retain());
			}
			
			return processed;
		}
		
		/**
		* If receive a frame with end-of-stream set, send a pre-canned response.
		*/
		@Override
		public void onHeadersRead (final ChannelHandlerContext ctx, final int streamId,
				final Http2Headers headers, final int streamDependency, final short weight,
				final boolean exclusive, final int padding, final boolean endStream) throws Http2Exception {
			
			if (endStream) {
				sendResponse(ctx, streamId, RESPONSE_BYTES.duplicate());
			}
		}
		
		/**
		* Sends a "Hello World" DATA frame to the client.
		*/
		private void sendResponse(final ChannelHandlerContext ctx, final int streamId, final ByteBuf payload) {
			// Send a frame for the response status
			final Http2Headers headers = new DefaultHttp2Headers ().status (OK.codeAsText ());
			
			encoder.writeHeaders (ctx, streamId, headers, 0, false, ctx.newPromise ());
			encoder.writeData(ctx, streamId, payload, 0, true, ctx.newPromise ());
			ctx.flush ();
		}		
	}
}
