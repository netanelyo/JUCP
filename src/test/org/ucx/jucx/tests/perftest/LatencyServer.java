package org.ucx.jucx.tests.perftest;

import org.ucx.jucx.EndPoint;
import org.ucx.jucx.Worker;
import org.ucx.jucx.tests.perftest.PerfContext.Callback;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;

public class LatencyServer extends LatencyTest {
	
	@Override
	protected void runPingPong(int iters) {
		EndPoint ep = ctx.ucpObj.endPoint;
		Worker worker = ctx.ucpObj.worker;
		PerfParams params = ctx.params;
		int size = params.size;
		Callback cb = (Callback) ctx.cb;
		cb.cnt = 0;
		
		for (int i = 0; i < iters; i++) {
			worker.recvMessageAsync(TAG, Worker.DEFAULT_TAG_MASK, recvBuff, size, i);
			
			int bound = 2*i + 1;
			while (cb.cnt < bound)
				worker.progress();
			
			ep.sendMessageAsync(TAG, sendBuff, size, i);
		}
	}
}
