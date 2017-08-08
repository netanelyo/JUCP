package org.ucx.jucx.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Random;

public final class StringUtils {
	
	private StringUtils() {}

	public static String getByteBufferAsString(ByteBuffer buff) {
		int pos = buff.position();
		byte[] tmpBuff = new byte[buff.remaining()];
		buff.get(tmpBuff);
		buff.position(pos);
		return new String(tmpBuff, Charset.forName("US-ASCII"));
	}
	
	public static String generateRandomString(int size) {
		Random rand = new Random();
		byte[] arr = new byte[size];
		rand.nextBytes(arr);
		
		String gen = new String(arr, Charset.forName("US-ASCII"));
		
		return gen;
	}
}
