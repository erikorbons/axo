package axo.features.osm.model;

import java.util.Map;

public final class OsmNode extends OsmPrimitive {
	private static final long serialVersionUID = -3414943087435350069L;
	
	final double latitude;
	final double longitude;
	
	public OsmNode (final double latitude, final double longitude, final Map<String, String> kvps) {
		super (kvps);
		
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	@Override
	public String toString () {
		return "OsmNode(" + latitude + ", " + longitude + ")";
	}
}
