package org.ucx.jucx.examples;
import java.io.ObjectInputStream;
import java.net.Socket;

import org.ucx.jucx.UCPConstants;
import org.ucx.jucx.UCPContext;
import org.ucx.jucx.UCPEndPoint;
import org.ucx.jucx.UCPParams;
import org.ucx.jucx.UCPWorker;
import org.ucx.jucx.UCPWorkerAddress;

public class UCPClient {

	public static void main(String[] args) {
		try {
			runClient();
		} catch (Exception e) {
			System.out.println("Exception in client");
		}

	}

	private static void runClient() throws Exception {
		
		System.out.println("Java UCP Hello World - Client");
		
		long feats = UCPConstants.UCPFeature.UCP_FEATURE_TAG;
		long mask = UCPConstants.UCPParamsField.UCP_PARAM_FIELD_FEATURES;

		UCPParams params = new UCPParams(feats, 0, mask);
		UCPContext ctx = UCPContext.getInstance(params);

		UCPWorker worker = UCPWorker.getInstance(ctx);
		
		System.out.println("Connecting....");
		Socket sock = new Socket("localhost", 12345);

		ObjectInputStream inStream = new ObjectInputStream(sock.getInputStream());
		UCPWorkerAddress addr = (UCPWorkerAddress)inStream.readObject();
		sock.close();
		
		System.out.println("Received UCP Address");
		
		UCPEndPoint ep = UCPEndPoint.getInstance(worker, addr);

		System.out.println("Created UCP end point" + System.lineSeparator() + "Sending Message");
		
		byte[] msg = "Hello from Java client".getBytes();

		ep.sendMessage(msg, Long.parseUnsignedLong("a2b9c5d9e2f", 16));
		
		
		ep.free();
		worker.free();
		
	}

}

