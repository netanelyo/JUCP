/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
package org.ucx.jucx;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class EndPoint {
	private long 			nativeID;
	private Worker 			localWorker;
	private WorkerAddress 	remoteWorkerAddress;
	
	/**
	 * Creates a new EndPoint object
	 * 
	 * @param 	worker
	 * 			(local) Worker object this EndPoint is associated with
	 * 
	 * @param 	addr
	 * 			(remote) Worker's address this EndPoint will connect to
	 */
	EndPoint(Worker worker, WorkerAddress addr) {
		localWorker = worker;
		remoteWorkerAddress = addr;
		nativeID = Bridge.createEndPoint(worker, remoteWorkerAddress);
	}
	
	/**
	 * EndPoint posts a send request for a message with tag tag. </br>
	 * Message is of length msgLen.
	 * 
	 * @param 	tag
	 * 			Sent message's tag
	 * 
	 * @param 	msg
	 * 			Outgoing message
	 * 
	 * @param 	msgLen
	 * 			Message length
	 * 
	 * @param 	reqID
	 * 			Send request ID
	 * 
	 * @return
	 * 			-1 - in case of an error
	 * 			0 - in case the request is not completed (in progress)
	 * 			o.w. - success
	 * 
	 * @throws 	BufferUnderflowException
	 * 			If there isn't enough data to send (msgLen > msg.remaining())
	 */
	public int tagSendAsync(long tag, ByteBuffer msg, int msgLen, long reqID) {
		int cnt = localWorker.sendMessage(this, tag, msg, msgLen, reqID);
		localWorker.setCounter(cnt);
		return cnt;
	}
	
	// dontcare request id
	public int tagSendAsync(long tag, ByteBuffer msg, int msgLen) {
		return tagSendAsync(tag, msg, msgLen, Worker.DEFAULT_REQ_ID);
	}
	
//	public int sendMessageSync(TagMsg msg, long reqID) {
//		return commonSend(msg, true, reqID);
//	}
	
//	public int sendMessageSync(TagMsg msg) {
//		return commonSend(msg, true, 0);
//	}
	
	public long getNativeID() {
		return nativeID;
	}
	
	/**
	 * Getter for local worker
	 * 
	 * @return Worker object
	 */
	public Worker getWorker() {
		return localWorker;
	}
	
	/**
	 * Frees all resources associated with this EndPoint. </br>
	 * EndPoint should not be used after calling this method.
	 */
	public void close() {
		Bridge.releaseEndPoint(this);
	}

	@Override
	public int hashCode() {
		return Long.hashCode(nativeID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		EndPoint other = (EndPoint) obj;
		
		return nativeID == other.nativeID;
	}
	
	
}
