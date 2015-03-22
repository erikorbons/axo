package axo.net.http;

import org.testng.annotations.Test;

import static axo.test.AxoTest.*;

public class HttpRequestProducerTest {

	@Test
	public void testCreateProducer () throws Throwable {
		axoTest ((context) -> {
			final HttpRequestProducer producer = new HttpRequestProducer (HttpServerConfiguration.defaultHttp (), context);
			
			consume (producer, (a) -> {
				System.out.println ("Consuming HTTP requests");
				final HttpRequest request = a.takeOne ();
			});
		});
	}
}
