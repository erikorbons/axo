package axo.geometry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class MultiGeometry extends Geometry implements Iterable<Geometry> {
	private static final long serialVersionUID = 8328028995141636208L;
	
	private final List<DetachedGeometry<? extends Geometry>> geometries;
	
	protected MultiGeometry (final VertexBuffer buffer, final List<DetachedGeometry<? extends Geometry>> geometries) {
		super (buffer);
		
		this.geometries = new ArrayList<> (geometries);
	}
	
	public int getGeometryCount () {
		return geometries.size ();
	}
	
	public Geometry getGeometry (final int index) {
		return geometries.get (index).attach (getVertexBuffer ());
	}
	
	protected final static class DetachedMultiGeometry extends DetachedGeometry<MultiGeometry> {
		private static final long serialVersionUID = 1006232842376003493L;
		
		private final List<DetachedGeometry<? extends Geometry>> geometries;

		protected DetachedMultiGeometry (final List<DetachedGeometry<? extends Geometry>> geometries) {
			this.geometries = new ArrayList<> (geometries);
		}

		@Override
		public MultiGeometry attach (final VertexBuffer buffer) {
			return new MultiGeometry (buffer, geometries);
		}
	}

	@Override
	public Iterator<Geometry> iterator () {
		return new Iterator<Geometry> () {
			private int i = 0;
			
			@Override
			public boolean hasNext () {
				return i < geometries.size ();
			}

			@Override
			public Geometry next () {
				return getGeometry (i ++);
			}
		};
	}
}
