package axo.features.osm.model;

public final class OsmNode extends OsmPrimitive {
	private static final long serialVersionUID = -3414943087435350069L;
	
	final double latitude;
	final double longitude;
	
	public OsmNode (final long id, final double latitude, final double longitude) {
		super (id);
		
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
}
