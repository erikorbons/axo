package axo.core.storage;

import axo.core.StreamContext;

public interface Storage extends AutoCloseable {

	StorageSession createSession ();
	StreamContext getContext ();
}
