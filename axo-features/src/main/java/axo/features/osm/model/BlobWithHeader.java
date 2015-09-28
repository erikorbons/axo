package axo.features.osm.model;

import java.io.Serializable;
import java.util.Objects;

import axo.features.osm.model.Osm.Blob;
import axo.features.osm.model.Osm.BlobHeader;

public final class BlobWithHeader implements Serializable {
	private static final long serialVersionUID = 8143931485153141403L;
	
	private final BlobHeader header;
	private final Blob blob;
	
	public BlobWithHeader (final BlobHeader header, final Blob blob) {
		this.header = Objects.requireNonNull (header, "header cannot be null");
		this.blob = Objects.requireNonNull (blob, "blob cannot be null");
	}

	public BlobHeader getHeader () {
		return header;
	}

	public Blob getBlob () {
		return blob;
	}
}
