package axo.features.osm;

import java.io.UnsupportedEncodingException;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reactivestreams.Subscriber;

import com.google.protobuf.ByteString;

import axo.core.LongPair;
import axo.core.StreamContext;
import axo.core.operators.FsmOperator;
import axo.features.osm.model.Osm.DenseNodes;
import axo.features.osm.model.Osm.Node;
import axo.features.osm.model.Osm.PrimitiveBlock;
import axo.features.osm.model.Osm.PrimitiveGroup;
import axo.features.osm.model.Osm.Relation;
import axo.features.osm.model.Osm.Relation.MemberType;
import axo.features.osm.model.Osm.Way;
import axo.features.osm.model.OsmNode;
import axo.features.osm.model.OsmPrimitive;
import axo.features.osm.model.OsmRelation;
import axo.features.osm.model.OsmWay;

public class OsmPrimitiveOperator extends FsmOperator<PrimitiveBlock, LongPair<OsmPrimitive>> {

	public OsmPrimitiveOperator (
			final StreamContext context, 
			final Subscriber<? super LongPair<OsmPrimitive>> subscriber) {
		super(context, subscriber);
	}

	@Override
	public void handleInput (final PrimitiveBlock input) {
		// Translate the string table:
		final List<String> strings = new ArrayList<> (input.getStringtable ().getSCount ());
		for (final ByteString bs: input.getStringtable ().getSList ()) {
			try {
				strings.add (new String (bs.toByteArray (), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new OsmFormatException ("Failed to parse string table", e);
			}
		}

		// Offsets and granularity:
		final long latitudeOffset = input.getLatOffset ();
		final long longitudeOffset = input.getLonOffset ();
		final long granularity = input.getGranularity ();
		
		// Process the primitive groups:
		for (final PrimitiveGroup group: input.getPrimitivegroupList ()) {
			
			// Produce single nodes:
			for (final Node node: group.getNodesList ()) {
				final double latitude = .000000001 * (latitudeOffset + (granularity * node.getLat ()));
				final double longitude = .000000001 * (longitudeOffset + (granularity * node.getLon ()));

				produce (new LongPair<OsmPrimitive> (
						node.getId (),
						new OsmNode (
								latitude, 
								longitude,
								createKvps (strings, node.getKeysList (), node.getValsList ())
							)
					));
			}

			// Produce densely packed nodes:
			if (group.hasDense ()) {
				final DenseNodes dense = group.getDense ();
				final List<Long> ids = dense.getIdList ();
				final List<Long> lats = dense.getLatList ();
				final List<Long> lons = dense.getLonList ();
				final List<Integer> keyVals = dense.getKeysValsList ();
				
				// Sanity check on the dense nodes:
				if (ids.size () != lats.size () || ids.size () != lons.size () || lats.size () != lons.size ()) {
					throw new OsmFormatException ("ids, lats and lons should have the same length");
				}
				
				long id = 0;
				long lat = 0;
				long lon = 0;
				
				for (int i = 0, length = ids.size (), kvpIndex = 0, kvpLength = keyVals.size (); i < length; ++ i) {
					// Unapply delta coding:
					if (i == 0) {
						id = ids.get (i);
						lat = lats.get (i);
						lon = lons.get (i);
					} else {
						id = id + ids.get (i);
						lat = lat + lats.get (i);
						lon = lon + lons.get (i);
					}
					
					// Collect kvps:
					final List<Integer> keys = new ArrayList<> ();
					final List<Integer> values = new ArrayList<> ();
					for (; kvpIndex < kvpLength && keyVals.get (kvpIndex) != 0; ++ kvpIndex) {
						keys.add (keyVals.get (kvpIndex ++));
						values.add (keyVals.get (kvpIndex));
					}
					if (keyVals.get (kvpIndex) == 0) {
						++ kvpIndex;
					}
					
					// Emit a node:
					final double latitude = .000000001 * (latitudeOffset + (granularity * lat));
					final double longitude = .000000001 * (longitudeOffset + (granularity * lon));
					
					produce (new LongPair<OsmPrimitive> (
							id, 
							new OsmNode (
									latitude, 
									longitude, 
									createKvps (strings, keys, values)
								) 
						));
				}
			}
			
			// Produce ways:
			for (final Way way: group.getWaysList ()) {
				final List<Long> codedRefs = way.getRefsList ();
				final long[] refs = new long[codedRefs.size ()];
				
				// Decode the refs:
				long ref = 0;
				
				for (int i = 0; i < refs.length; ++ i) {
					if (i == 0) {
						ref = codedRefs.get (i);
					} else {
						ref = ref + codedRefs.get (i);
					}
				}
				
				// Emit a way:
				produce (new LongPair<OsmPrimitive> (
						way.getId (),
						new OsmWay (
								refs, 
								createKvps (strings, way.getKeysList (), way.getValsList ())
							)
					));
			}
			
			// Produce relations:
			for (final Relation relation: group.getRelationsList ()) {
				final List<Long> codedRefs = relation.getMemidsList ();
				final long[] refs = new long[codedRefs.size ()];
				
				// Decode the member ids:
				long ref = 0;
				
				for (int i = 0; i < refs.length; ++ i) {
					if (i == 0) {
						ref = codedRefs.get (i);
					} else {
						ref = ref + codedRefs.get (i);
					}
				}
				
				// Emit a relation:
				produce (new LongPair<OsmPrimitive> (
						relation.getId (), 
						new OsmRelation (
								relation.getTypesList().toArray (new MemberType[relation.getTypesList ().size ()]), 
								refs, 
								createKvps (strings, relation.getKeysList (), relation.getValsList ())
							)
					));
			}
			
			// Produce changesets:
			/*
			Changesets are ignored for now, because they hold no additional
			information apart from their id in the current file format version.
			
			for (final ChangeSet changeSet: group.getChangesetsList ()) {
			}
			*/
		}
	}
	
	private final Map<String, String> createKvps (final List<String> strings, final List<Integer> keys, final List<Integer> values) {
		if (keys.size () != values.size ()) {
			throw new OsmFormatException ("Lengths of keys and values lists do not match");
		}
		
		return new AbstractMap<String, String> () {
			@Override
			public Set<java.util.Map.Entry<String, String>> entrySet () {
				return new AbstractSet<Map.Entry<String, String>> () {
					@Override
					public Iterator<Map.Entry<String, String>> iterator () {
						return new Iterator<Map.Entry<String,String>> () {
							private int i = 0;

							@Override
							public boolean hasNext () {
								return i < keys.size ();
							}

							@Override
							public Map.Entry<String, String> next () {
								final String key = strings.get (keys.get (i));
								final String value = strings.get (values.get (i));
								
								return new Map.Entry<String, String> () {
									@Override
									public String getKey () {
										return key;
									}

									@Override
									public String getValue () {
										return value;
									}

									@Override
									public String setValue(String value) {
										throw new UnsupportedOperationException ();
									}
								};
							}
						};
					}

					@Override
					public int size () {
						return keys.size ();
					}
				};
			}
		};
	}

	@Override
	public void handleComplete () {
		complete ();
	}
}