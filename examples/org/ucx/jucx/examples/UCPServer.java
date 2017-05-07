package org.ucx.jucx.examples;

import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.ucx.jucx.UCPConstants;
import org.ucx.jucx.UCPContext;
import org.ucx.jucx.UCPParams;
import org.ucx.jucx.UCPTagMsg;
import org.ucx.jucx.UCPWorker;
import org.ucx.jucx.UCPWorkerAddress;

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
		UCPContext ctx = UCPContext.getInstance(params);

		UCPWorker worker = UCPWorker.getInstance(ctx);

		ServerSocket servSock = new ServerSocket(12345);
		servSock.setReuseAddress(true);

		System.out.println("Waiting for connections....");
		Socket sock = servSock.accept();

		UCPWorkerAddress workerAddr = worker.getAddress();

		ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
		outStream.writeObject(workerAddr);
		Thread.sleep(1000);

		System.out.println("Sent UCP Address");

		UCPTagMsg msg = worker.recvMessage(Long.parseUnsignedLong("a2b9c5d9e2f", 16));
		System.out.println("In Java: " + msg.getMsgAsString());

		servSock.close();
		sock.close();

		worker.free();
	}

}
