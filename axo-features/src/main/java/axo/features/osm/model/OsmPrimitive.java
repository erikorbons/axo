package axo.features.osm.model;

import java.io.Serializable;

public abstract class OsmPrimitive implements Serializable {
	private static final long serialVersionUID = -1492421473520577127L;
	
	private final long id;
	
	OsmPrimitive (final long id) {
		this.id = id;
	}

	public long getId () {
		return id;
	}
}
