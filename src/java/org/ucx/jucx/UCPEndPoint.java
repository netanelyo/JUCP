package org.ucx.jucx;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class UCPEndPoint {
	
	private static Map<Long, UCPEndPoint> workers = new HashMap<>();
	
	private long nativeID;
	private UCPWorker localWorker;
	private UCPWorkerAddress remoteWorkerAddress;
	
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
	
	public void sendMessage(byte[] msg, long tag) {
		ByteBuffer buff = ByteBuffer.wrap(msg);
		UCPTagMsg tagMsg = UCPTagMsg.putOutMsg(this, tag, buff);
	}
	
	public long getNativeID() {
		return nativeID;
	}
	
	public UCPWorker getWorker() {
		return localWorker;
	}
	
//	public final class UCPRemoteWorkerAddress extends UCPWorkerAddress {
//		
//		public UCPRemoteWorkerAddress(byte[] remoteWorkerAddr) {
//			workerAddr = remoteWorkerAddr;
//		}
//	}
	
}
