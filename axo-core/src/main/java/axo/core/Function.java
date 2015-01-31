package axo.core;

@FunctionalInterface
public interface Function<A, R> {
	R apply (A a);
}
