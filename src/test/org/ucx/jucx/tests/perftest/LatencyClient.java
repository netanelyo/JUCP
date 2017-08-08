package org.ucx.jucx.tests.perftest;

import org.ucx.jucx.EndPoint;
import org.ucx.jucx.Worker;
import org.ucx.jucx.tests.perftest.PerfContext.Callback;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfMeasurements;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.utils.Time;

public class LatencyClient extends LatencyTest implements PerftestClient {

	@Override
	protected void warmup() {
		EndPoint ep = ctx.ucpObj.endPoint;
		Worker worker = ctx.ucpObj.worker;
		
		PerfParams params = ctx.params;
		int size = params.size;
		int iters = params.warmupIter;
		
		Callback cb = (Callback) ctx.cb;
		cb.cnt = 0;
		
		for (int i = 0; i < iters; i++) {
			ep.sendMessageAsync(TAG, sendBuff, size, i);

			worker.recvMessageAsync(TAG, Worker.DEFAULT_TAG_MASK, recvBuff, size, i);

			int bound = 2*(i+1);
			while (cb.cnt < bound)
				worker.progress();
		}
	}
	
	@Override
	protected void runPingPong(int iters) {
		EndPoint ep = ctx.ucpObj.endPoint;
		Worker worker = ctx.ucpObj.worker;
		
		PerfParams params = ctx.params;
		int size = params.size;
		Callback cb = (Callback) ctx.cb;
		
		cb.cnt = 0;
		
		PerfMeasurements time = ctx.measure;
		time.setPerfMeasurements(params.maxTimeSecs, params.reportInterval);
		time.setTimesArray(iters);
		
		int i = 0;
		while (!done()) {
			time.prevTime = Time.nanoTime();
			
			ep.sendMessageAsync(TAG, sendBuff, size, i);

			worker.recvMessageAsync(TAG, Worker.DEFAULT_TAG_MASK, recvBuff, size, i);

			int bound = 2*(i+1);
			while (cb.cnt < bound)
				worker.progress();
			
			time.currTime = Time.nanoTime();
			
			time.setCurrentMeasurement(i++, 1, 1, size);
		}
		
		System.out.println("\nLatency Test Results:");
		printResults(ctx);
	}
}
