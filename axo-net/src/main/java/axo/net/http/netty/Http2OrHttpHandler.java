package axo.net.http.netty;

import javax.net.ssl.SSLEngine;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2OrHttpChooser;

public class Http2OrHttpHandler extends Http2OrHttpChooser {

	private final static int MAX_CONTENT_LENGTH = 1024 * 200;
	
	public Http2OrHttpHandler () {
		super (MAX_CONTENT_LENGTH);
	}
	
	public Http2OrHttpHandler (final int maxHttpContentLength) {
		super (maxHttpContentLength);
	}
	
	@Override
	protected SelectedProtocol getProtocol (final SSLEngine engine) {
		final String[] protocol = engine.getSession ().getProtocol ().split (":");
		
		if (protocol != null && protocol.length > 1) {
			final SelectedProtocol selectedProtocol = SelectedProtocol.protocol (protocol[1]);
			return selectedProtocol;
		}
		
		return SelectedProtocol.UNKNOWN;
	}
	
	@Override
	protected ChannelHandler createHttp1RequestHandler () {
		return new Http1Handler ();
	}
	
	@Override
	protected Http2ConnectionHandler createHttp2RequestHandler () {
		return new Http2Handler ();
	}
}
