package axo.features.osm.model;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class OsmWay extends OsmPrimitive {
	private static final long serialVersionUID = -3237997818413121062L;
	
	private final long[] refs;
	
	public OsmWay (final long[] refs, final Map<String, String> kvps) {
		super (kvps);
		
		this.refs = Arrays.copyOf (refs, refs.length);
	}

	public List<Long> getRefs () {
		return new AbstractList<Long> () {
			@Override
			public Long get (final int index) {
				return refs[index];
			}

			@Override
			public int size () {
				return refs.length;
			}
		};
	}
	
	@Override
	public String toString () {
		final StringBuilder ids = new StringBuilder ();
		for (final long l: refs) {
			if (ids.length () != 0) {
				ids.append (",");
			}
			ids.append ("" + l);
		}
		
		return "OsmWay(" + ids.toString () + ")";
	}
}
