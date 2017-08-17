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
import org.ucx.jucx.utils.Utils;

public class BandwidthClient extends BandwidthTest implements PerftestClient {
	
	@Override
	protected void warmup() {
		EndPoint ep = ctx.ucpObj.endPoint;
		Worker worker = ctx.ucpObj.worker;
		
		PerfParams params = ctx.params;
		int size = params.size;
		int iters = params.warmupIter;
		
		for (int i = 0; i < iters; i++) {
			
			ep.tagSendAsync(buffer, size, TAG, i);

			worker.wait(1);
			
			if (ctx.print) {
				System.out.println("Iteration #" + i + " in warmup");
			}
		}
	}
	
	@Override
	protected void initBuffer() {
		super.initBuffer();
		buffer.put(Utils.generateRandomBytes(buffer.capacity()));
		buffer.rewind();
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
			ep.tagSendAsync(buffer, size, TAG, i);

			worker.wait(1);
			
			measure.currTime = Time.nanoTime();
			
			measure.setMeasurement(i++, 1);
			
			if (ctx.print) {
				System.out.println("Iteration #" + i + " in main loop");
			}
		}
		
		measure.endTime = measure.currTime;
		
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
