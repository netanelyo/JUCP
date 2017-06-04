package org.ucx.jucx.examples;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.ucx.jucx.Context;
import org.ucx.jucx.EndPoint;
import org.ucx.jucx.UCPParams;
import org.ucx.jucx.Utils;
import org.ucx.jucx.Worker;
import org.ucx.jucx.WorkerAddress;

public class UCPServer {
	
	private int 	port 	= 12345;
	private int 	iters 	= 1000;
	private int 	size	= 64;
	private long	tag		= 29592;
	private boolean print	= false;
	
	public class Callback implements Worker.Callbacks {
		
		public LinkedList<Long> requests = new LinkedList<>();
		
		@Override
		public void requestHandle(long requestId) {
			requests.add(requestId);
		}
	}

	public static void main(String[] args) {
		UCPServer server = new UCPServer();
		server.parseArgs(args);
		try {
			server.run();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.out.println("Exception in server");
		}
	}
	
	private static void usage() {
		StringBuffer str = new StringBuffer();
		String sep = System.lineSeparator();
		str.append("Usage: java org.ucx.jucx.examples.UCPServer [OPTION]..." + sep);
		str.append(sep + "Options:" + sep);
		str.append("\t-p port           port to listen on (default 12345)" + sep);
		str.append("\t-n iterations     number of iterations (default 1000)" + sep);
		str.append("\t-s size           msg size in bytes (default 64)" + sep);
		str.append("\t-t tag            request tag (default 29592)" + sep);
		str.append("\t-v                enable printing to System.out (default no print)" + sep);
//		str.append("\t-f file           file to print all messages to (default System.out)" + sep);
		str.append("\t-h                display this help and exit" + sep);
		System.out.println(str.toString());
	}
	
	private void parseArgs(String[] args) {
		Map<String, String> parameters = OptionsUtils.getOptionsMap(args);
		
		for (Entry<String, String> entry : parameters.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			switch (key) {
			case "-h":
				usage();
				System.exit(0);
				break;
				
			case "-p":
				port 	= Integer.parseInt(val);
				break;
				
			case "-n":
				iters 	= Integer.parseInt(val);
				break;
				
			case "-s":
				size 	= Integer.parseInt(val);
				break;
				
			case "-t":
				tag 	= Long.parseLong(val);
				break;
				
			case "-v":
				print 	= true;
				break;

			default:
				break;
			}
		}
	}

	private void run() throws Exception {
		
		System.out.println("Java UCP Hello World - Server");
		
		long feats 	= UCPParams.Features.UCP_FEATURE_TAG;
		long mask 	= UCPParams.FieldMask.UCP_PARAM_FIELD_FEATURES;

		UCPParams params = new UCPParams(feats, mask);
		Context ctx = Context.getInstance(params);
		Callback cb = new Callback();

		Worker worker = new Worker(ctx, cb);

		ServerSocket servSock = new ServerSocket(port);
		servSock.setReuseAddress(true);

		System.out.println("Waiting for connections....");
		Socket sock = servSock.accept();

		WorkerAddress workerAddr = worker.getAddress();

		ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
		outStream.writeObject(workerAddr);

		System.out.println("Sent UCP Address");
		
		ObjectInputStream inStream = new ObjectInputStream(sock.getInputStream());
		WorkerAddress remoteWorkerAddr = (WorkerAddress) inStream.readObject();

		EndPoint ep = new EndPoint(worker, remoteWorkerAddr);
		
		ByteBuffer in 	= ByteBuffer.allocateDirect(size);
		ByteBuffer out	= ByteBuffer.allocateDirect(size);
		out.put("Hello from UCPServer: ".getBytes());
		int pos = out.position();
		
		for (int i = 0; i < iters; i++) {
			worker.recvMessageAsync(tag, -1, in, size, 2*i);
			if (print)
				System.out.println(Utils.getByteBufferAsString(in));
			
			out.putInt(pos, i);
			ep.sendMessageAsync(tag, out, size, 2*i + 1);
		}
		
		worker.progress();
		System.out.println("Completed requests: " + cb.requests.size());

		servSock.close();
		sock.close();

		ep.free();
		worker.free();
	}

}
