package axo.geometry;

public final class Point extends Geometry {
	private static final long serialVersionUID = 6790877847612145846L;
	
	private final int index;
	
	protected Point (final VertexBuffer buffer, final int index) {
		super (buffer);
		
		if (index < 0) {
			throw new IllegalArgumentException ("index should be >= 0");
		}
		if (index < 0 || index >= buffer.getLength ()) {
			throw new IllegalArgumentException ("invalid index: " + index);
		}
			
		this.index = index;
	}
	
	public double getComponent (final int component) {
		return getVertexBuffer ().get (index, component);
	}
	
	protected final static class DetachedPoint extends DetachedGeometry<Point> {
		private static final long serialVersionUID = 1L;
		
		private final int index;
		
		public DetachedPoint (final int index) {
			this.index = index;
		}
		
		@Override
		public Point attach (final VertexBuffer buffer) {
			return new Point (buffer, index);
		}
	}
}
