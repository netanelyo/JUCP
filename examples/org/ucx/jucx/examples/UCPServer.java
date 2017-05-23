package org.ucx.jucx.examples;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.ucx.jucx.UCPConstants;
import org.ucx.jucx.Context;
import org.ucx.jucx.EndPoint;
import org.ucx.jucx.UCPParams;
import org.ucx.jucx.TagMsg;
import org.ucx.jucx.Worker;
import org.ucx.jucx.WorkerAddress;

public class UCPServer {

	public static void main(String[] args) {
		try {
			runServer();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.out.println("Exception in server");
		}
	}

	private static void runServer() throws Exception {
		
		System.out.println("Java UCP Hello World - Server");

		long feats = UCPConstants.UCPFeature.UCP_FEATURE_TAG;
		long mask = UCPConstants.UCPParamsField.UCP_PARAM_FIELD_FEATURES;

		UCPParams params = new UCPParams(feats, 0, mask);
		Context ctx = Context.getInstance(params);

		Worker worker = Worker.getInstance(ctx);

		ServerSocket servSock = new ServerSocket(12345);
		servSock.setReuseAddress(true);

		System.out.println("Waiting for connections....");
		Socket sock = servSock.accept();

		WorkerAddress workerAddr = worker.getAddress();

		ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
		outStream.writeObject(workerAddr);
		Thread.sleep(1000);

		System.out.println("Sent UCP Address");
		
		ObjectInputStream inStream = new ObjectInputStream(sock.getInputStream());
		WorkerAddress remoteWorkerAddr = (WorkerAddress) inStream.readObject();

		EndPoint ep = EndPoint.getInstance(worker, remoteWorkerAddr);
		
		ByteBuffer out = ByteBuffer.allocateDirect(100);
		long tag = Long.parseUnsignedLong("a2b9c5d9e2f", 16);
		TagMsg msg;
		
		for (int i = 0; i < 1000; i++) {
			msg = worker.recvMessage(tag);
			
			out.putInt(i);
			ep.sendMessage(out, tag);
			if (!out.hasRemaining())
				out.rewind();
		}

		servSock.close();
		sock.close();

		worker.free();
	}

}
