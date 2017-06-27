/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
package org.ucx.jucx;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Worker is the object representing a local communication resource such as
 * a network interface or host channel adapter port.
 * Worker is associated with a single Context.
 */
public class Worker {
	private final static int REQUEST_ERROR 		= -1;
	private final static int REQUEST_PENDING 	= 0;
	
	public final static long DEFAULT_TAG_MASK 	= -1L;
	public final static long DEFAULT_TAG		= -1L;
	public final static long DEFAULT_REQ_ID 	= -1L;
	
	private static AtomicInteger OutstandingRequests = new AtomicInteger(0);
	
	// for synchronized blocks
	private Object mutex = new Object();
	

	private Context ucpContext;
	private long nativeID;
	private CompletionQueue compQueue;
	private WorkerAddress workerAddr;
	private Callbacks callback;
	private Set<EndPoint> endPoints;
	
	/**
	 * Creates a new Worker associated with Context ctx.
	 * 
	 * @param 	ctx
	 * 			Application context this Worker will be associated with
	 * 
	 * @param 	cb
	 * 			Implementation of Worker.Callbacks interface
	 * 
	 * @param 	maxCompletions
	 * 			Number of max un-handled completions
	 */
	public Worker(Context ctx, Callbacks cb, int maxCompletions) {
		ucpContext = ctx;
		callback = cb;
		int maxInBytes = maxCompletions << 3;
		compQueue = new CompletionQueue(maxInBytes);
		nativeID = Bridge.createWorker(ucpContext.getNativeID(), maxInBytes, compQueue);
		if (nativeID < 0)
			System.out.println("Error: native worker");
		workerAddr = new WorkerAddress(nativeID);
		endPoints = Collections.synchronizedSet(new HashSet<EndPoint>());
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
	 * Worker posts a receive request for an event with matching tag
	 * (filtered by tagMask) at length msgLen. In case of success - msg will
	 * be set accordingly.
	 * 
	 * @param 	tag
	 * 			Message tag to expect
	 * 
	 * @param 	tagMask
	 * 			Bit mask for the comparison of received and expected tag
	 * 
	 * @param 	msg
	 *			Buffer to fill with received data 
	 * 
	 * @param 	msgLen
	 * 			Number of bytes to receive
	 * 
	 * @param 	reqID
	 * 			Receive request ID
	 * 
	 * @return
	 * 		   -1 - in case of an error
	 * 			0 - in case the request is not completed
	 * 			o.w. - request succeeded, message is in buffer msg
	 * 
	 * @throws 	BufferOverflowException
	 * 			If there is insufficient space in this buffer (msgLen > msg.remaining())
	 */
	public int recvMessageAsync(long tag, long tagMask, ByteBuffer msg, int msgLen, long reqID) {
		int cnt = recvMessage(tag, tagMask, msg, msgLen, reqID);
		setCounter(cnt);
		return cnt;
	}
	
	/**
	 * Worker posts a receive request for an event with tag = -1. Same as calling
	 * {@code recvMessageAsync(-1, -1, msg, msgLen, reqID)}.
	 * 
	 * @param 	msg
	 *			Buffer to fill with received data 
	 * 
	 * @param 	msgLen
	 * 			Number of bytes to receive
	 * 
	 * @param 	reqID
	 * 			Receive request ID
	 * 
	 * @return
	 * 		   -1 - in case of an error
	 * 			0 - in case the request is not completed
	 * 			o.w. - request succeeded, message is in buffer msg
	 * 
	 * @throws 	BufferOverflowException
	 * 			If there is insufficient space in this buffer (msgLen > msg.remaining())
	 */
	public int recvMessageAsync(ByteBuffer msg, int msgLen, long reqID) {
		return recvMessageAsync(DEFAULT_TAG, DEFAULT_TAG_MASK, msg, msgLen, reqID);
	}
	
	/**
	 * Worker posts a receive request (with id = -1, i.e. doesn't invokes the callback)
	 * Same as calling {@code recvMessageAsync(tag, tagMask, msg, msgLen, -1)}.
	 * 
	 * @param 	tag
	 * 			Message tag to expect
	 * 
	 * @param 	tagMask
	 * 			Bit mask for the comparison of received and expected tag
	 * 
	 * @param 	msg
	 *			Buffer to fill with received data 
	 * 
	 * @param 	msgLen
	 * 			Number of bytes to receive
	 * 
	 * @return
	 * 		   -1 - in case of an error
	 * 			0 - in case the request is not completed
	 * 			o.w. - request succeeded, message is in buffer msg
	 * 
	 * @throws 	BufferOverflowException
	 * 			If there is insufficient space in this buffer (msgLen > msg.remaining())
	 */
	public int recvMessageAsync(long tag, long tagMask, ByteBuffer msg, int msgLen) {
		return recvMessageAsync(tag, tagMask, msg, msgLen, DEFAULT_REQ_ID);
	}
	
	/**
	 * Worker posts a receive request for an event with tag = -1 and a don't care reqID.
	 * Same as calling {@code recvMessageAsync(-1, -1, msg, msgLen, -1)}.
	 * 
	 * @param 	msg
	 *			Buffer to fill with received data 
	 * 
	 * @param 	msgLen
	 * 			Number of bytes to receive
	 * 
	 * @return
	 * 		   -1 - in case of an error
	 * 			0 - in case the request is not completed
	 * 			o.w. - request succeeded, message is in buffer msg
	 * 
	 * @throws 	BufferOverflowException
	 * 			If there is insufficient space in this buffer (msgLen > msg.remaining())
	 */
	public int recvMessageAsync(ByteBuffer msg, int msgLen) {
		return recvMessageAsync(DEFAULT_TAG, DEFAULT_TAG_MASK, msg, msgLen, DEFAULT_REQ_ID);
	}
	
	/**
	 * Check for any completed send/receive requests.
	 * For each completion (with id != -1) the request handler is invoked.
	 */
	public void progress() {
		compQueue.completionBuff.rewind();
		int numOfEvents;
		
		synchronized (mutex)
		{
			numOfEvents = Bridge.progressWorker(this);
		}
		
		OutstandingRequests.getAndAdd(-numOfEvents);
		
		for (int i = 0; i < numOfEvents; i++) {
			long compId = compQueue.completionBuff.getLong();
			if (compId != DEFAULT_REQ_ID)
				callback.requestHandle(compId);
		}
	}
	
	/**
	 * Getter for native pointer as long.
	 * 
	 * @return long integer representing native pointer
	 */
	public long getNativeID() {
		return nativeID;
	}
	
	/**
	 * Getter for current (this) Worker's address.
	 * 
	 * @return WorkerAddress object representing Worker's address
	 */
	public WorkerAddress getAddress() {
		return workerAddr;
	}
	
	/**
	 * Frees all resources associated with this Worker.
	 * Worker should not be used after calling this method.
	 */
	public void close() {
		for (EndPoint ep : endPoints) {
			ep.close();
		}
		Bridge.releaseWorker(this);
	}
	
	void addEndPoint(EndPoint ep) {
		endPoints.add(ep);
	}
	
	void removeEndPoint(EndPoint ep) {
		endPoints.remove(ep);
	}
	
	public EndPoint createEndPoint(byte[] remoteAddr) {
		return createEndPoint(new WorkerAddress(remoteAddr));
	}
	
	/**
	 * @param worker
	 * @param addr
	 * @return
	 */
	public EndPoint createEndPoint(WorkerAddress remoteAddr) {
		EndPoint ep = new EndPoint(this, remoteAddr);
		addEndPoint(ep);
		return ep;
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
		if (msgLen > msg.remaining())
			throw new BufferUnderflowException();
		
		int sent = 0;
		OutstandingRequests.incrementAndGet();
		
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
		if (msgLen > msg.remaining())
			throw new BufferOverflowException();
		
		int rcvd = 0;
		OutstandingRequests.incrementAndGet();
		
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
	
	void setCounter(int cnt) {
		compQueue.setCompletionCnt(cnt);
	}
	
	class CompletionQueue {
		private int	completionCnt;
		private int	completionCap;
		ByteBuffer 	completionBuff;
		
		private CompletionQueue(int capacity) {
			completionCnt 	= 0;
			completionCap 	= capacity;
			completionBuff 	= null;
		}
		
		private void setCompletionCnt(int completionCnt) {
			this.completionCnt = completionCnt;
		}
	}
	
	/**
	 * The Callbacks interface must be implemented in-order to create a Worker.
	 * Worker will invoke the implemented method whenever a request is completed.
	 */
	public static interface Callbacks {
		
		/**
		 * Triggered whenever a new event is completed.
		 * 
		 * @param 	requestId 
		 * 			Id of completed request
		 */
		public void requestHandle(long requestId);
	}
	
}









