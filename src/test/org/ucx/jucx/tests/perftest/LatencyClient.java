package org.ucx.jucx.tests.perftest;

import org.ucx.jucx.EndPoint;
import org.ucx.jucx.Worker;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfMeasurements;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.utils.Time;
import org.ucx.jucx.utils.Utils;

public class LatencyClient extends LatencyTest implements PerftestClient {

	@Override
	protected void warmup() {
		EndPoint ep = ctx.ucpObj.endPoint;
		Worker worker = ctx.ucpObj.worker;
		
		PerfParams params = ctx.params;
		int size = params.size;
		int iters = params.warmupIter;
		
		for (int i = 0; i < iters; i++) {
			
			ep.tagSendAsync(sendBuff, size, TAG, i);
			
			worker.tagRecvAsync(recvBuff, size, TAG, Worker.DEFAULT_TAG_MASK, i);
			
			worker.wait(2);
			
			if (ctx.print)
				System.out.println("Iteration #" + i + " in warmup loop");
		}
	}
	
	@Override
	protected void execute(int iters) {
		EndPoint ep = ctx.ucpObj.endPoint;
		Worker worker = ctx.ucpObj.worker;
		
		PerfParams params = ctx.params;
		int size = params.size;
		
		PerfMeasurements measure = ctx.measure;
		measure.setPerfMeasurements(params.maxTimeSecs, params.reportInterval);
		measure.setTimesArray(iters);
		
		int i = 0;
		while (!done()) {
			
			ep.tagSendAsync(sendBuff, size, TAG, i);
			
			worker.tagRecvAsync(recvBuff, size, TAG, Worker.DEFAULT_TAG_MASK, i);
			
			worker.wait(2);
			
			measure.currTime = Time.nanoTime();
			
			measure.setMeasurement(i++, 1);
			
			if (recvBuff.getInt(0) != recvBuff.getInt(size - 4) || recvBuff.getInt(0) != i - 1)
				System.out.println("Error: " + i);
			
			if (ctx.print) {
				System.out.println("Iteration #" + i + " in main loop");
				System.out.println("Received message: " + Utils.getByteBufferAsString(recvBuff));
			}
		}
		
		measure.endTime = measure.currTime;
		
		System.out.println("\nLatency Test Results:");
		printResults(ctx);
	}
}
