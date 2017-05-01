package org.ucx.jucx;

import java.nio.ByteBuffer;

public class UCPTagMsg {

	private ByteBuffer 	in;
	private ByteBuffer 	out;
	private long 		tag;
	
	private UCPTagMsg(UCPWorker worker, long tag) {
		in = Bridge.recvMsgNb(worker, tag);
		this.tag = tag;
	}
	
	private UCPTagMsg(UCPEndPoint ep, long tag, ByteBuffer msg) {
		out = msg;
		this.tag = tag;
		Bridge.sendMsgNb(ep, tag, out);
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
	
	public String getInAsString() {
		return getByteBufferAsString(in);
	}
	
	public String getOutAsString() {
		return getByteBufferAsString(out);
	}
	
	public long getTag() {
		return this.tag;
	}
	
//	public ByteBuffer getInBuff(){
//		return msg;
//	}
	
	
	

}
