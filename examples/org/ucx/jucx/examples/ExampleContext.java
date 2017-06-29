package org.ucx.jucx.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.ucx.jucx.Context;
import org.ucx.jucx.EndPoint;
import org.ucx.jucx.Worker;

public class ExampleContext {
	
	static interface BufferPool {
		
		public ByteBuffer getInputBuffer();
		
		public ByteBuffer getOutputBuffer();
		
		public int setOutputBuffer(int size, String msg);
		
		public void setInputBuffer(int size);
		
	}
	
	static class PingPongBuffer implements BufferPool {
		private ByteBuffer in = null;
		private ByteBuffer out = null;
		
		@Override
		public ByteBuffer getInputBuffer() {
			return in;
		}
		
		@Override
		public ByteBuffer getOutputBuffer() {
			return out;
		}
		
		@Override
		public int setOutputBuffer(int size, String msg) {
			out = ByteBuffer.allocateDirect(size);
			out.put(msg.getBytes());
			return out.position();
		}
		
		@Override
		public void setInputBuffer(int size) {
			in = ByteBuffer.allocateDirect(size);
		}
	}
	
	static class BandwidthBuffer implements BufferPool {
		private List<ByteBuffer> inBuffers = null;
		private List<ByteBuffer> outBuffers = null;
		private int position;
		private int bound;
		private int free;
		
		public BandwidthBuffer(int capacity, boolean isServer) {
			if (isServer)
				inBuffers = new ArrayList<>(capacity);
			else
				outBuffers = new ArrayList<>(capacity);
			bound = capacity;
			free = capacity;
			position = 0;
		}
		
		private int next() {
			if (--free < 0)
				return -1;
			return increment();
		}
		
		private int increment() {
			int next = position;
			position = (position + 1) % bound;
			return next;
		}
		
		boolean hasFree() {
			return free > 0;
		}
		
		void freeOne() {
			free++;
		}
		
		@Override
		public ByteBuffer getInputBuffer() {
			ByteBuffer in = null;
			if (inBuffers != null)
				in = inBuffers.get(next());
			return in;
		}
		
		@Override
		public ByteBuffer getOutputBuffer() {
			ByteBuffer out = null;
			if (outBuffers != null)
				out = outBuffers.get(next());
			return out;
		}
		
		@Override
		public int setOutputBuffer(int size, String msg) {
			if (outBuffers == null)
				return -1;
			
			ByteBuffer out;
			for (int i = 0; i < bound; i++) {
				out = ByteBuffer.allocateDirect(size);
				out.put(msg.getBytes());
				outBuffers.add(out);
			}
			
			return outBuffers.get(0).position();
		}
		
		@Override
		public void setInputBuffer(int size) {
			if (inBuffers == null)
				return;
			
			for (int i = 0; i < bound; i++) {
				inBuffers.add(ByteBuffer.allocateDirect(size));
			}
		}
	}
	
	static class UcpObjects {
		public Context ctx = null;
		public Worker worker = null;
		public EndPoint endPoint = null;
		
		public UcpObjects(Context ctx, Worker worker, EndPoint ep) {
			this(ctx, worker);
			endPoint = ep;
		}
		
		public UcpObjects(Context ctx, Worker worker) {
			this.ctx = ctx;
			this.worker = worker;
		}
		
		public UcpObjects(Context ctx) {
			this.ctx = ctx;
		}
	}
	
	static class TcpConnection {
		public Socket sock;
		private InputStream inStream;
		private OutputStream outStream;
		
		public TcpConnection(Socket socket) {
			sock = socket;
			try {
				inStream = sock.getInputStream();
				outStream = sock.getOutputStream();
			} catch (IOException e) {
				System.out.println("TcpConnection");
				e.printStackTrace();
			}
		}
		
		public void barrier(boolean server) throws IOException {
			if (server)
				serverBarrier();
			else
				clientBarrier();
		}
		
		private void clientBarrier() throws IOException {
			int x = 0;
			outStream.write(x);
			x = inStream.read();
		}

		private void serverBarrier() throws IOException {
			int x;
			x = inStream.read();
			outStream.write(x);
		}
	}
}






