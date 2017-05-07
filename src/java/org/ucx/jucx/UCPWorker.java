package org.ucx.jucx;

import java.util.HashMap;
import java.util.Map;

public class UCPWorker {
	
	private static Map<Long, UCPWorker> threads = new HashMap<>();

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
	
	public UCPWorkerAddress getAddress() {
		return workerAddr;
	}
	
	public UCPTagMsg recvMessage(long tag) {
		UCPTagMsg msg = UCPTagMsg.getInMsg(this, tag);
		return msg;
	}
	
	public void free() {
		Bridge.releaseWorker(this);
	}
	
}









