package org.ucx.jucx;

import java.nio.ByteBuffer;

public class UCPTagMsg {

	private ByteBuffer msg;
	private int msgSize;
	private long tagMsgID;
	
	public UCPTagMsg(UCPWorker worker, long tag) {
		long[] retValue = new long[1];
		msgSize = Bridge.probeAndProgress(worker, tag, retValue);
		tagMsgID = retValue[0];
		msg = ByteBuffer.allocateDirect(msgSize);
	}
	
	public int getMsgSize() {
		return msgSize;
	}

	public String getMsg() {
		return msg.asCharBuffer().toString();
//		return new String(msg.array());
	}
	
	public ByteBuffer getBuffer(){
		return msg;
	}

	public long getTagMsgID() {
		return tagMsgID;
	}
	
}
