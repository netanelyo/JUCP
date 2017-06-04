package org.ucx.jucx;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker {
	private final static int REQUEST_ERROR 		= -1;
	private final static int REQUEST_PENDING 	= 0;
	private final static int REQUEST_COMPLETE 	= 1;
	
	private final static long DEFAULT_TAG_MASK 	= -1L;
	private final static long DEFAULT_TAG		= -1L;
	
	private static AtomicInteger OUTSTANDING_REQUESTS = new AtomicInteger(0);
	private Object mutex = new Object();
	
	// package visibility for EndPoint usage
	final static long DEFAULT_REQ_ID 	= 0;

	private Context ucpContext;
	private long nativeID;
	private CompletionQueue compQueue;
	private WorkerAddress workerAddr;
	private Callbacks callback;
	
	
	public Worker(Context ctx, Callbacks cb) {
		ucpContext = ctx;
		callback = cb;
		compQueue = new CompletionQueue(800 /*TODO: CHANGE!!!*/);
		nativeID = Bridge.createWorker(ucpContext.getNativeID(), compQueue.completionBuff);
		workerAddr = new WorkerAddress(nativeID);
	}
	
	
//	public void progress(/* int minEvents, int maxEvents, int timeOutMSec*/) {
//		compQueue.completionBuff.rewind();
//		int cnt = compQueue.completionCnt;
//		int maxEv = maxEvents;
//		int maxPossible = Math.min(OUTSTANDING_REQUESTS, compQueue.completionCap);
//		
//		// Max processed requests bounded by num of outstanding requests and capacity of buffer
//		if (maxEvents > maxPossible) {
//			maxEv = maxPossible;
//		}
//		
//		while (cnt < maxEv) {
//				cnt += Bridge.progressWorker(this, maxEv);
//		}
//		
//		for (int i = 0; i < maxEv; i++) {
//			callback.requestHandle(compQueue.completionBuff.getLong());
//			cnt--;
//		}
//		compQueue.completionBuff.compact(); // Compacting buffer (removing all read events)
//
//		OUTSTANDING_REQUESTS -= maxEv;
//		compQueue.completionCnt = cnt;
//	}
	
	/**
	 * Check for events
	 */
	public void progress() {
		compQueue.completionBuff.rewind();
		int numOfEvents;
		
		synchronized (mutex)
		{
			numOfEvents = Bridge.progressWorker(this);
		}
		
		for (int i = 0; i < numOfEvents; i++)
			callback.requestHandle(compQueue.completionBuff.getLong());
	}
	
	public long getNativeID() {
		return nativeID;
	}
	
	public WorkerAddress getAddress() {
		return workerAddr;
	}
	
	private void checkRequestReturnStatus(int rc) {
		switch (rc)
		{
		case REQUEST_ERROR:
			
			break;
			
		case REQUEST_PENDING:
			
			break;
			
		default:
			break;
		}
	}
	
	int sendMessage(EndPoint ep, long tag, ByteBuffer msg, int msgLen, long reqID) {
		int sent = 0;
		OUTSTANDING_REQUESTS.incrementAndGet();
		
		synchronized (mutex)
		{
			if (msg.isDirect()) {
				sent = Bridge.sendMsgAsync(ep, tag, msg, msgLen, reqID);
			}
			else {
				sent = Bridge.sendMsgAsync(ep, tag, msg.array(), msgLen, reqID);
			}
		}
		
		checkRequestReturnStatus(sent);
		
		return sent;
	}
	
	private int recvMessage(long tag, long tagMask, ByteBuffer msg, int msgLen, long reqID) {
		int rcvd = 0;
		OUTSTANDING_REQUESTS.incrementAndGet();
		
		synchronized (mutex)
		{
			if (msg.isDirect()) {
				rcvd = Bridge.recvMsgAsync(this, tag, tagMask, msg, msgLen, reqID);
			}
			else {
				rcvd = Bridge.recvMsgAsync(this, tag, tagMask, msg.array(), msgLen, reqID);
			}
		}
		
		checkRequestReturnStatus(rcvd);
		
		return rcvd;
	}
	
	/**
	 * 
	 * @param tag
	 * @param tagMask
	 * @param msg
	 * @param msgLen
	 * @param reqID
	 * @return
	 */
	public int recvMessageAsync(long tag, long tagMask, ByteBuffer msg, int msgLen, long reqID) {
		int cnt = recvMessage(tag, tagMask, msg, msgLen, reqID);
		setCounter(cnt);
		return cnt;
	}
	
	// dontcare msg tag
	public int recvMessageAsync(ByteBuffer msg, int msgLen, long reqID) {
		return recvMessageAsync(DEFAULT_TAG, DEFAULT_TAG_MASK, msg, msgLen, reqID);
	}
	
	// dontcare request id
	public int recvMessageAsync(long tag, long tagMask, ByteBuffer msg, int msgLen) {
		return recvMessageAsync(tag, tagMask, msg, msgLen, DEFAULT_REQ_ID);
	}
	
	// dontcare reqID and msg tag
	public int recvMessageAsync(ByteBuffer msg, int msgLen) {
		return recvMessageAsync(DEFAULT_TAG, DEFAULT_TAG_MASK, msg, msgLen, DEFAULT_REQ_ID);
	}
	
	public void free() {
		Bridge.releaseWorker(this);
	}
	
	void setCounter(int cnt) {
		compQueue.setCompletionCnt(cnt);
	}
	
	private class CompletionQueue {
		private int			completionCnt;
		private int			completionCap;
		private ByteBuffer 	completionBuff;
		
		private CompletionQueue(int capacity) {
			completionCnt 	= 0;
			completionCap 	= capacity;
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









