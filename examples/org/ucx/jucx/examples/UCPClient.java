package org.ucx.jucx.examples;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.stream.LongStream;

import org.ucx.jucx.EndPoint;
import org.ucx.jucx.Worker;
import org.ucx.jucx.WorkerAddress;
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
	
	@Override
	protected void exchWorkerAddressBW() throws Exception {
		super.exchWorkerAddressBW();
	}
	
	@Override
	protected Socket exchWorkerAddress() throws Exception {
		return recvWorkerAddress();
	}
	
	
	private void warmup(ByteBuffer in, ByteBuffer out) {
		PingPongCallback cb = callback;
		Worker worker = ucp.worker;
		EndPoint ep = ucp.endPoint;;
		events = 2;
		
		int warmupMsgSize = size - INT_SIZE;
		
		barrier();
		
		out.clear();
		for (int i = 0; i < warmup; i++) {
			ep.sendMessageAsync(tag, out, warmupMsgSize, 0);
			worker.recvMessageAsync(tag, Worker.DEFAULT_TAG_MASK, in, warmupMsgSize, 1);
			
			while (cb.last < events)
				worker.progress();
			cb.last = 0;
		}
		
		barrier();
	}
	
	private void warmup() {
		BandwidthCallback cb = (BandwidthCallback) callback;
		Worker worker = ucp.worker;
		EndPoint ep = ucp.endPoint;;
		
		int warmupMsgSize = size - INT_SIZE;
		
		barrier();
		for (int i = 0; i < warmup; i++) {
			ByteBuffer out = bufferPool.getOutputBuffer();
			out.clear();
			ep.sendMessageAsync(tag, out, warmupMsgSize,i);
			
			while (!cb.isReady())
				worker.progress();
		}
		
		cb.last = 0;
		barrier();
	}
	
	@Override
	protected void runBandwidth() {
		super.runBandwidth();
		BandwidthCallback cb = (BandwidthCallback) callback;
		
		String msg = ExampleUtils.generateRandomString(size - 6) + ": ";
		int pos = bufferPool.setOutputBuffer(size, msg);
		EndPoint ep = ucp.endPoint;
		Worker worker = ucp.worker;
		
		warmup();
		
		long[] times = new long[iters];
		long t1, t2;
		
		for (int i = 0; i < iters; i++) {
			t1 = Time.nanoTime();
			
			ByteBuffer out = bufferPool.getOutputBuffer();
			out.clear();
			out.putInt(pos, i);
			ep.sendMessageAsync(tag, out, size, i);
			
			while (!cb.isReady())
				worker.progress();
			
			t2 = Time.nanoTime();
			
			times[i] = t2 - t1;
		}
		
		while (cb.last < iters)
			worker.progress();
		
		System.out.println("\nBW Test Results:");
		printResults(times);
	}
	
	@Override
	protected void exchWorkerAddressPP() throws Exception {
		super.exchWorkerAddressPP();
		
		Socket connSock = tcpConn.sock;
		
		ObjectOutputStream outStream = new ObjectOutputStream(connSock.getOutputStream());
		outStream.writeObject(ucp.worker.getAddress());
	}
	
	@Override
	protected void runPingPong() {
		super.runPingPong();
		PingPongCallback cb = callback;
		
		Worker worker = ucp.worker;
		EndPoint ep = ucp.endPoint;
		
		ByteBuffer in 	= bufferPool.getInputBuffer();
		ByteBuffer out 	= bufferPool.getOutputBuffer();
		int pos = out.position();
		
		warmup(in, out);
		
		long[] times = new long[iters];
		long t1, t2;
		
		out.clear();
		long req = 0;
		for (int i = 0; i < iters; i++) {
			out.putInt(pos, i);
			
			t1 = Time.nanoTime();
			
			ep.sendMessageAsync(tag, out, size, req);

			worker.recvMessageAsync(tag, Worker.DEFAULT_TAG_MASK, in, size, req + 1);
//			if (print)
//				System.out.println(Utils.getByteBufferAsString(in));

			long done = req + 2;
			while (cb.last < done)
				worker.progress();
			
			t2 = Time.nanoTime();
			
			times[i] = t2 - t1;
			
			req += 2;
		}
	
		System.out.println("\nLatency Test Results:");
		printResults(true, times);
	}
	
	@Override
	protected void init() {
		System.out.println("Java UCP Hello World - Client");
		super.init();
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

	private String printable(double toPrint) {
		return new DecimalFormat("#0.000").format(toPrint);
	}
	
	private void printResults(boolean latency, long[] results) {
		int factor = 1;
		if (latency)
			factor = 2;
		long total = LongStream.of(results).sum();
		double[] percentile = { 0.99999, 0.9999, 0.999, 0.99, 0.90, 0.50 };
		
		if (fileName != null)
			printToFile(results);
		
		Arrays.sort(results);
		String format = "%-25s = %-10s";
		System.out.println(String.format(format, "---> <MAX> observation", printable(Time.toUsecs(results[results.length - 1]) / factor)));
		for (double per : percentile) {
			int index = (int)(0.5 + per*iters) - 1;
			System.out.println(String.format(format, "---> percentile " + per, printable(Time.toUsecs(results[index]) / factor)));
		}
		System.out.println(String.format(format, "---> <MIN> observation", printable(Time.toUsecs(results[0]) / factor)));
		
		System.out.println();
		
		double secs = Time.toSecs(total);
		double totalMBytes = (double)size * iters / Math.pow(2, 20);
		System.out.println("average latency (usec): " + printable(Time.toUsecs(total) / iters / factor));
		System.out.println("message rate (msg/s): " + (int)(iters/secs));
		System.out.println("bandwidth (MB/s) : " + printable(totalMBytes/secs));
	}
	
	private void printResults(long[] results) {
		printResults(false, results);
	}
	
	private void printToFile(long[] results) {
		try {
			FileWriter f = new FileWriter("../examples/" + fileName);
			BufferedWriter bw = new BufferedWriter(f);
			for (long l : results) {
				bw.write(Long.toString(l));
				bw.newLine();
			}
			bw.close();
		}
		catch (Exception e) {
			System.out.println("Writing to file failed");
		}
	}

}

