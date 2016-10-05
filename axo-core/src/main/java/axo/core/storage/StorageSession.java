package axo.core.storage;

import axo.core.Pair;
import axo.core.Producer;

public interface StorageSession extends AutoCloseable {

	Producer<String> getStores ();
	Producer<String> getStores (StorageMethod method);
	
	<T> Producer<Producer<T>> store (String name, Producer<T> producer);
	<T> Producer<Producer<T>> store (String name, Producer<T> producer, StorageMethod method);
	Producer<Void> removeAll (String name);
	
	<K, V> Producer<IndexedProducer<K, V>> storeIndexed (String name, Producer<Pair<K, V>> producer);
	<K, V> Producer<IndexedProducer<K, V>> storeIndexed (String name, Producer<Pair<K, V>> producer, StorageMethod method);
	<K, V> Producer<IndexedProducer<K, V>> updateIndexed (String name, Producer<Pair<K, V>> producer);
	<K, V> Producer<IndexedProducer<K, V>> deleteIndexed (String name, Producer<K> producer);
	
	<T> Producer<T> get (String name);
	<K, V> IndexedProducer<K, V> getIndexed (String name);
}
