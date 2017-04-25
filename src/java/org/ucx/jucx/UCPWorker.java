package org.ucx.jucx;

import java.util.HashSet;
import java.util.Set;

public class UCPWorker {
	
	private static Set<Long> threads = new HashSet<>();

	private UCPContext ucpContext;
	private long nativeID;
	private final UCPWorkerAddress workerAddr;
	
	private UCPWorker(UCPContext ctx) {
		ucpContext = ctx;
		nativeID = Bridge.createWorker(ctx.getNativeID());
		workerAddr = new UCPWorkerAddress(nativeID);
	}
	
	public static UCPWorker getInstance(UCPContext ctx) {
		long thID = Thread.currentThread().getId();
		if (threads.contains(thID)){
			System.out.println("Single worker per thread is allowed");
			return null;
		}
		else {
			threads.add(thID);
			return new UCPWorker(ctx);
		}
	}
	
	public long getNativeID() {
		return nativeID;
	}
	
	public UCPWorkerAddress getAddress() {
		return workerAddr;
	}
	
	// Users should be able to use only address...
	public final class UCPWorkerAddress {
		
		private final byte[] workerAddr;
		private long nativeID;
		
		private UCPWorkerAddress(long workerNativeID) {
			long[] retValue = new long[1];
			workerAddr = Bridge.getWorkerAddress(workerNativeID, retValue);
			nativeID = retValue[0];
		}

		public byte[] getWorkerAddr() {
			return workerAddr.clone();
		}
		
		public long getNativeID() {
			return nativeID;
		}
	}
	
	public UCPTagMsg recvMessage(long tag) {
		UCPTagMsg msg = new UCPTagMsg(this, tag);
		return msg;
	}
	
}









