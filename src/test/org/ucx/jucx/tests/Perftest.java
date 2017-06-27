package org.ucx.jucx.tests;

import org.ucx.jucx.utils.Utils.Options;

public class Perftest {

	protected void usage() {
		StringBuffer str = new StringBuffer();
		String sep = System.lineSeparator();
		str.append(sep + "Options:" + sep);
		str.append("\t-p <port>          port to listen on (default: 12345)" + sep);
		str.append("\t-n <iterations>    number of iterations (default: 100000)" + sep);
		str.append("\t-s <size>          msg size in bytes (default: 64)" + sep);
		str.append("\t-t <tag>           request tag (default: 29592)" + sep);
		str.append("\t-v                 enable printing to System.out (default: no print)" + sep);
		str.append("\t-b                 bandwidth test (default: ping-pong)" + sep);
//		str.append("\t-f file            file to print all messages to (default System.out)" + sep);
		str.append("\t-h                 display this help and exit" + sep);
		System.out.println(str.toString());
	}
	
	protected void parseArgs(String[] args) {
		Options options = new Options("p:n:s:t:vhb", args);
		char opt;
		while ((opt = options.getOpt()) != Options.EOF) {
			String val = options.optArg;
			switch (opt) {
			case 'h':
				usage();
				System.exit(0);
				break;
				
			case 'p':
				port 	= Integer.parseInt(val);
				break;
				
			case 'n':
				iters 	= Integer.parseInt(val);
				break;
				
			case 's':
				size 	= Integer.parseInt(val);
				break;
				
			case 't':
				tag 	= Long.parseLong(val);
				break;
				
			case 'v':
				print 	= true;
				break;
				
			default:
				System.out.println("Invalid option. Exiting...");
				usage();
				System.exit(1);
				break;
			
			}
		}
	}
	
	public static void main(String[] args) {
		
	}
}
