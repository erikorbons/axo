package axo.geometry;

public class VertexBufferBuilder {
	private final int dimensions;
	private final double epsilon;
	private double[] values;
	
	public VertexBufferBuilder (final int dimensions, final double epsilon) {
		this.dimensions = dimensions;
		this.epsilon = epsilon;
	}

	public VertexBuffer build () {
		return new VertexBuffer (dimensions, values);
	}
}
