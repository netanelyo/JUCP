package org.ucx.jucx.examples.helloworld;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.ucx.jucx.utils.Utils;

public class HelloServer extends HelloWorld {
	
	@Override
	protected void connect() throws IOException {
		ServerSocket serv = new ServerSocket(port);
		serv.setReuseAddress(true);
		
		System.out.println("Waiting for connections...");
		Socket sock = serv.accept();
		serv.close();
		System.out.println("Connected to: " + sock.getInetAddress().getHostAddress());
		
		InputStream inStream = sock.getInputStream();
		ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
		outStream.writeObject(worker.getAddress());
		outStream.flush();
		
		inStream.read();
		
		sock.close();
	}
	
	@Override
	protected void usage() {
		System.out.println("Usage: ./runHelloWorld.sh server [OPTION]...");
		super.usage();
	}
	
	@Override
	protected void run() {
		try {
			connect();
		}
		catch (IOException e) {
			System.out.println("Error in server");
		}
		
		worker.tagRecvAsync(buff, buff.capacity());
		worker.wait(1);
		
		System.out.println("Received:");
		System.out.println(Utils.getByteBufferAsString(buff));
	}
	
	public static void main(String[] args) {
		HelloServer server = new HelloServer();
		server.parseArgs(args);
		server.run();
		server.close();
	}
}
