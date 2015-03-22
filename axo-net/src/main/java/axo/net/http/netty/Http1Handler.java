package axo.net.http.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class Http1Handler  extends SimpleChannelInboundHandler<HttpRequest> {

	@Override
	protected void channelRead0 (final ChannelHandlerContext ctx, final HttpRequest req) throws Exception {
		if (HttpHeaderUtil.is100ContinueExpected (req)) {
			ctx.write (new DefaultFullHttpResponse (HTTP_1_1, CONTINUE));
		}
		
		final boolean keepAlive = HttpHeaderUtil.isKeepAlive (req);
		final ByteBuf content = ctx.alloc ().buffer ();
		
		content.writeBytes (Http2Handler.RESPONSE_BYTES.duplicate ());
		
		final FullHttpResponse response = new DefaultFullHttpResponse (HTTP_1_1, OK, content);
		
		response.headers().set (CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.headers().setInt (CONTENT_LENGTH, response.content ().readableBytes ());
		
		if (!keepAlive) {
			ctx.writeAndFlush (response).addListener (ChannelFutureListener.CLOSE);
		} else {
			response.headers ().set (CONNECTION, HttpHeaderValues.KEEP_ALIVE);
			ctx.writeAndFlush (response);
		}	
	}

	@Override
	public void exceptionCaught (final ChannelHandlerContext ctx, final Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, cause);
	}
}
