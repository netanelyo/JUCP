package org.ucx.jucx;

import java.util.HashMap;
import java.util.Map;

public class UCPWorker {
	
	private static Map<Long, UCPWorker> threads = new HashMap<>();

	private UCPContext ucpContext;
	private long nativeID;
	private final UCPLocalWorkerAddress workerAddr;
	
	private UCPWorker(UCPContext ctx) {
		ucpContext = ctx;
		nativeID = Bridge.createWorker(ctx.getNativeID());
		workerAddr = new UCPLocalWorkerAddress(nativeID);
	}
	
	public static UCPWorker getInstance(UCPContext ctx) {
		long thID = Thread.currentThread().getId();
		if (threads.containsKey(thID)){
			System.out.println("Single worker per thread is allowed");
			return threads.get(thID);
		}
		else {
			UCPWorker worker = new UCPWorker(ctx);
			threads.put(thID, worker);
			return worker;
		}
	}
	
	public long getNativeID() {
		return nativeID;
	}
	
	public UCPLocalWorkerAddress getAddress() {
		return workerAddr;
	}
	
	// Users should be able to use only address...
	public final class UCPLocalWorkerAddress extends UCPWorkerAddress {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private transient long nativeID;
		
		private UCPLocalWorkerAddress(long workerNativeID) {
			long[] retValue = new long[1];
			workerAddr = Bridge.getWorkerAddress(workerNativeID, retValue);
			nativeID = retValue[0];
		}
		
		public long getNativeID() {
			return nativeID;
		}
		
	}
	
	public UCPTagMsg recvMessage(long tag) {
		UCPTagMsg msg = UCPTagMsg.getInMsg(this, tag);
		return msg;
	}
	
}









