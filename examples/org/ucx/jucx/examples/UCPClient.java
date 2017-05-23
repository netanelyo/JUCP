package org.ucx.jucx.examples;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.ucx.jucx.UCPConstants;
import org.ucx.jucx.Context;
import org.ucx.jucx.EndPoint;
import org.ucx.jucx.UCPParams;
import org.ucx.jucx.TagMsg;
import org.ucx.jucx.Worker;
import org.ucx.jucx.WorkerAddress;

public class UCPClient {

	public static void main(String[] args) {
		try {
			runClient();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.out.println("Exception in client");
		}

	}

	private static void runClient() throws Exception {
		
		System.out.println("Java UCP Hello World - Client");
		
		long feats = UCPConstants.UCPFeature.UCP_FEATURE_TAG;
		long mask = UCPConstants.UCPParamsField.UCP_PARAM_FIELD_FEATURES;

		UCPParams params = new UCPParams(feats, 0, mask);
		Context ctx = Context.getInstance(params);

		Worker worker = Worker.getInstance(ctx);
		
		System.out.println("Connecting....");
		Socket sock = new Socket("localhost", 12345);

		ObjectInputStream inStream = new ObjectInputStream(sock.getInputStream());
		WorkerAddress addr = (WorkerAddress)inStream.readObject();
		
		System.out.println("Received UCP Address");
		
		EndPoint ep = EndPoint.getInstance(worker, addr);
		
		ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
		outStream.writeObject(worker.getAddress());
		sock.close();
		
		System.out.println("Created UCP end point" + System.lineSeparator() + "Sending Message");
		
		ByteBuffer out = ByteBuffer.allocateDirect(100);
		long tag = Long.parseUnsignedLong("a2b9c5d9e2f", 16);
		TagMsg msg;
		
		long start = System.nanoTime();
		
		for (int i = 0; i < 1000; i++) {
			out.putInt(i);
			ep.sendMessage(out, tag);
			if (!out.hasRemaining())
				out.rewind();
			
			msg = worker.recvMessage(tag);
		}
		
		long end = System.nanoTime();
		
		System.out.println("1000 packets in " + (end - start)/1000000000.0 + " secs");
		System.out.println("Packet rate = " + 1000.0/(end - start));
		
		ep.free();
		worker.free();
		
	}

}

