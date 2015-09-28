package axo.features.osm;

import org.reactivestreams.Subscriber;

import axo.core.StreamContext;
import axo.core.operators.FsmOperator;
import axo.features.osm.model.Osm.PrimitiveBlock;
import axo.features.osm.model.OsmPrimitive;

public class OsmPrimitiveOperator extends FsmOperator<PrimitiveBlock, OsmPrimitive> {

	public OsmPrimitiveOperator (
			final StreamContext context, 
			final Subscriber<? super OsmPrimitive> subscriber) {
		super(context, subscriber);
	}

	@Override
	public void handleInput (final PrimitiveBlock input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleComplete () {
		complete ();
	}
}