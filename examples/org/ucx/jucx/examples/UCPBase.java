package org.ucx.jucx.examples;

import org.ucx.jucx.Context;
import org.ucx.jucx.UCPParams;
import org.ucx.jucx.Worker;
import org.ucx.jucx.examples.ExampleContext.BufferPool;
import org.ucx.jucx.examples.ExampleContext.PingPongBuffer;
import org.ucx.jucx.examples.ExampleContext.UcpObjects;
import org.ucx.jucx.examples.ExampleUtils.BandwidthCallback;
import org.ucx.jucx.examples.ExampleUtils.PingPongCallback;
import org.ucx.jucx.utils.Utils.Options;

public abstract class UCPBase {
	
	protected int 		port 		= 12345;
	protected int 		iters 		= 100000;
	protected int 		size		= 64;
	protected long		tag			= 29592;
	protected boolean 	print		= false;
	protected boolean	pingPong	= true;
	protected int		outstanding = 1;
	protected int 		events 		= 200;
	
	protected UcpObjects ucp		= null;
	protected BufferPool bufferPool = null;
	
	protected abstract void runBandwidth();
	protected abstract void runPingPong();
	protected abstract void exchWorkerAddressPP() throws Exception;
	protected abstract void exchWorkerAddressBW() throws Exception;
	
	protected void exceptionHandler(Exception exp) {
		System.out.println("Exception in server");
		exp.printStackTrace();
		System.exit(1);
	}
	
	protected void init() {
		long feats 	= UCPParams.Features.UCP_FEATURE_TAG;
		long mask 	= UCPParams.FieldMask.UCP_PARAM_FIELD_FEATURES;

		UCPParams params = new UCPParams(feats, mask);
		Context ctx = Context.getInstance(params);
		
		ucp = new UcpObjects(ctx);
	}
	
	protected void run() {
		init();
		if (pingPong)
			runPingPong();
		else
			runBandwidth();
		
		ucp.worker.close();
	}

	protected int initPingPong(PingPongCallback cb) {
		try {
			connect(cb);
		}
		catch (Exception e) {
			ucp.worker.close();
			ucp.ctx.close();
			exceptionHandler(e);
		}
		
		bufferPool = new PingPongBuffer();
		bufferPool.setInputBuffer(size);
		String msg = ExampleUtils.generateRandomString(size - 6) + ": "; //TODO
		int pos = bufferPool.setOutputBuffer(size, msg);
		
		System.out.println("length = " + msg.getBytes().length); //TODO
		return pos;
	}
	
	protected void initBandwidth(BandwidthCallback cb) {
		try {
			connect(cb);
		}
		catch (Exception e) {
			ucp.worker.close();
			ucp.ctx.close();
			exceptionHandler(e);
		}
	}
	
	protected void usage() {
		StringBuffer str = new StringBuffer();
		String sep = System.lineSeparator();
		str.append(sep + "Options:" + sep);
		str.append("\t-p <port>          port to listen on (default: 12345)" + sep);
		str.append("\t-n <iterations>    number of iterations (default: 1000)" + sep);
		str.append("\t-s <size>          msg size in bytes (default: 64)" + sep);
		str.append("\t-t <tag>           request tag (default: 29592)" + sep);
		str.append("\t-v                 enable printing to System.out (default: no print)" + sep);
		str.append("\t-b                 bandwidth test (default: ping-pong)" + sep);
		str.append("\t-e <events>        size of shared event queue (default: 200)" + sep);
		str.append("\t-r <requests>      number of outstanding requests for bw test (default: 1); also implies -b" + sep);
		str.append("\t-h                 display this help and exit" + sep);
		System.out.println(str.toString());
	}
	
	protected void parseArgs(String[] args) {
		Options options = new Options("p:n:s:t:vbe:r:h", args);
		char opt;
		while ((opt = options.getOpt()) != Options.EOF) {
			String val = options.optArg;
			switch (opt) {
			case 'h':
				usage();
				System.exit(0);
				break;
				
			case 'p':
				port 		= Integer.parseInt(val);
				break;
				
			case 'n':
				iters 		= Integer.parseInt(val);
				break;
				
			case 's':
				size 		= Integer.parseInt(val);
				break;
				
			case 't':
				tag 		= Long.parseLong(val);
				break;
				
			case 'v':
				print 		= true;
				break;
				
			case 'r':
				outstanding = Integer.parseInt(val);
			case 'b':
				pingPong	= false;
				break;
				
			case 'e':
				events 		= Integer.parseInt(val);
				break;
				
			default:
				System.out.println("Invalid option. Exiting...");
				usage();
				System.exit(1);
				break;
			
			}
		}
	}
	
	private void connect(PingPongCallback cb) throws Exception {
		ucp.worker = new Worker(ucp.ctx, cb, events);
		exchWorkerAddressPP();
	}
	
	private void connect(BandwidthCallback cb) throws Exception {
		ucp.worker = new Worker(ucp.ctx, cb, events);
		exchWorkerAddressBW();
	}
}
