package axo.features.osm;

public class OsmFormatException extends RuntimeException {
	private static final long serialVersionUID = -2111691870414261687L;

	public OsmFormatException () {
		super ();
	}
	
	public OsmFormatException (final Throwable cause) {
		super (cause);
	}
	
	public OsmFormatException (final String message) {
		super (message);
	}
	
	public OsmFormatException (final String message, final Throwable cause) {
		super (message, cause);
	}
}
