package axo.core.data;

import java.io.Serializable;

public interface ByteString extends Serializable {

	int getLength ();
	byte get (int i);
	byte[] toByteArray ();
	
	public static ByteString fromByteArray (final byte[] bytes) {
		return new ByteArrayByteString (bytes);
	}
}
