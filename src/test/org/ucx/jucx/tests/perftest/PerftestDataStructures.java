package org.ucx.jucx.tests.perftest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

import org.ucx.jucx.Context;
import org.ucx.jucx.EndPoint;
import org.ucx.jucx.Worker;
import org.ucx.jucx.Worker.Callbacks;
import org.ucx.jucx.WorkerAddress;
import org.ucx.jucx.utils.Time;

public class PerftestDataStructures {

	public static enum PerfTestType {
		UCP_TEST_PINGPONG,	// for latency test
		UCP_TEST_STREAM		// for BW test
	}
	
	public static enum PerfDataType {
		UCP_DATATYPE_CONTIG,
		UCP_DATATYPE_IOV,
	}
	
	public static class PerfParams implements Serializable {
		private static final long serialVersionUID = 1L;
		
		PerfTestType 	testType; 		// Test communication type
		int 			maxOutstanding;	// Maximal number of outstanding sends
		int 			warmupIter;		// Number of warm-up iterations
		int 			maxIter;		// Iterations limit, 0 - unlimited
		double 			maxTimeSecs;	// Time limit (seconds), 0 - unlimited
		int 			size;			// Message size
		double 			reportInterval;	// Interval at which to call the report callback
		PerfDataType	sendType;		
		PerfDataType	recvType;
		UcpObjects 		ucp = null;
		String 			filename = null;
		TcpConnection	tcpConn = null;
		int 			events;
		boolean			print;
		
		PerfParams() {
			testType = PerfTestType.UCP_TEST_PINGPONG;
			maxOutstanding = 1;
			warmupIter = 10000;
			maxIter = 1000000;
			size = 64;
			events = 200;
			sendType = PerfDataType.UCP_DATATYPE_CONTIG;
			recvType = PerfDataType.UCP_DATATYPE_CONTIG;
			maxTimeSecs = 0.0;
			reportInterval = 1.0;
			print = false;
		}
	}
	
	public static class PerfMeasurements {
		// Time
		long startTime;
		long prevTime;
		long currTime;
		long endTime;
		long reportInterval;
		
		// Space
		long bytes;
		int  iters;
		int  msgs;
		int  maxIter;
		
		
		private long[] times;
		
		PerfMeasurements(int maxIter) {
			this.maxIter = maxIter;
		}
		
		void setPerfMeasurements(double secs, double report) {
			currTime = prevTime = startTime = Time.nanoTime();
			endTime = (secs == 0.0) ? Long.MAX_VALUE : (Time.secsToNanos(secs) + startTime);
			reportInterval = Time.secsToNanos(report);
			bytes = iters = msgs = 0;
		}

		void setTimesArray(int size) {
			times = new long[size];
		}
		
		//TODO
		void setMeasurement(int index, int iters) {
			this.iters += iters;
			setTimeSample(index);
			prevTime = currTime;
		}
		
		void setCurrentMeasurement(int index, int iters, int msgs, long bytes) {
			setSpaceSample(iters, msgs, bytes);
			setTimeSample(index);
		}
		
		boolean done() {
			return (currTime >= endTime) || (iters >= maxIter);
		}
		
		long[] timeSamples() {
			return times.clone();
		}
		
		private void setSpaceSample(int iters, int msgs, long bytes) {
			this.iters += iters;
			this.msgs += msgs;
			this.bytes += bytes;
		}
		
		private void setTimeSample(int index) {
			times[index] = currTime - prevTime;
		}
	}
	
	public static class UcpObjects {
		Context 	ctx 		= null;
		Worker 		worker 		= null;
		EndPoint	endPoint 	= null;
		
		public UcpObjects(Context ctx) {
			this.ctx = ctx;
		}
		
		public void setWorker(Callbacks cb, int queueSize) {
			worker = new Worker(ctx, cb, queueSize);
		}
		
		public void setEndPoint(WorkerAddress remoteAddr) {
			endPoint = worker.createEndPoint(remoteAddr);
		}
		
		public void close() {
			worker.close();
			ctx.close();
		}
	}
	
	public static class TcpConnection {
		private Socket sock;
		private ObjectInputStream inStream;
		private ObjectOutputStream outStream;
		
		TcpConnection(Socket socket) {
			sock = socket;
			try {
				outStream = new ObjectOutputStream(sock.getOutputStream());
				inStream = new ObjectInputStream(sock.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		void close() {
			try {
				inStream.close();
				outStream.close();
				sock.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		void write(Object s) throws IOException {
			outStream.writeObject(s);
		}
		
		Object read() throws IOException {
			try {
				return inStream.readObject();
			}
			catch (ClassNotFoundException ce) {
				ce.printStackTrace();
				System.exit(1);
			}
			
			return null;
		}
		
		void barrier(boolean server) {
			try {
				if (server)
					serverBarrier();
				else
					clientBarrier();
			}
			catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		private void clientBarrier() throws IOException {
			int x = 0;
			outStream.write(x);
			outStream.flush();
			x = inStream.read();
		}

		private void serverBarrier() throws IOException {
			int x;
			x = inStream.read();
			outStream.write(x);
			outStream.flush();
		}
	}
}







