package org.ucx.jucx.tests.perftest;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

import org.ucx.jucx.Worker.Callbacks;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.utils.Utils;

import sun.nio.ch.DirectBuffer;

public abstract class BandwidthTest extends PerftestBase {

	protected ByteBufferArray buffers;
	
	@Override
	protected void run(PerfParams params) {
		ctx = new PerfContext(params);
		buffers = new ByteBufferArray(params.maxOutstanding, params.size, this instanceof BandwidthClient);
		ctx.cb = new Callback(buffers);
		super.run(params);
	}
	
	private static class CyclicArray<T> {
		protected T[] arr;
		protected int position;
		protected int free;
		
		@SuppressWarnings("unchecked")
		protected CyclicArray(Class<T> c, int size) {
			arr = (T[])Array.newInstance(c, size);
			position = 0;
			free = size;
		}
		
		void free() {
			free++;
		}
		
		boolean ready() {
			return free > 0;
		}
		
		boolean initialized() {
			return free == arr.length;
		}
		
		T get() {
			--free;
			T ret = arr[position];
			position = ++position % arr.length;
			return ret;
		}
	}
	
	static class ByteBufferArray extends CyclicArray<ByteBuffer> {
		
		ByteBufferArray(int length, int bufferSize, boolean client) {
			this(length, bufferSize);
			
			if (client) {
				for (ByteBuffer bb : arr) {
					byte[] r = Utils.generateRandomBytes(bufferSize);
					bb.put(r);
					bb.rewind();
				}
			}
		}
		
		ByteBufferArray(int length, int bufferSize) {
			super(ByteBuffer.class, length);
			
			for (int i = 0; i < arr.length; i++) {
				arr[i] = ByteBuffer.allocateDirect(bufferSize);
				long addr = ((DirectBuffer)arr[i]).address();
				System.out.println("Buffer address: 0x" + Long.toHexString(addr));
			}
		}
	}
	
	static class Callback implements Callbacks {
		
		private ByteBufferArray arr;
		
		Callback(ByteBufferArray a) {
			arr = a;
		}
		
		@Override
		public void requestHandle(long requestId) {
			arr.free();
		}
	}
}






