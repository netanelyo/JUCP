package org.ucx.jucx.examples.helloworld;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.ucx.jucx.Context;
import org.ucx.jucx.UCPParams;
import org.ucx.jucx.UCPParams.Features;
import org.ucx.jucx.UCPParams.FieldMask;
import org.ucx.jucx.Worker;
import org.ucx.jucx.Worker.Callback;
import org.ucx.jucx.utils.Options;

public abstract class HelloWorld {
	
	protected 	int 		port 	= 12345;
	protected 	Worker		worker	= null;
	protected	ByteBuffer	buff	= null;
	protected	Options		options = null;
	private 	Context		ctx		= null;
	
	protected static final String MESSAGE = "UCP Client says hello!";
	
	protected HelloWorld() {
		long feats = Features.UCP_FEATURE_TAG;
		long mask = FieldMask.UCP_PARAM_FIELD_FEATURES 		| 
					FieldMask.UCP_PARAM_FIELD_REQUEST_INIT 	|
					FieldMask.UCP_PARAM_FIELD_REQUEST_SIZE;
		
		UCPParams params = new UCPParams(feats, mask);
		ctx = Context.getInstance(params);
		worker = new Worker(ctx, new HelloWorldCallback(), 100);
		
		buff = ByteBuffer.allocateDirect(MESSAGE.length());
	}
	
	protected void usage() {
		StringBuffer str = new StringBuffer();
		String sep = System.lineSeparator();
		str.append(sep + "Options:" + sep);
		str.append("\t-p <port>          TCP port (default: 12345)" + sep);
		str.append("\t-h                 display this help and exit" + sep);
		System.out.println(str.toString());
	}
	
	protected void parseArgs(String[] args) {
		options = new Options("p:h", args);
		char opt;
		while ((opt = options.getOpt()) != Options.EOF) {
			String val = options.optArg;
			switch (opt) {
			case 'h':
				usage();
				System.exit(0);
				break;
				
			case 'p':
				port = Integer.parseInt(val);
				break;
				
			default:
				System.out.println("Invalid option. Exiting...");
				usage();
				System.exit(1);
				break;
			}
		}
	}
	
	protected void close() {
		worker.close();
		ctx.close();
		System.out.println("[SUCCESS] Exiting...");
	}
	
	class HelloWorldCallback implements Callback {
		@Override
		public void requestHandle(long requestId) {}
	}

	protected abstract void connect() throws IOException, ClassNotFoundException;

	protected abstract void run();
}







