package org.ucx.jucx.tests.perftest;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.ucx.jucx.WorkerAddress;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.TcpConnection;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.UcpObjects;
import org.ucx.jucx.utils.StringUtils;

public abstract class LatencyTest extends PerftestBase {
	
	protected ByteBuffer sendBuff;
	protected ByteBuffer recvBuff;
	
	protected abstract void runPingPong(int iters);
	
	protected void warmup() {
		runPingPong(ctx.params.warmupIter);
	}
	
	@Override
	protected void run(PerfParams params) {
		super.run(params);
		initBuffers();
		
		TcpConnection tcp = params.tcpConn;
		
		barrier(tcp);
		warmup();
		barrier(tcp);
		
		runPingPong(params.maxIter);
		barrier(tcp);
	}
	
	@Override
	protected void connect() {
		UcpObjects ucp = ctx.ucpObj;
		PerfParams params = ctx.params;
		TcpConnection tcp = params.tcpConn;
		
		WorkerAddress myAddr = ucp.worker.getAddress();
		
		try {
			tcp.write(myAddr);
			WorkerAddress remote = (WorkerAddress) tcp.read();
			ucp.setEndPoint(remote);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
	}
	
	private void initBuffers() {
		PerfParams params = ctx.params;
		int s = params.size;
		sendBuff = ByteBuffer.allocateDirect(s);
		recvBuff = ByteBuffer.allocateDirect(s);
		
		String msg = StringUtils.generateRandomString(s);
		sendBuff.put(msg.getBytes());
	}
	
	private void barrier(TcpConnection tcp) {
		tcp.barrier(this instanceof LatencyServer);
	}
}



