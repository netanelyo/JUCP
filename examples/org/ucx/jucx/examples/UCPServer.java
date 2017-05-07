package org.ucx.jucx.examples;

import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.ucx.jucx.Bridge;
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
		// FileWriter fw = new FileWriter(new File("server_output"));

		long feats = UCPConstants.UCPFeature.UCP_FEATURE_TAG;
		long mask = UCPConstants.UCPParamsField.UCP_PARAM_FIELD_FEATURES;

		System.out.println("hello");

		UCPParams params = new UCPParams(feats, 0, mask);
		UCPContext ctx = UCPContext.getInstance(params);

		UCPWorker worker = UCPWorker.getInstance(ctx);
		byte[] addr = worker.getAddress().getWorkerAddr();
		long len = addr.length;

		System.out.println("In Java: Length = " + len);
		// fw.flush();

		ServerSocket servSock = new ServerSocket(12345);
		servSock.setReuseAddress(true);

		System.out.println("Waiting for connections....");
		// fw.flush();
		Socket sock = servSock.accept();

		UCPWorkerAddress workerAddr = worker.getAddress();

		ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
		outStream.writeObject(workerAddr);
		Thread.sleep(1000);

		System.out.println("Sent UCP Address");
		// fw.flush();

		UCPTagMsg msg = worker.recvMessage(Long.parseUnsignedLong("a2b9c5d9e2f", 16));
		// System.out.println("In Java: Msg size = " + msg.getMsgSize());
		System.out.println("In Java: " + msg.getInAsString());
		// fw.close();

		servSock.close();
		sock.close();

		Bridge.releaseWorker(worker);
	}

}
