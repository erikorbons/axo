package axo.geometry;

import java.util.Iterator;

import axo.data.IntString;

public final class LineString extends Geometry implements Iterable<Point> {
	private static final long serialVersionUID = -4919865508647853239L;
	
	private final IntString indices;
	
	protected LineString (final VertexBuffer buffer, final IntString indices) {
		super (buffer);

		this.indices = indices;
	}
	
	public int getPointCount () { 
		return indices.getLength ();
	}
	
	public Point getPoint (final int i) {
		return new Point (getVertexBuffer (), indices.get (i));
	}
	
	protected final static class DetachedLineString extends DetachedGeometry<LineString> {
		private static final long serialVersionUID = 1369497076553227986L;
		
		private final IntString indices;
		
		public DetachedLineString (final IntString indices) {
			this.indices = indices;
		}
		
		@Override
		public LineString attach (final VertexBuffer buffer) {
			return new LineString (buffer, indices);
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
