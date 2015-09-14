package axo.data;

import java.io.Serializable;
import java.util.Arrays;

public final class IntString implements Serializable {
	private static final long serialVersionUID = -1242627013625823218L;
	
	private final int[] values;
	
	public IntString (final int ... values) {
		this.values = Arrays.copyOf (values, values.length);
	}
	
	public int get (final int index) {
		return values[index];
	}
	
	public int getLength () {
		return values.length;
	}
}
