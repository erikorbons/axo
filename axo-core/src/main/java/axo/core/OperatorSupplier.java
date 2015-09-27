package axo.core;

import org.reactivestreams.Subscriber;

@FunctionalInterface
public interface OperatorSupplier<T, R> extends Function2<StreamContext, Subscriber<? super R>, Operator<T, R>> {
}
