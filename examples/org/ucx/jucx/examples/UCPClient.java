package org.ucx.jucx.examples;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.ucx.jucx.EndPoint;
import org.ucx.jucx.Worker;
import org.ucx.jucx.WorkerAddress;
import org.ucx.jucx.examples.ExampleContext.BandwidthBuffer;
import org.ucx.jucx.examples.ExampleUtils.BandwidthCallback;
import org.ucx.jucx.examples.ExampleUtils.PingPongCallback;
import org.ucx.jucx.utils.Utils;
import org.ucx.jucx.utils.Utils.Time;

public class UCPClient extends UCPBase {
	
	private String host = "127.0.0.1";
	
	public static void main(String[] args) {
		UCPClient client = new UCPClient();
		client.parseArgs(args);
		client.run();
	}
	
	@Override
	protected void usage() {
		System.out.println("Usage: ./runClient.sh <Host_IP_address> [OPTION]...");
		super.usage();
	}
	
	@Override
	protected void parseArgs(String[] args) {
		if (args.length > 0 && !args[0].startsWith("-"))
		{
			host = args[0];
		}
		super.parseArgs(args);
	}
	
	private Socket recvWorkerAddress() throws Exception {
		System.out.println("Connecting....");
		Socket sock = new Socket(host, port);

		ObjectInputStream inStream = new ObjectInputStream(sock.getInputStream());
		WorkerAddress addr = (WorkerAddress)inStream.readObject();
		
		System.out.println("Received UCP Address");
		
		ucp.endPoint = ucp.worker.createEndPoint(addr);
		
		return sock;
	}
	
	@Override
	protected void exchWorkerAddressBW() throws Exception {
		Socket sock = recvWorkerAddress();
		sock.close();
	}
	
	@Override
	protected void runBandwidth() {
		bufferPool = new BandwidthBuffer(outstanding, false);
		BandwidthCallback cb = new BandwidthCallback((BandwidthBuffer) bufferPool, iters);
		initBandwidth(cb);
		
		String msg = ExampleUtils.generateRandomString(size - 6) + ": ";
		int pos = bufferPool.setOutputBuffer(size, msg);
		EndPoint ep = ucp.endPoint;
		Worker worker = ucp.worker;
		
		long[] times = new long[iters];
		int reqs = 1;
		
		for (int i = 0; i < iters; i++) {
			times[i] = Time.nanoTime();
			
			ByteBuffer out = bufferPool.getOutputBuffer();
			out.putInt(pos, i);
			out.flip();
			ep.sendMessageAsync(tag, out, size, 2*i);
			out.clear();
			
			if (reqs >= outstanding) {
				reqs = 1;
				
				while (!cb.ready())
					worker.progress();
			}
			else {
				reqs++;
			}
		}
	}
	
	@Override
	protected void exchWorkerAddressPP() throws Exception {
		Socket sock = recvWorkerAddress();
		
		ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
		outStream.writeObject(ucp.worker.getAddress());
		
		sock.close();		
	}
	
	@Override
	protected void runPingPong() {
		PingPongCallback cb = new PingPongCallback(2*iters);
		int pos = initPingPong(cb);
		
		Worker worker = ucp.worker;
		EndPoint ep = ucp.endPoint;
		
		ByteBuffer in 	= bufferPool.getInputBuffer();
		ByteBuffer out 	= bufferPool.getOutputBuffer();
		
		long[] times = new long[iters];
		long t1, t2;
		
		for (int i = 0; i < iters; i++) {
			t1 = Time.nanoTime();
			
			out.putInt(pos, i);
			out.flip();
			ep.sendMessageAsync(tag, out, size, 2*i);
			out.clear();

			worker.recvMessageAsync(tag, Worker.DEFAULT_TAG_MASK, in, size, 2*i + 1);
			if (print)
				System.out.println(Utils.getByteBufferAsString(in));
			
			while (cb.last < 2*(i+1))
				worker.progress();
			
			t2 = Time.nanoTime();
			
			times[i] = t2 - t1;
		}
		
		while (cb.last < 2*iters)
			worker.progress();
	
		
//		double secs = timeInSecs(total);
//		System.out.println("average latency (usec): " + new DecimalFormat("#0.000").format(timeInUsecs(total)/(2*iters)));
//		System.out.println("message rate (msg/s): " + Math.round(2*iters/secs));
	}
	
	@Override
	protected void init() {
		System.out.println("Java UCP Hello World - Client");
		super.init();
	}


}

