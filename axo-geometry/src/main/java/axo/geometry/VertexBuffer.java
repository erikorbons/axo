package axo.geometry;

import java.io.Serializable;
import java.util.Arrays;

public final class VertexBuffer implements Serializable {
	private static final long serialVersionUID = -3739108644416096758L;
	
	private final int dimensions;
	private final double[] values;
	
	public VertexBuffer (final int dimensions, final double ... values) {
		if (dimensions <= 0) {
			throw new IllegalArgumentException ("dimensions should be > 0");
		}
		if (values.length % dimensions != 0) {
			throw new IllegalArgumentException ("length of values should be a multiple of " + dimensions);
		}
		
		this.dimensions = dimensions;
		this.values = Arrays.copyOf (values, values.length);
	}
	
	public int getDimensions () {
		return dimensions;
	}

	public int getLength () {
		return values.length / dimensions;
	}
	
	public double get (final int index, final int component) {
		return values[dimensions * index + component];
	}
}
