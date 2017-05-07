package org.ucx.jucx;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class UCPEndPoint {
	
	private static Map<Long, UCPEndPoint> workers = new HashMap<>();
	
	private long 				nativeID;
	private UCPWorker 			localWorker;
	private UCPWorkerAddress 	remoteWorkerAddress;
	
	public UCPEndPoint(UCPWorker worker, UCPWorkerAddress addr) {
		localWorker = worker;
		remoteWorkerAddress = addr;
		nativeID = Bridge.createEndPoint(worker, remoteWorkerAddress);
	}
	
	public static UCPEndPoint getInstance(UCPWorker worker, UCPWorkerAddress addr) {
		Long workerID = worker.getNativeID();
		if (workers.containsKey(workerID)){
			System.out.println("Single EP per Worker allowed");
			return workers.get(workerID);
		}
		else
			return new UCPEndPoint(worker, addr);
	}
	
	/*
	 * Get ByteBuffer as an arg (overload)
	 * Check if direct - maybe allocate direct if user sent indirect
	 */
	
	public void sendMessage(byte[] msg, long tag) {
		ByteBuffer buff = ByteBuffer.allocateDirect(msg.length);
		buff.put(msg); //TODO
		UCPTagMsg tagMsg = UCPTagMsg.putOutMsg(this, tag, buff);
	}
	
	public void sendMessage(ByteBuffer msg, long tag) {
		
	}
	
	public long getNativeID() {
		return nativeID;
	}
	
	public UCPWorker getWorker() {
		return localWorker;
	}
	
	public void free() {
		Bridge.releaseEndPoint(this);
	}
}
