package axo.features.osm.model;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class OsmPrimitive implements Serializable {
	private static final long serialVersionUID = -1492421473520577127L;
	
	private final String[]	kvps;
	
	OsmPrimitive (final Map<String, String> kvps) {
		if (kvps == null || kvps.isEmpty ()) {
			this.kvps = null;
		} else {
			this.kvps = new String[kvps.size () * 2];
			int i = 0;
			
			for (final Map.Entry<String, String> entry: kvps.entrySet ()) {
				this.kvps[i ++] = entry.getKey ();
				this.kvps[i ++] = entry.getValue ();
			}
		}
	}
	
	public Map<String, String> getKvps () {
		return new KvpMap (new KvpSet (kvps));
	}
	
	private final static class KvpSet extends AbstractSet<Map.Entry<String, String>> {
		private final String[] kvps;
		
		public KvpSet (final String[] kvps) {
			this.kvps = kvps;
		}
		
		@Override
		public Iterator<Entry<String, String>> iterator () {
			if (kvps == null || kvps.length == 0) {
				return Collections.<Entry<String, String>>emptyList ().iterator ();
			}
			
			return new Iterator<Map.Entry<String,String>> () {
				private int i = 0;
				
				@Override
				public boolean hasNext () {
					return i < kvps.length;
				}

				@Override
				public Entry<String, String> next () {
					final String key = kvps[i ++];
					final String value = kvps[i ++];
					
					return new Entry<String, String> () {
						@Override
						public String getKey () {
							return key;
						}

						@Override
						public String getValue () {
							return value;
						}

						@Override
						public String setValue (String value) {
							throw new UnsupportedOperationException ();
						}
					};
				}
			};
		}

		@Override
		public int size() {
			return kvps == null ? 0 : kvps.length / 2;
		}
	}
	
	private final static class KvpMap extends AbstractMap<String, String> {
		private final KvpSet kvpSet;
		
		public KvpMap (final KvpSet kvpSet) {
			this.kvpSet = kvpSet;
		}
		
		@Override
		public Set<Map.Entry<String, String>> entrySet() {
			return kvpSet;
		}
	}
}
