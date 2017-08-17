package org.ucx.jucx.examples.helloworld;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.ucx.jucx.EndPoint;
import org.ucx.jucx.WorkerAddress;

public class HelloClient extends HelloWorld {
	
	private String 		host 	= "127.0.0.1";
	private EndPoint 	ep 		= null;
	
	@Override
	protected void connect() throws IOException, ClassNotFoundException {
		Socket sock = new Socket(host, port);
		
		System.out.println("Connected to: " + sock.getInetAddress().getHostAddress());
		
		OutputStream outStream = sock.getOutputStream();
		ObjectInputStream inStream = new ObjectInputStream(sock.getInputStream());
		WorkerAddress addr = (WorkerAddress) inStream.readObject();
		
		ep = worker.createEndPoint(addr);
		
		outStream.write(0);
		
		sock.close();
	}
	
	@Override
	protected void usage() {
		System.out.println("Usage: ./runHelloWorld.sh client [<Host_IP_address>] [OPTION]...");
		System.out.println("Default Host_IP_address: 127.0.0.1");
		super.usage();
	}
	
	@Override
	protected void parseArgs(String[] args) {
		super.parseArgs(args);
		String h = options.getNonOptionArgument();
		if (h != null)
			host = h;
	}
	
	@Override
	protected void run() {
		try {
			connect();
		}
		catch (IOException e1) {
			System.out.println("IO error in client");
		}
		catch (ClassNotFoundException e1) {
			System.out.println("Class error in client");
		}
		
		buff.put(MESSAGE.getBytes());
		buff.rewind();
		ep.tagSendAsync(buff, buff.capacity());
	}
	
	public static void main(String[] args) {
		HelloClient client = new HelloClient();
		client.parseArgs(args);
		client.run();
		client.close();
	}
}
