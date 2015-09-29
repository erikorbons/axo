package axo.core.data;

public class ByteStringRange implements ByteString {
	private static final long serialVersionUID = -204603447234506928L;
	
	private final ByteString source;
	private final int startIndex;
	private final int endIndex;
	
	protected ByteStringRange (final ByteString source, final int startIndex, final int endIndex) {
		this.source = source;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}
	
	@Override
	public int getLength () {
		return endIndex - startIndex;
	}

	@Override
	public byte get (final int i) {
		return source.get (startIndex + i);
	}

	@Override
	public byte[] toByteArray () {
		final int length = getLength ();
		final byte[] data = new byte[length];
		
		for (int i = 0; i < length; ++ i) {
			data[i] = source.get (startIndex + i);
		}
		
		return data;
	}
}
