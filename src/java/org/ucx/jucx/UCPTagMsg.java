package org.ucx.jucx;

import java.nio.ByteBuffer;

public class UCPTagMsg {

	private ByteBuffer msg;
	
	public UCPTagMsg(UCPWorker worker, long tag) {
		msg = Bridge.recvMsgNb(worker, tag);
	}
	
	public String getMsg() {
		StringBuffer str = new StringBuffer();
		
		while (msg.hasRemaining())
			str.append((char) msg.get());
		
		msg.rewind();
		
		return str.toString();
	}
	
	public ByteBuffer getBuffer(){
		return msg;
	}

}
