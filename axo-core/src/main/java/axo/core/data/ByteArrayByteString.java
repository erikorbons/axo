package axo.core.data;

import java.util.Arrays;
import java.util.Objects;

public class ByteArrayByteString implements ByteString {
	private static final long serialVersionUID = 4617001406582401259L;
	
	private final byte[] bytes;
	
	public ByteArrayByteString (final byte[] bytes) {
		this.bytes = Arrays.copyOf (
				Objects.requireNonNull (bytes, "bytes cannot be null"), 
				bytes.length
			);
	}

	@Override
	public int getLength () {
		return bytes.length;
	}

	@Override
	public byte get (final int i) {
		return bytes[i];
	}

	@Override
	public byte[] toByteArray () {
		return Arrays.copyOf (bytes, bytes.length);
	}
}
