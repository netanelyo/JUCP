package org.ucx.jucx;

import java.nio.ByteBuffer;

public class UCPTagMsg {
	
	/*
	 * Ideas:
	 * 	1. Allocate a ByteBuffer and reuse it (pool?)
	 * 	2. Request from the user Direct BB
	 * 	3. For small messages - non-direct might be better
	 * 	4. 
	 */

	private ByteBuffer 	msg;
	private long 		tag;
	
	private UCPTagMsg(UCPWorker worker, long tag) {
		msg = Bridge.recvMsgNb(worker, tag);
		this.tag = tag;
	}
	
	private UCPTagMsg(UCPEndPoint ep, long tag, ByteBuffer msg) {
		this.msg = msg;
		this.tag = tag;
		Bridge.sendMsgNb(ep, tag, this.msg);
	}
	
	public static UCPTagMsg getInMsg(UCPWorker worker, long tag) {
		return new UCPTagMsg(worker, tag);
	}
	
	public static UCPTagMsg putOutMsg(UCPEndPoint ep, long tag, ByteBuffer msg) {
		return new UCPTagMsg(ep, tag, msg);
	}
	
	private String getByteBufferAsString(ByteBuffer buff) {
		StringBuffer str = new StringBuffer();
		
		while (buff.hasRemaining())
			str.append((char) buff.get());
		
		buff.rewind();
		
		return str.toString();
	}
	
	public String getMsgAsString() {
		return getByteBufferAsString(msg);
	}
	
	public long getTag() {
		return this.tag;
	}
	
}
