package org.ucx.jucx.tests.perftest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfTestType;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.TcpConnection;
import org.ucx.jucx.utils.Options;

public class Perftest {
	TestContext ctx;
	boolean isServer = true;
	
	private Perftest() {
		ctx = new TestContext();
	}

	protected void usage() {
		StringBuffer str = new StringBuffer();
		String sep = System.lineSeparator();
		str.append(sep + "Options:" + sep);
		str.append("\t-p <port>          TCP port (default: 12345)" + sep);
		str.append("\t-n <iterations>    number of iterations (default: 1000000)" + sep);
		str.append("\t-s <size>          msg size in bytes (default: 64)" + sep);
		str.append("\t-w <iterations>    number if warmup iterations (default: 10000; max.: iterations/10)" + sep);
		str.append("\t-v                 enable progress printing to System.out (default: no print)" + sep);
		str.append("\t-b                 bandwidth test (default: ping-pong)" + sep);
		str.append("\t-e <events>        size of shared event queue (default: 200)" + sep);
		str.append("\t-O <outstanding>   number of outstanding requests for bw test (default: 1); also implies -b" + sep);
		str.append("\t-f <filename>      write results to file <filename>" + sep);
		str.append("\t-T <time>          run a total of <time> seconds (default: 0.0 => unlimited)" + sep);
		str.append("\t-r <time>          report results every <time> seconds (default: 1.0)" + sep);
		str.append("\t-h                 display this help and exit" + sep);
		System.out.println(str.toString());
	}
	
	protected void parseArgs(String[] args) {
		Options options = new Options("w:f:p:n:s:vbe:O:hT:r:", args);
		char opt;
		PerfParams params = ctx.params;
		while ((opt = options.getOpt()) != Options.EOF) {
			String val = options.optArg;
			switch (opt) {
			case 'h':
				usage();
				System.exit(0);
				break;
				
			case 'T':
				params.maxTimeSecs		= Double.parseDouble(val);
				break;
				
			case 'r':
				params.reportInterval	= Double.parseDouble(val);
				break;
				
			case 'p':
				ctx.port 				= Integer.parseInt(val);
				break;
				
			case 'n':
				params.maxIter			= Integer.parseInt(val);
				break;
				
			case 's':
				params.size 			= Integer.parseInt(val);
				break;
				
			case 'v':
				params.print			= true;
				break;
				
			case 'w':
				params.warmupIter		= Integer.parseInt(val);
				break;
			
			case 'O':
				params.maxOutstanding	= Integer.parseInt(val);
			case 'b':
				params.testType			= PerfTestType.UCP_TEST_STREAM;
				break;
				
			case 'e':
				params.events 			= Integer.parseInt(val);
				break;
				
			case 'f':
				params.filename			= val;
				break;
				
			default:
				System.out.println("Invalid option. Exiting...");
				usage();
				System.exit(1);
				break;
			}
		}
		
		params.warmupIter = Math.min(params.warmupIter, params.maxIter/10);
		ctx.server = options.getNonOptionArgument();
		if (ctx.server != null)
			isServer = false;
	}
	
	private class TestContext {
		int 			port	= 12345;
		String 			server	= null;
		PerfParams 		params;
		
		private TestContext() {
			params = new PerfParams();
		}
	}
	
	private PerfParams setRTE() throws IOException {
		TcpConnection tcp;
		Socket sock;
		if (isServer) {
			ServerSocket servSock = new ServerSocket(ctx.port);
			servSock.setReuseAddress(true);
			
			System.out.println("Waiting for connections...");
			
			sock = servSock.accept();
			servSock.close();
			
			tcp = new TcpConnection(sock);
			
			ctx.params = (PerfParams) tcp.read();
		}
		else {
			sock = new Socket(ctx.server, ctx.port);
			tcp = new TcpConnection(sock);
			tcp.write(ctx.params);
		}
		
		System.out.println("Connected to: " + sock.getInetAddress().getHostAddress());
		
		ctx.params.tcpConn = tcp;
		
		return ctx.params;
	}
	
	private void printStartingMessage(PerfParams params) {
		if (params.testType == PerfTestType.UCP_TEST_STREAM)
			System.out.println("****   Bandwidth Test   ****");
		else
			System.out.println("*****   Latency Test   *****");
		
		System.out.println("# iterations: " + params.maxIter);
		System.out.println("Message size: " + params.size);
	}
	
	public static void main(String[] args) {
		Perftest test = new Perftest();
		test.parseArgs(args);
		PerfParams params = null;
		
		try {
			params = test.setRTE();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
		
		test.printStartingMessage(params);
		
		PerftestBase perf;
		
		switch (params.testType) {
		case UCP_TEST_PINGPONG:
			if (test.isServer) 	perf = new LatencyServer();
			else				perf = new LatencyClient();
			break;
			
		case UCP_TEST_STREAM:
			if (test.isServer) 	perf = new BandwidthServer();
			else 				perf = new BandwidthClient();
			break;

		default:
			perf = null;
			break;
		}
		
		perf.run(params);
		perf.close();
	}
}

