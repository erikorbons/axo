package axo.net.http;

import java.util.concurrent.atomic.AtomicReference;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.GenericFutureListener;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.Producer;
import axo.core.StreamContext;
import axo.net.http.netty.HttpServerInitializer;

public class HttpRequestProducer extends Producer<HttpRequest> {

	private final StreamContext context;
	private final HttpServerConfiguration configuration;
	private AtomicReference<Subscriber<? super HttpRequest>> subscriber = new AtomicReference<> (null);
	
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
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}
		
		// Don't allow multi-subscribe on a HTTP request producer:
		if (!this.subscriber.compareAndSet (null, subscriber)) {
			subscriber.onError (new IllegalStateException ("Multi-subscribe not supported on HttpRequestProducer"));
			return;
		}
		
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

		try {
			Channel ch = channelFuture.sync().channel();
			
			ch.closeFuture ().sync ();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		subscriber.onSubscribe (new Subscription () {
			@Override
			public void request (final long n) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void cancel () {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
