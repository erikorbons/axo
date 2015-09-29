package axo.core;

import java.io.Serializable;
import java.util.Objects;

public class Pair<A, B> implements Serializable {
	private static final long serialVersionUID = -1957092092384185882L;
	
	private final A a;
	private final B b;
	
	public Pair (final A a, final B b) {
		this.a = Objects.requireNonNull (a, "a cannot be null");
		this.b = Objects.requireNonNull (b, "b cannot be null");
	}

	public A getA () {
		return a;
	}

	public B getB () {
		return b;
	}
}
