package axo.core.data;

import java.util.Objects;

public final class ConcatenatedByteString implements ByteString {
	private static final long serialVersionUID = -1163465066411895878L;
	
	private final int aLength;
	private final ByteString a;
	private final ByteString b;
	
	public ConcatenatedByteString (final ByteString a, final ByteString b) {
		this.a = Objects.requireNonNull (a, "a cannot be null");
		this.b = Objects.requireNonNull (b, "b cannot be null");
		this.aLength = a.getLength ();
	}

	@Override
	public int getLength () {
		return a.getLength () + b.getLength ();
	}

	@Override
	public byte get (final int i) {
		return i < aLength ? a.get (i) : b.get (i - aLength);
	}

	@Override
	public byte[] toByteArray () {
		final byte[] bytes = new byte[aLength + b.getLength ()];
		
		if (aLength > 0) {
			System.arraycopy (a.toByteArray (), 0, bytes, 0, aLength);
		}
		if (b.getLength () > 0) {
			System.arraycopy (b.toByteArray (), 0, bytes, aLength, b.getLength ());
		}
		
		return bytes;
	}
	
	

}
