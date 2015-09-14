package axo.features.osm;

import org.testng.annotations.Test;

public class OsmReaderTest {

	@Test
	public void testReadPbf () throws Throwable {
		try (final OsmReader reader = new OsmReader (getClass ().getClassLoader ().getResourceAsStream ("axo/features/osm/map.pbf"))) {
			reader.read ();
		}
	}
}
