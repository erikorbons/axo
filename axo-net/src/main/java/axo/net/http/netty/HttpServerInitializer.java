package axo.net.http.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.ssl.SslContext;

import java.util.Collections;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslContext;
	
	public HttpServerInitializer (final SslContext sslContext) {
		this.sslContext = sslContext;
	}
	
	@Override
	protected void initChannel (final SocketChannel ch) throws Exception {
		System.out.println ("initChannel");
		if (sslContext != null) {
			configureSsl (ch);
		} else {
			configurePlain (ch);
		}
	}
	
	private void configureSsl (final SocketChannel ch) {
		ch
			.pipeline ()
			.addLast (sslContext.newHandler (ch.alloc ()), new Http2OrHttpHandler ());
	}
	
	private void configurePlain (final SocketChannel ch) {
		final HttpServerCodec sourceCodec = new HttpServerCodec ();
		final HttpServerUpgradeHandler.UpgradeCodec upgradeCodec =
				new Http2ServerUpgradeCodec (new Http2Handler ());
		final HttpServerUpgradeHandler upgradeHandler =
				new HttpServerUpgradeHandler (sourceCodec, Collections.singletonList (upgradeCodec), 65536);
		
		ch.pipeline ().addLast (sourceCodec);
		ch.pipeline ().addLast (upgradeHandler);
		ch.pipeline ().addLast (new UserEventLogger ());
	}
	
	private static class UserEventLogger extends ChannelInboundHandlerAdapter {
		@Override
		public void userEventTriggered (final ChannelHandlerContext context, final Object event) {
			System.out.println (event);
			context.fireUserEventTriggered (event);
		}
	}
}
