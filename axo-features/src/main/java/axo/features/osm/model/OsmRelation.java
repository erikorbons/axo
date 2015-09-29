package axo.features.osm.model;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import axo.features.osm.model.Osm.Relation.MemberType;

public final class OsmRelation extends OsmPrimitive {
	private static final long serialVersionUID = 3563639595334276233L;
	
	private final MemberType[] memberTypes;
	private final long[] refs;
	
	public OsmRelation (final MemberType[] memberTypes, final long[] refs, final Map<String, String> kvps) {
		super (kvps);
		
		this.memberTypes = Arrays.copyOf (memberTypes, memberTypes.length);
		this.refs = Arrays.copyOf (refs, refs.length);
	}

	public List<Member> getMembers () {
		return new AbstractList<Member> () {
			@Override
			public Member get (final int index) {
				return new Member (memberTypes[index], refs[index]);
			}

			@Override
			public int size () {
				return memberTypes.length;
			}
		};
	}
	
	public final static class Member implements Serializable {
		private static final long serialVersionUID = 5697818347966065985L;
		
		private final MemberType memberType;
		private final long ref;
		
		public Member (final MemberType memberType, final long ref) {
			this.memberType = memberType;
			this.ref = ref;
		}

		public MemberType getMemberType () {
			return memberType;
		}

		public long getRef () {
			return ref;
		}
	}
	
	@Override
	public String toString () {
		return "OsmRelation(" + refs + ")";
	}
}
