package axo.features.osm;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.google.protobuf.InvalidProtocolBufferException;

import axo.features.osm.model.Osm;
import axo.features.osm.model.Osm.Blob;
import axo.features.osm.model.Osm.BlobHeader;
import axo.features.osm.model.Osm.ChangeSet;
import axo.features.osm.model.Osm.HeaderBlock;
import axo.features.osm.model.Osm.Node;
import axo.features.osm.model.Osm.PrimitiveBlock;
import axo.features.osm.model.Osm.PrimitiveGroup;
import axo.features.osm.model.Osm.Relation;
import axo.features.osm.model.Osm.Way;

public class OsmReader implements AutoCloseable {
	
	private final DataInputStream inputStream;
	
	public OsmReader (final InputStream inputStream) {
		this.inputStream = new DataInputStream (
				Objects.requireNonNull (inputStream, "inputStream cannot be null")
			);
	}

	public void read () throws IOException, DataFormatException {
		while (inputStream.available () > 0) {
			System.out.println ("Parsing FileBlock:");
			
			final int blobHeaderSize = inputStream.readInt ();
			
			System.out.println ("  Blob header size: " + blobHeaderSize);
			
			if (blobHeaderSize > 64 * 1024) {
				throw new IOException ("blob header too large");
			}
			
			final byte[] headerBytes = new byte[blobHeaderSize];
			inputStream.readFully (headerBytes);
			
			final BlobHeader header = Osm.BlobHeader.parseFrom (headerBytes);
			
			System.out.println ("  type: " + header.getType ());
			System.out.println ("  datasize: " + header.getDatasize ());
			
			if (header.getDatasize () > 32 * 1024 * 1024) {
				throw new IOException ("blob data size too large");
			}
			
			final byte[] blobBytes = new byte[header.getDatasize ()];
			inputStream.readFully (blobBytes);

			final Blob blob = Blob.parseFrom (blobBytes);
			
			System.out.println ("  has raw: " + blob.hasRaw ());
			System.out.println ("  has raw size: " + blob.hasRawSize ());
			System.out.println ("  has zlibData: " + blob.hasZlibData ());
			
			final byte[] blobData = getBlobData (blob);
			
			parseFileBlock (header, blobData);
		}
	}
	
	private byte[] getBlobData (final Blob blob) throws DataFormatException {
		if (blob.hasRaw ()) {
			return blob.getRaw ().toByteArray ();
		} else {
			final byte[] uncompressed = new byte[blob.getRawSize ()];
			final Inflater inflater = new Inflater ();
			inflater.setInput (blob.getZlibData ().toByteArray ());
			inflater.inflate (uncompressed);
			return uncompressed;
		}
	}
	
	private void parseFileBlock (final BlobHeader header, final byte[] data) throws InvalidProtocolBufferException {
		if ("OSMHeader".equals (header.getType ())) {
			parseOSMHeader (data);
		} else if ("OSMData".equals (header.getType ())) {
			parseOSMData (data);
		}
	}
	
	private void parseOSMHeader (final byte[] data) throws InvalidProtocolBufferException {
		final HeaderBlock header = HeaderBlock.parseFrom (data);

		if (header.hasBbox ()) {
			System.out.println ("  bbox: " 
					+ header.getBbox().getLeft ()
					+ ", " + header.getBbox ().getTop ()
					+ ", " + header.getBbox ().getRight ()
					+ ", " + header.getBbox ().getBottom ());
		}
		
		for (final String s: header.getRequiredFeaturesList ()) {
			System.out.println ("  required feature: " + s);
		}
		for (final String s: header.getOptionalFeaturesList ()) {
			System.out.println ("  optional feature: " + s);
		}
	}
	
	private void parseOSMData (final byte[] data) throws InvalidProtocolBufferException {
		final PrimitiveBlock block = PrimitiveBlock.parseFrom (data);

		for (final PrimitiveGroup group: block.getPrimitivegroupList ()) {
			for (final Node node: group.getNodesList ()) {
				System.out.println ("  node: " + node.getId () + ", " + node.getLat () + node.getLon ());
			}

			if (group.hasDense ()) {
				System.out.println ("  dense nodes: " + group.getDense ().getLatCount ());
			}
			
			for (final Way way: group.getWaysList ()) {
				System.out.println ("  way: " + way.getId () + ", count: " + way.getRefsCount ());
			}
			
			for (final Relation relation: group.getRelationsList ()) {
				System.out.println ("  relation: " + relation.getId () + ", count: " + relation.getMemidsCount ());
			}
			
			for (final ChangeSet changeSet: group.getChangesetsList ()) {
				System.out.println ("  changeSet: " + changeSet.getId ());
			}
		}
	}
	
	@Override
	public void close () throws Exception {
		inputStream.close ();
	}
}
