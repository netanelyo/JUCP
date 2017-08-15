package org.ucx.jucx.tests.perftest;

import java.io.IOException;

import org.ucx.jucx.EndPoint;
import org.ucx.jucx.Worker;
import org.ucx.jucx.WorkerAddress;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfMeasurements;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.TcpConnection;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.UcpObjects;
import org.ucx.jucx.utils.Time;

public class BandwidthClient extends BandwidthTest implements PerftestClient {
	
	@Override
	protected void warmup() {
		EndPoint ep = ctx.ucpObj.endPoint;
		Worker worker = ctx.ucpObj.worker;
		
		PerfParams params = ctx.params;
		int size = params.size;
		int iters = params.warmupIter;
		
		for (int i = 0; i < iters; i++) {
			
			ep.tagSendAsync(TAG, buffers.get(), size, i);

			while (!buffers.ready())
				worker.progress();
			
			if (ctx.print) {
				System.out.println("Iteration #" + i + " in warmup");
			}
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
			measure.prevTime = Time.nanoTime();
			
			ep.tagSendAsync(TAG, buffers.get(), size, i);

			while (!buffers.ready())
				worker.progress();
			
			measure.currTime = Time.nanoTime();
			
			measure.setCurrentMeasurement(i++, 1, 1, size);
			
			if (ctx.print) {
				System.out.println("Iteration #" + i + " in main loop");
			}
		}
		
		measure.endTime = measure.currTime;
		
		while (!buffers.initialized())
			worker.progress();
		
		System.out.println("\nBandwidth Test Results:");
		printResults(ctx);
	}
	
	@Override
	protected void connect() {
		UcpObjects ucp = ctx.ucpObj;
		PerfParams params = ctx.params;
		TcpConnection tcp = params.tcpConn;
		
		try {
			WorkerAddress remote = (WorkerAddress) tcp.read();
			ucp.setEndPoint(remote);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
	}
}
