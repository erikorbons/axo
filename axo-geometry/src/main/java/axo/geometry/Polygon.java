package axo.geometry;

import java.util.Arrays;

import axo.data.IntString;

public final class Polygon extends Geometry {
	private static final long serialVersionUID = -6171674334115147998L;
	
	private final IntString outerWinding;
	private final IntString[] innerWindings;
	
	protected Polygon (final VertexBuffer buffer, final IntString outerWinding, final IntString ... innerWindings) {
		super (buffer);
		
		this.outerWinding = outerWinding;
		this.innerWindings = Arrays.copyOf (innerWindings, innerWindings.length);
	}
	
	public Winding getOuterWinding () {
		return new Winding (getVertexBuffer (), outerWinding);
	}
	
	public int getInnerWindingCount () {
		return innerWindings.length;
	}
	
	public Winding getInnerWinding (final int index) {
		if (index < 0 || index >= innerWindings.length) {
			throw new ArrayIndexOutOfBoundsException ("index out of range: " + index);
		}
		
		return new Winding (getVertexBuffer (), innerWindings[index]);
	}
	
	protected final static class DetachedPolygon extends DetachedGeometry<Polygon> {
		private static final long serialVersionUID = 5950627035269869919L;
		
		private final IntString outerWinding;
		private final IntString[] innerWindings;
		
		public DetachedPolygon (final IntString outerWinding, final IntString ... innerWindings) {
			this.outerWinding = outerWinding;
			this.innerWindings = Arrays.copyOf (innerWindings, innerWindings.length);
		}

		@Override
		public Polygon attach (final VertexBuffer buffer) {
			return new Polygon (buffer, outerWinding, innerWindings);
		}
	}
}
