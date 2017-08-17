package org.ucx.jucx.tests.perftest;

import java.io.IOException;

import org.ucx.jucx.Worker;
import org.ucx.jucx.WorkerAddress;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.TcpConnection;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.UcpObjects;

public class BandwidthServer extends BandwidthTest {
	
	@Override
	protected void connect() {
		UcpObjects ucp = ctx.ucpObj;
		PerfParams params = ctx.params;
		TcpConnection tcp = params.tcpConn;
		
		try {
			WorkerAddress myAddr = ucp.worker.getAddress();
			tcp.write(myAddr);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	protected void execute(int iters) {
		Worker worker = ctx.ucpObj.worker;
		PerfParams params = ctx.params;
		int size = params.size;
		
		for (int i = 0; i < iters; i++) {
			worker.tagRecvAsync(buffer, size, TAG, Worker.DEFAULT_TAG_MASK, i);
			
			worker.wait(1);
		}
	}
}










