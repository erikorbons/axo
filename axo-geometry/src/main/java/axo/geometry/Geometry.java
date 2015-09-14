package axo.geometry;

import java.io.Serializable;
import java.util.Objects;

public abstract class Geometry implements Serializable {
	private static final long serialVersionUID = -929837229364447459L;
	
	private final VertexBuffer	vertexBuffer;
	
	protected Geometry (final VertexBuffer vertexBuffer) {
		this.vertexBuffer = Objects.requireNonNull (vertexBuffer, "vertexBuffer cannot be null");
	}
	
	public VertexBuffer getVertexBuffer () {
		return vertexBuffer;
	}
	
	protected abstract static class DetachedGeometry<T extends Geometry> implements Serializable {
		private static final long serialVersionUID = 5311171353121926197L;
		
		public abstract T attach (final VertexBuffer buffer);
	}
	
	public static Point createPoint (final double ... components) {
		if (components.length < 2 || components.length > 3) {
			throw new IllegalArgumentException ("components must be 2 or 3");
		}
		
		return new Point (new VertexBuffer (components.length, components), 0);
	}

	public static void buildLineString (final int dimensions) {
	}
	
	public static void buildLineString () {
	}
	
	public static void buildLineString3D () {
	}
	
	public static void buildWinding (final int dimensions) {
	}
	
	public static void buildWinding () {
	}
	
	public static void buildWinding3D () {
	}
	
	public static void buildPolygon (final int dimensions) {
	}
	
	public static void buildPolygon () {
	}
	
	public static void buildPolygon3D () {
	}
	
	public static void buildMultiGeometry (final int dimensions) {
	}
	
	public static void buildMultiGeometry () {
	}
	
	public static void buildMultiGeometry3D () {
	}
}
