package org.ucx.jucx;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class EndPoint {
	private long 			nativeID;
	private Worker 			localWorker;
	private WorkerAddress 	remoteWorkerAddress;
	
	public EndPoint(Worker worker, WorkerAddress addr) {
		localWorker = worker;
		remoteWorkerAddress = addr;
		nativeID = Bridge.createEndPoint(worker, remoteWorkerAddress);
	}
	
	public EndPoint(Worker worker, byte[] addr) {
		localWorker = worker;
		remoteWorkerAddress = new WorkerAddress(addr);
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
	
	public void free() {
		Bridge.releaseEndPoint(this);
	}
}
