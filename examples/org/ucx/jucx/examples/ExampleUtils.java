package org.ucx.jucx.examples;

import java.nio.charset.Charset;
import java.util.Random;

import org.ucx.jucx.Worker;
import org.ucx.jucx.examples.ExampleContext.BandwidthBuffer;

public class ExampleUtils {
	
	static String generateRandomString(int size) {
		Random rand = new Random();
		byte[] arr = new byte[size];
		rand.nextBytes(arr);
		
		String gen = new String(arr, Charset.forName("US-ASCII"));
		
		return gen;
	}
	
	public static class BandwidthCallback extends PingPongCallback {
		private BandwidthBuffer bufferPool;
		
		public BandwidthCallback(BandwidthBuffer buff, int size) {
			super(size);
			bufferPool = buff;
		}
		
		public boolean ready() {
			return bufferPool.ready();
		}
		
		@Override
		public void requestHandle(long requestId) {
			bufferPool.freeOne();
			super.requestHandle(requestId);
		}
	}
	
	public static class PingPongCallback implements Worker.Callbacks {
		public int[] requests;
		public int last;
		
		public PingPongCallback(int size) {
			requests = new int[size];
			last = 0;
		}
		
		@Override
		public void requestHandle(long requestId) {
			requests[(int)requestId]++;
			last++;
		}
	}
}
