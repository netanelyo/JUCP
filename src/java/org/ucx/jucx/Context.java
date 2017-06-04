package org.ucx.jucx;

public class Context {
	// Singleton - in order to enforce the one per process suggestion
	private static Context ctxInstance = null;
	
	public UCPParams params;
	private long nativeID;
	
	private Context(UCPParams params) {
		this.params = params;
		nativeID = Bridge.createCtx(params);
	}
	
	public static Context getInstance(UCPParams params) {
		if (ctxInstance == null) {
			ctxInstance = new Context(params);
		}
		
		return ctxInstance;
	}

	public long getNativeID() {
		return nativeID;
	}

	//TODO - getParams
}
