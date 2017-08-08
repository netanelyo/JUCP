package org.ucx.jucx.examples;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.ucx.jucx.EndPoint;
import org.ucx.jucx.Worker;
import org.ucx.jucx.WorkerAddress;
import org.ucx.jucx.examples.ExampleContext.TcpConnection;
import org.ucx.jucx.examples.ExampleUtils.BandwidthCallback;
import org.ucx.jucx.examples.ExampleUtils.PingPongCallback;
import org.ucx.jucx.utils.StringUtils;

public class UCPServer extends UCPBase {
	
	public static void main(String[] args) {
		UCPServer server = new UCPServer();
		server.parseArgs(args);
		server.run();
	}
	
	@Override
	protected void usage() {
		System.out.println("Usage: ./runServer.sh [OPTION]...");
		super.usage();
	}
	
	private Socket sendWorkerAddress() throws Exception {
		ServerSocket servSock = new ServerSocket(port);
		servSock.setReuseAddress(true);
		
		System.out.println("Waiting for connections....");
		Socket sock = servSock.accept();
		servSock.close();
		
		WorkerAddress workerAddr = ucp.worker.getAddress();
		
		ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
		outStream.writeObject(workerAddr);
		
		System.out.println("Sent UCP Address");
		
		return sock;
	}
	
	@Override
	protected Socket exchWorkerAddress() throws Exception {
		return sendWorkerAddress();
	}
	
	@Override
	protected void exchWorkerAddressPP() throws Exception {
		super.exchWorkerAddressPP();
		
		Socket connSock = tcpConn.sock;
		
		WorkerAddress remoteWorkerAddr = null;
		ObjectInputStream inStream = new ObjectInputStream(connSock.getInputStream());
		remoteWorkerAddr = (WorkerAddress) inStream.readObject();
		
		ucp.endPoint = ucp.worker.createEndPoint(remoteWorkerAddr);
		tcpConn = new TcpConnection(connSock);
	}
	
	private void warmup(ByteBuffer in, ByteBuffer out) {
		PingPongCallback cb = callback;
		Worker worker = ucp.worker;
		EndPoint ep = ucp.endPoint;
		int events = 2;
		
		int warmupMsgSize = size - INT_SIZE;
		
		barrier();
		
		out.clear();
		cb.last = 1;
		for (int i = 0; i < warmup; i++) {
			worker.recvMessageAsync(tag, Worker.DEFAULT_TAG_MASK, in, warmupMsgSize, 0);
			
			while (cb.last < events)
				worker.progress();
			cb.last = 0;
			
			ep.sendMessageAsync(tag, out, warmupMsgSize, 1);
			
		}
		
		while (cb.last < events - 1)
			worker.progress();
		
		cb.last = 0;
		barrier();
	}
	
	private void warmup() {
		Worker worker = ucp.worker;
		BandwidthCallback cb = (BandwidthCallback) callback;
		
		barrier();
		
		for (int i = 0; i < warmup; i++) {
			ByteBuffer in = bufferPool.getInputBuffer();
			worker.recvMessageAsync(tag, Worker.DEFAULT_TAG_MASK, in, size, i);
			
			while (!cb.isReady())
				worker.progress();
		}
		
		while (cb.last < warmup)
			worker.progress();
		
		cb.last = 0;
		barrier();
	}
	
	
	protected void runPingPong() {
		super.runPingPong();
		PingPongCallback cb = callback;
		
		Worker worker = ucp.worker;
		EndPoint ep = ucp.endPoint;
		
		ByteBuffer in 	= bufferPool.getInputBuffer();
		ByteBuffer out 	= bufferPool.getOutputBuffer();
		int pos = out.position();

		warmup(in, out);

		out.clear();
		long req = 0;
		for (int i = 0; i < iters; i++) {
			worker.recvMessageAsync(tag, Worker.DEFAULT_TAG_MASK, in, size, req);
//			if (print)
//				System.out.println(Utils.getByteBufferAsString(in));
			
			long done = req + 1;
			while (cb.last < done)
				worker.progress();
			
			// TODO: Move to cb
//			out.putInt(pos, i);
			ep.sendMessageAsync(tag, out, size, req + 1);
			
			req += 2;
		}
		
		finish();
	}
	
	@Override
	protected void exchWorkerAddressBW() throws Exception {
		super.exchWorkerAddressBW();
	}
	
	@Override
	protected void runBandwidth() {
		super.runBandwidth();
		BandwidthCallback cb = (BandwidthCallback) callback;
		
		bufferPool.setInputBuffer(size);
		Worker worker = ucp.worker;
		
		warmup();
		
		for (int i = 0; i < iters; i++) {
			ByteBuffer in = bufferPool.getInputBuffer();
			worker.recvMessageAsync(tag, Worker.DEFAULT_TAG_MASK, in, size, i);
			if (print)
				System.out.println(StringUtils.getByteBufferAsString(in));
			
			while (!cb.isReady())
				worker.progress();
		}
		
		finish();
	}
	
	private void finish() {
		int factor = pingPong ? 2 : 1;
		while (callback.last < factor*iters)
			ucp.worker.progress();
		System.out.println("Completed requests: " + callback.last);
	}

	@Override
	protected void init() {
		System.out.println("Java UCP Hello World - Server");
		super.init();
	}
	
}
