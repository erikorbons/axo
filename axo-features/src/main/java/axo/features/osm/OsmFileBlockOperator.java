package axo.features.osm;

import java.nio.ByteBuffer;

import org.reactivestreams.Subscriber;

import axo.core.StreamContext;
import axo.core.data.ByteString;
import axo.core.operators.FsmOperator;
import axo.features.osm.model.FileBlock;
import axo.features.osm.model.Osm.Blob;
import axo.features.osm.model.Osm.BlobHeader;

public class OsmFileBlockOperator extends FsmOperator<ByteString, FileBlock>{

	private ByteString collectedBytes = null;
	
	public OsmFileBlockOperator (final StreamContext context, 
			final Subscriber<? super FileBlock> subscriber) {
		super(context, subscriber);
	}

	@Override
	public void handleInput (final ByteString input) {
		System.out.println ("handleInput");
		if (collectedBytes == null) {
			collectedBytes = input;
		} else {
			collectedBytes = collectedBytes.concat (input);
		}
		
		if (collectedBytes.getLength () >= 4) {
			final ByteBuffer buffer = ByteBuffer.wrap (collectedBytes.toByteArray ());
			final int blobHeaderSize = buffer.getInt ();
			
			// Check header size for sanity:
			if (blobHeaderSize > 64 * 1024) {
				throw new OsmFormatException ("Blob header too large: " + blobHeaderSize);
			}

			// Read the blob header with the remaining bytes:
			System.out.println ("Moving to read blob header state: " + 4 + ", " + collectedBytes.getLength ());
			if (collectedBytes.getLength () > 4) {
				final ByteString remainder = collectedBytes.subString (4, collectedBytes.getLength ());
				pushState (readBlobHeaderState (blobHeaderSize), remainder);
			} else {
				pushState (readBlobHeaderState (blobHeaderSize));
			}
		}
	}

	@Override
	public void handleComplete () {
		if (collectedBytes != null && collectedBytes.getLength () > 0) {
			throw new OsmFormatException ("Unexpected end of stream while reading the length of next FileBlock");
		}
		
		complete ();
	}

	public State<ByteString> readBlobHeaderState (final int blobHeaderSize) {
		// Start with no bytes read:
		collectedBytes = null;
		
		return state (
			(input) -> {
				collectedBytes = collectedBytes == null ? input : collectedBytes.concat (input);
				
				if (collectedBytes.getLength () < blobHeaderSize) {
					return;
				}
				
				// Parse the header:
				final BlobHeader header;
				try {
					 header = BlobHeader.parseFrom (
							collectedBytes
								.subString (0, blobHeaderSize)
								.toByteArray ()
						);
				} catch (Exception e) {
					throw new OsmFormatException ("Unable to parse BlobHeader", e);
				}

				// Sanity check on the blob header:
				if (header.getDatasize () > 32 * 1024 * 1024) {
					throw new OsmFormatException ("Blob data size too large: " + header.getDatasize ());
				}
				
				// Parse the blob itself with the remaining bytes:
				System.out.println ("Moving to read blob state: " + collectedBytes.getLength () + ", " + blobHeaderSize);
				if (collectedBytes.getLength () > blobHeaderSize) {
					final ByteString remainder = collectedBytes.subString (blobHeaderSize, collectedBytes.getLength ());
					final State<ByteString> state = readBlobState (header);
					gotoState (state, remainder);
				} else {
					gotoState (readBlobState (header));
				}
			},
			() -> {
				throw new OsmFormatException ("Unexpected end of stream while reading BlobHeader");
			}
		);
	}
	
	public State<ByteString> readBlobState (final BlobHeader blobHeader) {
		// Start with no bytes read:
		collectedBytes = null;
		
		final int blobSize = blobHeader.getDatasize (); 
		
		return state (
			(input) -> {
				collectedBytes = collectedBytes == null ? input : collectedBytes.concat (input);
				
				System.out.println ("readBlobState.handleInput: " + collectedBytes.getLength () + ", " + blobSize);
				
				if (collectedBytes.getLength () < blobSize) {
					return;
				}
				
				// Read the blob:
				final Blob blob;
				
				try {
					blob = Blob.parseFrom (
							collectedBytes
								.subString (0, blobSize)
								.toByteArray ()
						);
				} catch (Exception e) {
					throw new OsmFormatException ("Unable to parse Blob", e);
				}
				
				// Produce the blob and its header:
				produce (new FileBlock (blobHeader, blob));
			
				// Switch to the default state with the remaining bytes:
				// Parse the blob itself with the remaining bytes:
				System.out.println ("Switching to default state: " + blobSize + ", " + collectedBytes.getLength ());
				if (collectedBytes.getLength () > blobSize) {
					final ByteString remainder = collectedBytes.subString (blobSize, collectedBytes.getLength ());
					collectedBytes = null;
					popState (remainder);
				} else {
					collectedBytes = null;
					popState ();
				}
			},
			() -> {
				throw new OsmFormatException ("Unexpected end of stream while reading Blob");
			}
		);
	}
}
