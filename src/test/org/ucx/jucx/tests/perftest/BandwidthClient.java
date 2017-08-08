package org.ucx.jucx.tests.perftest;

import java.io.IOException;

import org.ucx.jucx.WorkerAddress;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.TcpConnection;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.UcpObjects;

public class BandwidthClient extends BandwidthTest {

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
