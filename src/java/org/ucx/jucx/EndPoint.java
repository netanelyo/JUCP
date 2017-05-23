package org.ucx.jucx;

import java.util.HashMap;
import java.util.Map;

public class EndPoint {
	private static int REQUEST_ERROR 	= -1;
	private static int REQUEST_PENDING 	= 0;
	private static int REQUEST_COMPLETE = 1;
	private static Map<Long, EndPoint> workerToEP = new HashMap<>();
	
	private long 				nativeID;
	private Worker 			localWorker;
	private WorkerAddress 	remoteWorkerAddress;
	
	private EndPoint(Worker worker, WorkerAddress addr) {
		localWorker = worker;
		remoteWorkerAddress = addr;
		nativeID = Bridge.createEndPoint(worker, remoteWorkerAddress);
	}
	
	private EndPoint(Worker worker, byte[] addr) {
		localWorker = worker;
		remoteWorkerAddress = new WorkerAddress(addr);
		nativeID = Bridge.createEndPoint(worker, remoteWorkerAddress);
	}
	
	public static EndPoint getInstance(Worker worker, WorkerAddress addr) {
		Long workerID = worker.getNativeID();
		if (workerToEP.containsKey(workerID)){
			System.out.println("Single EP per Worker allowed");
			return workerToEP.get(workerID);
		}
		else
			return new EndPoint(worker, addr);
	}
	
	public static EndPoint getInstance(Worker worker, byte[] addr) {
		Long workerID = worker.getNativeID();
		if (workerToEP.containsKey(workerID)){
			System.out.println("Single EP per Worker allowed");
			return workerToEP.get(workerID);
		}
		else
			return new EndPoint(worker, addr);
	}
	
//	/**
//	 * TODO
//	 * 
//	 * @param msg
//	 * @param tag
//	 */
//	public void sendMessage(byte[] msg, long tag) {
//		ByteBuffer buff = ByteBuffer.allocateDirect(msg.length);
//		buff.put(msg); //TODO
//		UCPTagMsg.sendMsg(this, tag, buff);
//	}		
//	
//	/**
//	 * The method sends a message through native ucp API.
//	 * It's recommended to pass a Direct ByteBuffer as msg, to avoid copy.
//	 * 
//	 * @param msg
//	 * @param tag
//	 */
//	public void sendMessage(ByteBuffer msg, long tag) {
//		if (msg.isDirect()) {
//			UCPTagMsg.sendMsg(this, tag, msg);
//		}
//		else {
//			sendMessage(msg.array(), tag);
//		}
//	}
	
	private int sendMessage(TagMsg msg, boolean sync, long reqID) {
		int sent = 0;
		
		switch (msg.getType()) {
		case DIRECT:
			sent = Bridge.sendMsg(this, msg, sync, true, reqID);
			break;
			
		case INDIRECT:
			sent = Bridge.sendMsg(this, msg, sync, false, reqID);
			break;

		default:
			break;
		}
		
		return sent;
	}
	
	private int commonSend(TagMsg msg, boolean sync, long reqID) {
		int cnt = sendMessage(msg, sync, reqID);
		localWorker.setCounter(cnt);
		return cnt;
	}
	
	public int sendMessageAsync(TagMsg msg, long reqID) {
		return commonSend(msg, false, reqID);
	}
	
	public int sendMessageSync(TagMsg msg, long reqID) {
		return commonSend(msg, true, reqID);
	}
	
	// dontcare request id
	public int sendMessageAsync(TagMsg msg) {
		return commonSend(msg, false, 0);
	}
	
	public int sendMessageSync(TagMsg msg) {
		return commonSend(msg, true, 0);
	}
	
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
