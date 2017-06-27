package org.ucx.jucx.examples;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.ucx.jucx.EndPoint;
import org.ucx.jucx.Worker;
import org.ucx.jucx.WorkerAddress;
import org.ucx.jucx.examples.ExampleContext.BandwidthBuffer;
import org.ucx.jucx.examples.ExampleUtils.BandwidthCallback;
import org.ucx.jucx.examples.ExampleUtils.PingPongCallback;
import org.ucx.jucx.utils.Utils;

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
		
		WorkerAddress workerAddr = ucp.worker.getAddress();
		
		ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
		outStream.writeObject(workerAddr);
		
		System.out.println("Sent UCP Address");
		servSock.close();
		
		return sock;
	}
	
	@Override
	protected void exchWorkerAddressPP() throws Exception {
		Socket sock = sendWorkerAddress();
		
		WorkerAddress remoteWorkerAddr = null;
		ObjectInputStream inStream = new ObjectInputStream(sock.getInputStream());
		remoteWorkerAddr = (WorkerAddress) inStream.readObject();
		
		ucp.endPoint = ucp.worker.createEndPoint(remoteWorkerAddr);
		
		sock.close();
	}
	
	protected void runPingPong() {
		PingPongCallback cb = new PingPongCallback(2*iters);
		int pos = initPingPong(cb);
		
		Worker worker = ucp.worker;
		EndPoint ep = ucp.endPoint;
		
		ByteBuffer in 	= bufferPool.getInputBuffer();
		ByteBuffer out 	= bufferPool.getOutputBuffer();
		
		for (int i = 0; i < iters; i++) {
			worker.recvMessageAsync(tag, Worker.DEFAULT_TAG_MASK, in, size, 2*i);
			if (print)
				System.out.println(Utils.getByteBufferAsString(in));
			
			while (cb.last < 2*(i+1) - 1)
				worker.progress();
			
			out.putInt(pos, i);
			out.flip();
			ep.sendMessageAsync(tag, out, size, 2*i + 1);
			out.clear();
		}
		
		while (cb.last < 2*iters)
			worker.progress();
		System.out.println("Completed requests: " + cb.last);
	}
	
	@Override
	protected void exchWorkerAddressBW() throws Exception {
		Socket sock = sendWorkerAddress();
		sock.close();
	}
	
	@Override
	protected void runBandwidth() {
		bufferPool = new BandwidthBuffer(outstanding, true);
		BandwidthCallback cb = new BandwidthCallback((BandwidthBuffer) bufferPool, iters);
		initBandwidth(cb);
		
		bufferPool.setInputBuffer(size);
		Worker worker = ucp.worker;
		
		int reqs = 1;
		
		for (int i = 0; i < iters; i++) {
			ByteBuffer in = bufferPool.getInputBuffer();
			worker.recvMessageAsync(tag, Worker.DEFAULT_TAG_MASK, in, size, i);
			if (print)
				System.out.println(Utils.getByteBufferAsString(in));
			
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
	protected void init() {
		System.out.println("Java UCP Hello World - Server");
		super.init();
	}
	
}
