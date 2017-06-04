package org.ucx.jucx.examples;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.ucx.jucx.UCPConstants;
import org.ucx.jucx.Context;
import org.ucx.jucx.EndPoint;
import org.ucx.jucx.UCPParams;
import org.ucx.jucx.Utils;
import org.ucx.jucx.TagMsg;
import org.ucx.jucx.Worker;
import org.ucx.jucx.WorkerAddress;

public class UCPClient {
	
	private String 	host	= "127.0.0.1";
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
		UCPClient client = new UCPClient();
		client.parseArgs(args);
		try {
			client.run();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			System.out.println("Exception in client");
		}

	}
	
	private static void usage() {
		StringBuffer str = new StringBuffer();
		String sep = System.lineSeparator();
		str.append("Usage: java org.ucx.jucx.examples.UCPClient <Host_IP_address> [OPTION]..." + sep);
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
		int whence = 0;
		if (args.length > 0 && !args[0].startsWith("-"))
		{
			host = args[0];
			whence = 1;
		}
		Map<String, String> parameters = OptionsUtils.getOptionsMap(args, whence);
		
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
		
		System.out.println("Java UCP Hello World - Client");
		
		long feats 	= UCPParams.Features.UCP_FEATURE_TAG;
		long mask 	= UCPParams.FieldMask.UCP_PARAM_FIELD_FEATURES;

		UCPParams params = new UCPParams(feats, mask);
		Context ctx = Context.getInstance(params);
		Callback cb = new Callback();

		Worker worker = new Worker(ctx, cb);
		System.out.println("Connecting....");
		Socket sock = new Socket(host, port);

		ObjectInputStream inStream = new ObjectInputStream(sock.getInputStream());
		WorkerAddress addr = (WorkerAddress)inStream.readObject();
		
		System.out.println("Received UCP Address");
		
		EndPoint ep = new EndPoint(worker, addr);
		
		ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
		outStream.writeObject(worker.getAddress());
		sock.close();
		
		System.out.println("Created UCP end point" + System.lineSeparator() + "Sending Message");
		
		ByteBuffer out 	= ByteBuffer.allocateDirect(size);
		ByteBuffer in 	= ByteBuffer.allocateDirect(size);
		out.put("Hello from UCPClient: ".getBytes());
		int pos = out.position();
		
		long start = System.nanoTime();
		
		for (int i = 0; i < iters; i++) {
			out.putInt(pos, i);
			ep.sendMessageAsync(tag, out, size, 2*i + 1);
			
			worker.recvMessageAsync(tag, -1, in, size, 2*i);
			if (print)
				System.out.println(Utils.getByteBufferAsString(in));
		}
		
		long end = System.nanoTime();
		
		System.out.println("1000 packets in " + (end - start)/1000000000.0 + " secs");
		System.out.println("Packet rate = " + 1000.0/(end - start));
		
		ep.free();
		worker.free();
		
	}

}

