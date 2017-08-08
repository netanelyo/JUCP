package org.ucx.jucx.tests.perftest;

import org.ucx.jucx.Context;
import org.ucx.jucx.UCPParams;
import org.ucx.jucx.UCPParams.Features;
import org.ucx.jucx.UCPParams.FieldMask;
import org.ucx.jucx.tests.perftest.PerfContext.Callback;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.UcpObjects;

public abstract class PerftestBase {
	protected PerfContext ctx;
	
	protected final static long TAG = 0x290592;
	
	protected abstract void connect();
	
	protected void run(PerfParams params) {
		ctx = new PerfContext(params);
		setup();
		connect();
	}
	
	protected boolean done() {
		return ctx.measure.done();
	}
	
	private void setup() {
		long feats 	= Features.UCP_FEATURE_TAG;
		long mask 	= FieldMask.UCP_PARAM_FIELD_FEATURES;

		Context ucpCtx = Context.getInstance(new UCPParams(feats, mask));
		
		UcpObjects ucpObj = new UcpObjects(ucpCtx);
		PerfParams params = ctx.params;
		Callback cb = new Callback();
		
		ucpObj.setWorker(cb, params.events);
		
		ctx.cb = cb;
		ctx.ucpObj = ucpObj;
	}
}







