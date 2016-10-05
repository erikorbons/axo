package axo.core.storage;

import java.util.Objects;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.TxMaker;

import axo.core.Pair;
import axo.core.Producer;
import axo.core.Promise;
import axo.core.StreamContext;

public class MapDbStorage implements Storage {
	private final StreamContext context;
	private final TxMaker txMakerMemory;
	private final TxMaker txMaker;
	
	public MapDbStorage (final StreamContext context) {
		this.context = Objects.requireNonNull (context, "context cannot be null");
		this.txMaker = DBMaker
			.memoryDB ()
			.makeTxMaker ();
		this.txMakerMemory = DBMaker
				.memoryDB ()
				.makeTxMaker ();
	}
	
	@Override
	public void close () {
		txMaker.close ();
	}

	@Override
	public StorageSession createSession () {
		return new Session (txMaker.makeTx (), txMakerMemory.makeTx ());
	}

	@Override
	public StreamContext getContext () {
		return context;
	}
	
	private class Session implements StorageSession {
		private final DB db;
		private final DB memoryDb;
		
		Session (final DB db, final DB memoryDb) {
			this.db = db;
			this.memoryDb = memoryDb;
		}
		
		@Override
		public void close () throws Exception {
			try {
				db.commit ();
			} catch (Throwable t) {
				memoryDb.rollback ();
				throw t;
			}
			
			memoryDb.commit ();
		}

		@Override
		public Producer<String> getStores () {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Producer<String> getStores (final StorageMethod method) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> Producer<Producer<T>> store (final String name, final Producer<T> producer) {
			return store (name, producer, StorageMethod.IN_MEMORY);
		}

		@Override
		public <T> Producer<Producer<T>> store (final String name, final Producer<T> producer, final StorageMethod method) {
			return store (
				name, 
				producer.zip (
					getContext ().range (0, Long.MAX_VALUE), 
					(a, b) -> new Pair<Long, T> (b, a))).
				map ((result) -> result.map ((item) -> item.getB ()));
		}

		@Override
		public Producer<Void> removeAll (final String name) {
			db.delete (name);
			memoryDb.delete (name);
			return getContext ().from ((Void) null);
		}

		@Override
		public <K, V> Producer<IndexedProducer<K, V>> storeIndexed (final String name, final Producer<Pair<K, V>> producer) {
			return storeIndexed (name, producer, StorageMethod.IN_MEMORY);
		}

		@Override
		public <K, V> Producer<IndexedProducer<K, V>> storeIndexed (final String name, final Producer<Pair<K, V>> producer,
				final StorageMethod method) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <K, V> Producer<IndexedProducer<K, V>> updateIndexed (final String name, final Producer<Pair<K, V>> producer) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <K, V> Promise<IndexedProducer<K, V>> deleteIndexed (final String name, final Producer<K> producer) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> Producer<T> get (final String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <K, V> IndexedProducer<K, V> getIndexed (final String name) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
