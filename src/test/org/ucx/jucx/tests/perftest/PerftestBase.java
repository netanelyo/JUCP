package org.ucx.jucx.tests.perftest;

import org.ucx.jucx.Context;
import org.ucx.jucx.UCPParams;
import org.ucx.jucx.UCPParams.Features;
import org.ucx.jucx.UCPParams.FieldMask;
import org.ucx.jucx.Worker.Callback;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.TcpConnection;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.UcpObjects;

public abstract class PerftestBase {
	protected PerfContext ctx;
	
	protected final static long TAG = 0x290592;
	
	protected abstract void connect();
	
	protected abstract void execute(int iters);
	
	protected void warmup() { execute(ctx.params.warmupIter); }
	
	protected void barrier(TcpConnection tcp) {
		tcp.barrier(this instanceof LatencyServer);
	}
	
	protected void run(PerfParams params) {
		setup();
		connect();
		
		TcpConnection tcp = params.tcpConn;
		
		barrier(tcp);
		warmup();
		barrier(tcp);
		
		execute(params.maxIter);
		barrier(tcp);
	}
	
	protected boolean done() {
		return ctx.measure.done();
	}
	
	void close() {
		ctx.ucpObj.close();
	}
	
	private void setup() {
		long feats 	= 	Features.UCP_FEATURE_TAG;
		long mask 	= 	FieldMask.UCP_PARAM_FIELD_FEATURES 		|
						FieldMask.UCP_PARAM_FIELD_REQUEST_SIZE	|
						FieldMask.UCP_PARAM_FIELD_REQUEST_INIT;

		Context ucpCtx = Context.getInstance(new UCPParams(feats, mask));
		
		UcpObjects ucpObj = new UcpObjects(ucpCtx);
		PerfParams params = ctx.params;
		
		ucpObj.setWorker(ctx.cb, params.events);
		
		ctx.ucpObj = ucpObj;
	}
	
	static class PerftestCallback implements Callback {
		@Override
		public void requestHandle(long requestId) { }
	}
	
}







