package axo.net.http;

public final class HttpServerConfiguration {

	private final int port;
	private final boolean useSsl;
	
	public HttpServerConfiguration (final int port, final boolean useSsl) {
		this.port = port;
		this.useSsl = useSsl;
	}

	public static HttpServerConfiguration defaultHttp () {
		return new HttpServerConfiguration (8080, false);
	}
	
	public int getPort () {
		return port;
	}

	public boolean isUseSsl () {
		return useSsl;
	}
}
