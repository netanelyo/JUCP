package org.ucx.jucx;

import java.nio.ByteBuffer;

public class UCPTagMsg {

	private ByteBuffer in;
	private ByteBuffer out;
	
	private UCPTagMsg(UCPWorker worker, long tag) {
		in = Bridge.recvMsgNb(worker, tag);
	}
	
	private UCPTagMsg(UCPEndPoint ep, long tag, ByteBuffer msg) {
		out = msg;
		Bridge.sendMsgNb(ep, tag, msg);
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
	
//	public ByteBuffer getInBuff(){
//		return msg;
//	}

}
