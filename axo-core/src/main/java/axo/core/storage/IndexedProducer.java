package axo.core.storage;

import java.util.List;

import axo.core.Pair;
import axo.core.Producer;
import axo.core.StreamContext;

public abstract class IndexedProducer<K, V> extends Producer<Pair<K, V>> {

	public IndexedProducer (final StreamContext context) {
		super(context);
	}
	
	public abstract Producer<Pair<K, V>> item (K key);
	public abstract Producer<Pair<K, V>> items (List<K> keys);
	public abstract Producer<Pair<K, V>> items (Producer<K> keys);
	public abstract Producer<Pair<K, V>> range (K min, K max);
}
