package axo.core;

@FunctionalInterface
public interface Action<A> {
	void apply (A a);
}
