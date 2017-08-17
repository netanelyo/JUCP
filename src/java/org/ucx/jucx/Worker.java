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
	
//	private AtomicInteger 	outstandingRequests = new AtomicInteger(0);
	private int				outstandingRequests = 0;
	
	// TODO: for synchronized blocks
//	private Object mutex = new Object();
	

	private Context ucpContext;
	private long nativeID;
	private CompletionQueue compQueue;
	private WorkerAddress workerAddr;
	private Callback callback;
	private Set<EndPoint> endPoints;
	private int maxEvents;
	
	/**
	 * Creates a new Worker associated with Context ctx.
	 * 
	 * @param 	ctx
	 * 			Application context this Worker will be associated with
	 * 
	 * @param 	cb
	 * 			Implementation of Worker.Callbacks interface
	 * 
	 * @param 	maxEvents
	 * 			Number of max queued completed events
	 * 
	 * @throws 	IllegalArgumentException
	 * 			In case maxComp <= 0
	 */
	public Worker(Context ctx, Callback cb, int maxEvents /*TODO - the Java 8 version of callbacks, Consumer<Long> cb*/) {
		if (maxEvents <= 0)
			throw new IllegalArgumentException();
		
		ucpContext = ctx;
		callback = cb;
		this.maxEvents = maxEvents;
		compQueue = new CompletionQueue();
		nativeID = Bridge.createWorker(ucpContext.getNativeID(), maxEvents, compQueue);
		workerAddr = new WorkerAddress(this);
		endPoints = Collections.synchronizedSet(new HashSet<EndPoint>());
	}
	
	/**
	 * Worker posts a receive request for an event with matching tag
	 * (filtered by tagMask) at length buffLen. In case of success - buff will
	 * be set accordingly.
	 * 
	 * @param 	buff
	 *			Buffer to fill with received data 
	 * 
	 * @param 	buffLen
	 * 			Number of bytes to receive
	 * 
	 * @param 	tag
	 * 			Message tag to expect
	 * 
	 * @param 	tagMask
	 * 			Bit mask for the comparison of received and expected tag
	 * 
	 * @param 	reqID
	 * 			Receive request ID
	 * 
	 * @return
	 * 		   -1 - in case of an error
	 * 			0 - in case the request is not completed
	 * 			o.w. - request succeeded, message is in buffer buff
	 * 
	 * @throws 	BufferOverflowException
	 * 			If there is insufficient space in this buffer (buffLen > buff.remaining())
	 * 
	 * @throws 	IllegalStateException
	 * 			If events queue is in full capacity
	 */
	public int tagRecvAsync(ByteBuffer buff, int buffLen, long tag, long tagMask, long reqID) {
		int cnt = recvMessage(buff, buffLen, tag, tagMask, reqID);
		return cnt;
	}
	
	/**
	 * Worker posts a receive request for an event with tag = DEFAULT_TAG.</br>
	 * An invocation of this method has exactly the same effect as invoking
	 * {@code tagRecvAsync(DEFAULT_TAG, DEFAULT_TAG_MASK, buff, buffLen, reqID)}.
	 * 
	 * @param 	buff
	 *			Buffer to fill with received data 
	 * 
	 * @param 	buffLen
	 * 			Number of bytes to receive
	 * 
	 * @param 	reqID
	 * 			Receive request ID
	 * 
	 * @return
	 * 		   -1 - in case of an error
	 * 			0 - in case the request is not completed
	 * 			o.w. - request succeeded, message is in buffer buff
	 * 
	 * @throws 	BufferOverflowException
	 * 			If there is insufficient space in this buffer (buffLen > buff.remaining())
	 * 
	 * @throws 	IllegalStateException
	 * 			If events queue is in full capacity
	 */

	public int tagRecvAsync(ByteBuffer buff, int buffLen, long reqID) {
		return tagRecvAsync(buff, buffLen, DEFAULT_TAG, DEFAULT_TAG_MASK, reqID);
	}
	
	/**
	 * Worker posts a receive request (with id = DEFAULT_REQ_ID,
	 * i.e. doesn't invoke the callback).</br>
	 * An invocation of this method has exactly the same effect as invoking
	 * {@code tagRecvAsync(tag, tagMask, buff, buffLen, DEFAULT_REQ_ID)}.
	 * 
	 * @param 	buff
	 *			Buffer to fill with received data 
	 * 
	 * @param 	buffLen
	 * 			Number of bytes to receive
	 * 
	 * @param 	tag
	 * 			Message tag to expect
	 * 
	 * @param 	tagMask
	 * 			Bit mask for the comparison of received and expected tag
	 * 
	 * @return
	 * 		   -1 - in case of an error
	 * 			0 - in case the request is not completed
	 * 			o.w. - request succeeded, message is in buffer buff
	 * 
	 * @throws 	BufferOverflowException
	 * 			If there is insufficient space in this buffer (buffLen > buff.remaining())
	 * 
	 * @throws 	IllegalStateException
	 * 			If events queue is in full capacity
	 */
	public int tagRecvAsync(ByteBuffer buff, int buffLen, long tag, long tagMask) {
		return tagRecvAsync(buff, buffLen, tag, tagMask, DEFAULT_REQ_ID);
	}
	
	/**
	 * Worker posts a receive request for an event with
	 * tag = DEFAULT_TAG and a don't care reqID.</br>
	 * An invocation of this method has exactly the same effect as invoking
	 * {@code tagRecvAsync(DEFAULT_TAG, DEFAULT_TAG_MASK, buff, buffLen, DEFAULT_REQ_ID)}.
	 * 
	 * @param 	buff
	 *			Buffer to fill with received data 
	 * 
	 * @param 	buffLen
	 * 			Number of bytes to receive
	 * 
	 * @return
	 * 		   -1 - in case of an error
	 * 			0 - in case the request is not completed
	 * 			o.w. - request succeeded, message is in buffer buff
	 * 
	 * @throws 	BufferOverflowException
	 * 			If there is insufficient space in this buffer (buffLen > buff.remaining())
	 * 
	 * @throws 	IllegalStateException
	 * 			If events queue is in full capacity
	 */
	public int tagRecvAsync(ByteBuffer buff, int buffLen) {
		return tagRecvAsync(buff, buffLen, DEFAULT_TAG, DEFAULT_TAG_MASK, DEFAULT_REQ_ID);
	}
	
	/**
	 * Check for any completed send/receive requests.</br>
	 * For each completion (with id != DEFAULT_REQ_ID) cb.requestHandler(id) is invoked.
	 */
	public void progress() {
		wait(0);
	}
	
	/**
	 * Block until receiving at least numEvents completions.</br>
	 * For each completion (with id != DEFAULT_REQ_ID) cb.requestHandler(id) is invoked.</br>
	 * {@code wait(0)} An invocation of this method has exactly the same effect
	 * as invoking {@code progress()}
	 * 
	 * @param 	numEvents
	 * 			Minimum number of completions to wait for.</br>
	 * 			If numEvents > outstandingRequests then numEvents = outstandingRequests.
	 * 
	 * @throws  IllegalArgumentException
	 * 			If numEvents < 0
	 */
	public void wait(int numEvents) {
		compQueue.completionBuff.clear();
		int events = Math.min(numEvents, outstandingRequests);
		
		if (events >= 0) {
			int num = Bridge.workerWait(this, events);
			executeCallback(num);
		}
		
		else
			throw new IllegalArgumentException();
	}
	
	/**
	 * Block until all outstanding requests are completed.</br>
	 * For each completion (with id != DEFAULT_REQ_ID) cb.requestHandler(id) is invoked.</br>
	 * 
	 * @throws  IllegalArgumentException
	 * 			If numEvents < 0 or numEvents > outstandingRequests
	 */
	public void waitAll() {
		wait(outstandingRequests);
	}
	
	/**
	 * releases all outstanding requests
	 */
	private void flush() {
		Bridge.workerFlush(this, outstandingRequests);
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
	 * @return the outstandingRequests
	 */
	public int getOutstandingRequests() {
		return outstandingRequests;
	}

	/**
	 * Getter for (this) Worker's address.
	 * 
	 * @return WorkerAddress object representing Worker's address
	 */
	public WorkerAddress getAddress() {
		return workerAddr;
	}
	
	/**
	 * Frees all resources associated with this Worker.</br>
	 * Worker should not be used after calling this method.
	 */
	public void close() {
		for (EndPoint ep : endPoints) {
			removeEndPoint(ep);
			ep.close();
		}
		flush();
		Bridge.releaseWorker(this);
	}
	
	/**
	 * create a new EndPoint object linked to (this) Worker
	 * 
	 * @param 	remoteAddr
	 * 			Address of the remote worker the new EndPoint will be connected to
	 * 
	 * @return new EndPoint object
	 */
	public EndPoint createEndPoint(WorkerAddress remoteAddr) {
		EndPoint ep = new EndPoint(this, remoteAddr);
		addEndPoint(ep);
		return ep;
	}
	
	private void addEndPoint(EndPoint ep) {
		endPoints.add(ep);
	}
	
	private void removeEndPoint(EndPoint ep) {
		endPoints.remove(ep);
	}
	
	private void executeCallback(int numOfEvents) {
		outstandingRequests -= numOfEvents;
		
		for (int i = 0; i < numOfEvents; i++) {
			long compId = compQueue.completionBuff.getLong();
			if (compId != DEFAULT_REQ_ID)
				callback.requestHandle(compId);
		}
	}
	
	int sendMessage(EndPoint ep, ByteBuffer msg, int msgLen, long tag, long reqID) {
		if (msgLen > msg.remaining())
			throw new BufferUnderflowException();
		
		if (outstandingRequests == maxEvents)
			throw new IllegalStateException("Number of requests exceeds limit");
		
		int sent = Bridge.sendMsgAsync(ep, tag, msg, msgLen, reqID);

		outstandingRequests++;
		
		return sent;
	}
	
	private int recvMessage(ByteBuffer buff, int buffLen, long tag, long tagMask, long reqID) {
		if (buffLen > buff.remaining())
			throw new BufferOverflowException();
		
		if (outstandingRequests == maxEvents)
			throw new IllegalStateException("Number of requests exceeds limit");
		
		int rcvd = Bridge.recvMsgAsync(this, tag, tagMask, buff, buffLen, reqID);
		
		outstandingRequests++;
		
		return rcvd;
	}
	
	//TODO: currently unused
	public EndPoint createEndPoint(byte[] remoteAddr) {
		return createEndPoint(new WorkerAddress(remoteAddr));
	}
	
	//TODO: currently unused
	@SuppressWarnings("unused")
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
	
	class CompletionQueue {
		ByteBuffer completionBuff = null;
	}
	
	/**
	 * The Callbacks interface must be implemented in-order to create a Worker.</br>
	 * Worker will invoke the implemented method whenever a request is completed.
	 */
	@FunctionalInterface
	public static interface Callback {
		
		/**
		 * Invoked whenever an event is completed.
		 * 
		 * @param 	requestId
		 * 			Id of completed request
		 */
		public void requestHandle(long requestId);
	}
	
}









