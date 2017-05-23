package org.ucx.jucx;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class TagMsg {
	
	public enum BufferType {
		DIRECT, INDIRECT
	}
	
	/*
	 * Ideas:
	 * 	1. Allocate a ByteBuffer and reuse it (pool?)
	 * 	2. Request from the user Direct BB
	 * 	3. For small messages - non-direct might be better
	 * 	4. 
	 */

	private ByteBuffer 	msg;
	private long 		tag;
	private int 		capacity;
	private BufferType	type;
	
	public TagMsg(long tag, int cap, BufferType type) {
		this.tag = tag;
		capacity = cap;
		this.type = type;
		allocateBuffer(type);	
	}
	
	private TagMsg(Worker worker, long tag) {
		this.tag = tag;
		msg = Bridge.recvMsgNb(worker, tag);
	}
	
	private void allocateBuffer(BufferType type) {
		switch (type) {
		case DIRECT:
			this.msg = ByteBuffer.allocateDirect(capacity);
			break;

		case INDIRECT:
			this.msg = ByteBuffer.allocate(capacity);
			break;
		
		default:
			break;
		}
	}
	
	//TODO
//	private UCPTagMsg(UCPEndPoint ep, long tag, ByteBuffer msg) {
//		this.msg = msg;
//		this.tag = tag;
//		Bridge.sendMsgNb(ep, tag, this.msg);
//	}
	
	public static TagMsg getInMsg(Worker worker, long tag) {
		return new TagMsg(worker, tag);
	}
	
//	public static void sendMsg(UCPEndPoint ep, long tag, ByteBuffer msg) {
//		new UCPTagMsg(ep, tag, msg);
//	}
	
	private String getByteBufferAsString(ByteBuffer buff) {
		byte[] tmpBuff = new byte[buff.remaining()];
		buff.get(tmpBuff);
		return new String(tmpBuff, Charset.forName("US-ASCII"));
	}
	
	public int getInt() {
		return msg.getInt();
	}
	
	public String getMsgAsString() {
		return getByteBufferAsString(msg);
	}
	
	public long getTag() {
		return this.tag;
	}
	
	public int getCapacity() {
		return this.capacity;
	}
	
	public BufferType getType() {
		return this.type;
	}
	
	public ByteBuffer getBuffer() {
		return this.msg;
	}
	
}
