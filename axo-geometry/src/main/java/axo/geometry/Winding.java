package axo.geometry;

import java.util.Iterator;

import axo.data.IntString;

public final class Winding extends Geometry implements Iterable<Point> {
	private static final long serialVersionUID = -7222766396962364052L;
	
	private final IntString indices;
	
	protected Winding (final VertexBuffer buffer, final IntString indices) {
		super (buffer);
		
		this.indices = indices;
	}

	public int getPointCount () {
		return indices.getLength ();
	}
	
	public Point getPoint (final int index) {
		return new Point (getVertexBuffer (), indices.get (index));
	}
	
	protected final static class DetachedWinding extends DetachedGeometry<Winding> {
		private static final long serialVersionUID = 6861036428516805494L;
		
		private final IntString indices;
		
		public DetachedWinding (final IntString indices) {
			this.indices = indices;
		}

		@Override
		public Winding attach (final VertexBuffer buffer) {
			return new Winding (buffer, indices);
		}
	}

	@Override
	public Iterator<Point> iterator () {
		return new Iterator<Point> () {
			int i = 0;
			
			@Override
			public boolean hasNext () {
				return i < indices.getLength ();
			}

			@Override
			public Point next () {
				return getPoint (i ++);
			}
		};
	}
}
