package org.ucx.jucx;

public class UCPContext {
	
	// Singleton - in order to enforce the one per process demand
	private static UCPContext ctxInstance = null;
	
	public UCPParams params;
	private long nativeID;
	
	private UCPContext(UCPParams params) {
		this.params = params;
		nativeID = Bridge.createCtx(params);
	}
	
	public static UCPContext getInstance(UCPParams params) {
		if (ctxInstance == null) {
			ctxInstance = new UCPContext(params);
		}
		
		return ctxInstance;
	}

	public long getNativeID() {
		return nativeID;
	}

	//TODO - getParams
}
