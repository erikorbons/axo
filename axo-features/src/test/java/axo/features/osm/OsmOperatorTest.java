package axo.features.osm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import axo.core.LongPair;
import axo.core.Producer;
import axo.features.osm.model.FileBlock;
import axo.features.osm.model.Osm.PrimitiveBlock;
import axo.features.osm.model.OsmPrimitive;
import axo.test.AxoTest;
import axo.test.AxoTest.SynchronousConsumer;

public class OsmOperatorTest  {
	
	static AxoTest axo;
	
	
	@BeforeClass
	public void createAxo () {
		axo = new AxoTest ();
	}
	
	@AfterClass
	public void destroyAxo () throws Throwable {
		axo.close ();
	}

	@Test
	public void testConsumeOsm () throws Throwable {
		final Producer<LongPair<OsmPrimitive>> primitives = axo
			.resource ("axo/features/osm/map.pbf", 2)
			.<FileBlock>lift ((context, subscriber) -> new OsmFileBlockOperator (context, subscriber))
			.<PrimitiveBlock>lift ((context, subscriber) -> new OsmDataOperator (context, subscriber))
			.lift ((context, subscriber) -> new OsmPrimitiveOperator (context, subscriber));
		
		AxoTest.consume (primitives, this::assertPrimitiveStream);
	}
	
	private void assertPrimitiveStream (final SynchronousConsumer<LongPair<OsmPrimitive>> consumer) {
		LongPair<OsmPrimitive> primitive;
		while ((primitive = consumer.takeOne ()) != null) {
			System.out.println (primitive);
		}
	}
}
