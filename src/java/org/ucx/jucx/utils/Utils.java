package org.ucx.jucx.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;

public final class Utils {
	
	private Utils() {}

	public static String getByteBufferAsString(ByteBuffer buff) {
		int pos = buff.position();
		byte[] tmpBuff = new byte[buff.remaining()];
		buff.get(tmpBuff);
		buff.position(pos);
		return new String(tmpBuff, Charset.forName("US-ASCII"));
	}
	
	public static byte[] generateRandomBytes(int size) {
		Random rand = new Random();
		byte[] arr = new byte[size];
		rand.nextBytes(arr);
		
		return arr;
	}
}
