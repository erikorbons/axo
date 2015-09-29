package axo.core;

import java.io.Serializable;
import java.util.Objects;

public class LongPair<B> implements Serializable {
	private static final long serialVersionUID = -8849686028635335273L;
	
	private final long a;
	private final B b;
	
	public LongPair (final long a, final B b) {
		this.a = a;
		this.b = Objects.requireNonNull (b, "b cannot be null");
	}

	public long getA () {
		return a;
	}

	public B getB () {
		return b;
	}
	
	@Override
	public String toString () {
		return "Pair(" + a + ", " + b + ")";
	}
}
