package org.ucx.jucx.tests.perftest;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.ucx.jucx.WorkerAddress;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.TcpConnection;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.UcpObjects;
import org.ucx.jucx.utils.Utils;

public abstract class LatencyTest extends PerftestBase {
	
	protected ByteBuffer sendBuff;
	protected ByteBuffer recvBuff;
	
	protected abstract void execute(int iters);
	
	@Override
	protected void run(PerfParams params) {
		ctx = new PerfContext(params);
		initBuffers();
		ctx.cb = new PerftestCallback();
		super.run(params);
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
		
		byte[] msg = Utils.generateRandomBytes(s);
		sendBuff.put(msg);
		sendBuff.rewind();
	}
}



