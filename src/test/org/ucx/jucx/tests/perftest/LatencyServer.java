package org.ucx.jucx.tests.perftest;

import org.ucx.jucx.EndPoint;
import org.ucx.jucx.Worker;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;

public class LatencyServer extends LatencyTest {
	
	@Override
	protected void execute(int iters) {
		EndPoint ep = ctx.ucpObj.endPoint;
		Worker worker = ctx.ucpObj.worker;
		PerfParams params = ctx.params;
		int size = params.size;
		
		worker.tagRecvAsync(recvBuff, size, TAG, Worker.DEFAULT_TAG_MASK, 0);
		
		worker.wait(1);
		
		for (int i = 0; i < iters - 1; i++) {
			
			sendBuff.putInt(0, i);
			sendBuff.putInt(size - 4, i);
			ep.tagSendAsync(sendBuff, size, TAG, i);
			
			worker.tagRecvAsync(recvBuff, size, TAG, Worker.DEFAULT_TAG_MASK, i + 1);
			
			worker.wait(2);
//			int bound = 2*i + 1;
//			while (cb.cnt < bound)
//				worker.progress();
		}
		
		sendBuff.putInt(0, iters - 1);
		sendBuff.putInt(size - 4, iters - 1);
		ep.tagSendAsync(sendBuff, size, TAG, iters - 1);
		
		worker.wait(1);
		
//		worker.flush();
		
//		while (cb.cnt < 2*iters)
//			worker.progress();
	}
}
