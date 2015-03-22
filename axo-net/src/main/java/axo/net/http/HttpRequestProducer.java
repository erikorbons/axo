package axo.net.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

import org.reactivestreams.Subscriber;

import axo.core.Producer;
import axo.core.StreamContext;
import axo.net.http.netty.HttpServerInitializer;

public class HttpRequestProducer extends Producer<HttpRequest> {

	private final StreamContext context;
	private final HttpServerConfiguration configuration;
	
	public HttpRequestProducer (final HttpServerConfiguration configuration, final StreamContext context) {
		if (configuration == null) {
			throw new NullPointerException ("configuration cannot be null");
		}
		if (context == null) {
			throw new NullPointerException ("context cannot be null");
		}
		
		this.configuration = configuration;
		this.context = context;
	}

	@Override
	public StreamContext getContext () {
		return context;
	}
	
	@Override
	public void subscribe (final Subscriber<? super HttpRequest> subscriber) {
		final SslContext sslContext = null;
		
		final EventLoopGroup bossGroup = new NioEventLoopGroup (1);
		final EventLoopGroup workerGroup = new NioEventLoopGroup ();
		
		final ServerBootstrap serverBootstrap = new ServerBootstrap ();
		
		serverBootstrap.option (ChannelOption.SO_BACKLOG, 1024);
		serverBootstrap
			.group (bossGroup, workerGroup)
			.channel (NioServerSocketChannel.class)
			.handler (new LoggingHandler (LogLevel.DEBUG))
			.childHandler (new HttpServerInitializer (sslContext));
		
		final ChannelFuture channelFuture = serverBootstrap
				.bind (configuration.getPort ());
		
	}
}
