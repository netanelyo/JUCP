/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
package org.ucx.jucx;

import java.nio.ByteBuffer;

public class EndPoint {
	private long 			nativeID;
	private Worker 			localWorker;
	private WorkerAddress 	remoteWorkerAddress;
	
	EndPoint(Worker worker, WorkerAddress addr) {
		localWorker = worker;
		remoteWorkerAddress = addr;
		nativeID = Bridge.createEndPoint(worker, remoteWorkerAddress);
	}
	
	public int sendMessageAsync(long tag, ByteBuffer msg, int msgLen, long reqID) {
		int cnt = localWorker.sendMessage(this, tag, msg, msgLen, reqID);
		localWorker.setCounter(cnt);
		return cnt;
	}
	
	// dontcare request id
	public int sendMessageAsync(long tag, ByteBuffer msg, int msgLen) {
		return sendMessageAsync(tag, msg, msgLen, Worker.DEFAULT_REQ_ID);
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
	
	public Worker getWorker() {
		return localWorker;
	}
	
	public void close() {
		localWorker.removeEndPoint(this);
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
