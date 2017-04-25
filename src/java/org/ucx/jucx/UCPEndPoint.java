package org.ucx.jucx;

import java.nio.ByteBuffer;

import org.ucx.jucx.UCPWorker.UCPWorkerAddress;

public class UCPEndPoint {
	
	private long nativeID;
	private UCPWorker localWorker;
	private UCPWorkerAddress remoteWorkerAddress;
	
	public UCPEndPoint(UCPWorker worker, UCPWorkerAddress addr) {
		nativeID = Bridge.createEndPoint(worker, addr);
		localWorker = worker;
		remoteWorkerAddress = addr;
	}
	
	public void sendMessage(byte[] msg, long tag) {
		ByteBuffer buff = ByteBuffer.wrap(msg);
		UCPTagMsg tagMsg = UCPTagMsg.putOutMsg(this, tag, buff);
	}
	
	public long getNativeID() {
		return nativeID;
	}
	
}
