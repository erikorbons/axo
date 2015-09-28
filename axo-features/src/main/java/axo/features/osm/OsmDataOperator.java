package axo.features.osm;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.reactivestreams.Subscriber;

import com.google.protobuf.InvalidProtocolBufferException;

import axo.core.StreamContext;
import axo.core.operators.FsmOperator;
import axo.features.osm.model.FileBlock;
import axo.features.osm.model.Osm.Blob;
import axo.features.osm.model.Osm.HeaderBlock;
import axo.features.osm.model.Osm.PrimitiveBlock;

public class OsmDataOperator extends FsmOperator<FileBlock, PrimitiveBlock> {

	public OsmDataOperator (
			final StreamContext context, 
			final Subscriber<? super PrimitiveBlock> subscriber) {
		super(context, subscriber);
	}

	@Override
	public void handleInput (final FileBlock input) {
		if ("OSMHeader".equals (input.getHeader ().getType ())) {
			handleHeaderBlock (getBlobData (input.getBlob ()));
		} else if ("OSMData".equals (input.getHeader ().getType ())) {
			handlePrimitiveBlock (getBlobData (input.getBlob ()));
		}
	}
	
	private void handleHeaderBlock (final byte[] data) {
		final HeaderBlock header;
		
		try {
			header = HeaderBlock.parseFrom (data);
		} catch (InvalidProtocolBufferException e) {
			throw new OsmFormatException ("Failed to parse HeaderBlock", e);
		}

		for (final String s: header.getRequiredFeaturesList ()) {
			if (!"OsmSchema-V0.6".equals (s) && !"DenseNodes".equals (s)) {
				throw new OsmFormatException ("Encountered unsupported required feature: " + s);
			}
		}
	}
	
	private void handlePrimitiveBlock (final byte[] data) {
		try {
			produce (PrimitiveBlock.parseFrom (data));
		} catch (InvalidProtocolBufferException e) {
			throw new OsmFormatException ("Failed to read PrimitiveBlock", e);
		}
	}
	
	private static byte[] getBlobData (final Blob blob) {
		if (blob.hasRaw ()) {
			return blob.getRaw ().toByteArray ();
		} else {
			final byte[] uncompressed = new byte[blob.getRawSize ()];
			final Inflater inflater = new Inflater ();
			inflater.setInput (blob.getZlibData ().toByteArray ());
			try {
				inflater.inflate (uncompressed);
			} catch (DataFormatException e) {
				throw new OsmFormatException ("Unable to decode deflated block content", e);
			}
			return uncompressed;
		}
	}

	@Override
	public void handleComplete () {
		complete ();
	}
}
