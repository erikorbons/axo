package axo.core;

@FunctionalInterface
public interface Action2<A, B> {
	void apply (A a, B b);
}
