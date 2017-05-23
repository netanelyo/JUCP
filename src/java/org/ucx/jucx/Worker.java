package org.ucx.jucx;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Worker {
	/*
	 * TODO: find a better place
	 */
	private static int REQUEST_ERROR 	= -1;
	private static int REQUEST_PENDING 	= 0;
	private static int REQUEST_COMPLETE = 1;
//	private static Map<Long, Worker> threads = new HashMap<>();

	private Context ucpContext;
	private long nativeID;
	private CompletionQueue compQueue;
	private final WorkerAddress workerAddr;
	private final Callbacks callback;
	
	
	public Worker(Context ctx, Callbacks cb) {
		ucpContext = ctx;
		callback = cb;
		compQueue = new CompletionQueue(800 /*TODO: CHANGE!!!*/);
		nativeID = Bridge.createWorker(ucpContext.getNativeID(), compQueue.completionBuff);
		workerAddr = new WorkerAddress(nativeID);
	}
	
//	public static Worker getInstance(Context ctx, Callbacks cb) {
//		long thID = Thread.currentThread().getId();
//		if (threads.containsKey(thID)){
//			System.out.println("Single worker per thread is allowed");
//			return threads.get(thID);
//		}
//		else {
//			Worker worker = new Worker(ctx, cb);
//			threads.put(thID, worker);
//			return worker;
//		}
//	}
	
	
	
	/*
	 * can get stuck in infinite loop...
	 */
	public void progress(int maxEvents) {
		compQueue.completionBuff.rewind();
		int cnt = compQueue.completionCnt;
		while (cnt < maxEvents) {
				cnt += Bridge.progressWorker(this, maxEvents);
		}
		for (int i = 0; i < maxEvents; i++) {
			callback.requestHandle(compQueue.completionBuff.getLong());
			cnt--;
		}
		compQueue.completionCnt = cnt;
	}
	
	public void progress() {
		progress(1);
	}
	
	public long getNativeID() {
		return nativeID;
	}
	
	public WorkerAddress getAddress() {
		return workerAddr;
	}
	
	public TagMsg recvMessage(long tag) {
		TagMsg msg = TagMsg.getInMsg(this, tag);
		return msg;
	}
	
	public void free() {
		Bridge.releaseWorker(this);
	}
	
	void setCounter(int cnt) {
		compQueue.setCompletionCnt(cnt);
	}
	
	private class CompletionQueue {
		private int			completionCnt;
		private ByteBuffer 	completionBuff;
		
		private CompletionQueue(int capacity) {
			completionCnt 	= 0;
			completionBuff 	= ByteBuffer.allocateDirect(capacity);
		}
		
		private void setCompletionCnt(int completionCnt) {
			this.completionCnt = completionCnt;
		}
	}
	
	public static interface Callbacks {
		
		public void requestHandle(long requestId);
	}
	
}









