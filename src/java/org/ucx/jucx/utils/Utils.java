package org.ucx.jucx.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.ucx.jucx.Bridge;

public class Utils {

	public static String getByteBufferAsString(ByteBuffer buff) {
		int pos = buff.position();
		byte[] tmpBuff = new byte[buff.remaining()];
		buff.get(tmpBuff);
		buff.position(pos);
		return new String(tmpBuff, Charset.forName("US-ASCII"));
	}

	public static abstract class Time {
		
		public static long nanoTime() {
			return Bridge.getTime();
		}

		public static long cycle() {
			return Bridge.getCycle();
		}
		
		public double toUsecs(long time) {
			return time/1000.0;
		}
		
		public double toSecs(long time) {
			return time/1000000000.0;
		}
	}

	public static class Options {
		
		public static final char EOF = (char) -1;
		
		private String optsStr;
		private String[] args;
		private int optCnt;
		private Map<Character, Boolean> options;
		
		public String optArg;
		
		public Options(String opts, String[] args) {
			this.args = args;
			optsStr = opts;
			optCnt = 0;
			options = null;
			optArg = null;
		}
		
		private void parseArgs() {
			if (optsStr == null)
				return;
			
			int len = optsStr.length();
			for (int i = 0; i < len; i++) {
				char ch = optsStr.charAt(i);
				
				if (ch == ':')
					continue;
				
				if (i < len - 1 && optsStr.charAt(i + 1) == ':')
					options.put(ch, true);
				else
					options.put(ch, false);
			}
		}
		
		public char getOpt() {
			if (options == null) {
				options = new HashMap<>();
				parseArgs();
			}
			
			if (args == null || optCnt >= args.length)
				return EOF;
			
			char opt = args[optCnt++].charAt(1);
			if (!options.containsKey(opt))
				return opt;
			
			if (options.get(opt)) {
				optArg = args[optCnt++];
			}
			else
				optArg = null;
			
			return opt;
		}
	}

}
