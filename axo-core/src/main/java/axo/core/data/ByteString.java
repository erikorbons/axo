package axo.core.data;

import java.io.Serializable;
import java.util.Iterator;

public interface ByteString extends Serializable {

	int getLength ();
	byte get (int i);
	byte[] toByteArray ();
	
	public static ByteString fromByteArray (final byte[] bytes) {
		return new ByteArrayByteString (bytes);
	}
	
	public default ByteString subString (final int startIndex, final int endIndex) {
		if (startIndex < 0 || startIndex > getLength ()) {
			throw new IllegalArgumentException ("startIndex is invalid");
		}
		if (endIndex < startIndex || endIndex > getLength ()) {
			throw new IllegalArgumentException ("endIndex is invalid");
		}
		
		return new ByteStringRange (this, startIndex, endIndex);
	}
	
	public default Iterable<ByteString> partition (final int blockSize) {
		if (blockSize <= 0) {
			throw new IllegalArgumentException ("blockSize should be > 0");
		}
		
		return () -> new Iterator<ByteString> () {
			private int index = 0;
			
			@Override
			public boolean hasNext () {
				return index < getLength ();
			}

			@Override
			public ByteString next () {
				final int endIndex = Math.min (index + blockSize, getLength ());
				final ByteString result = subString (index, endIndex);
				index = endIndex;
				return result;
			}
		};
	}
}
