/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
package org.ucx.jucx;

/**
 * Application context that holds the UCP communication instance's global
 * information. Represents a single communication instance.
 */
public class Context {
	// Singleton - in order to enforce the one per process suggestion
	private static Context CTX = null;
	
	private UCPParams params;
	private long nativeID;
	
	private Context(UCPParams params, long nativeID) {
		this.params = params;
		this.nativeID = nativeID;
	}
	
	/**
	 * Retrieves the application context (singleton).
	 * 
	 * @param 	params
	 * 			the UCP parameters to be passed to context initialization.
	 * 
	 * @return 	application context
	 */
	public static Context getInstance(UCPParams params) {
		if (CTX == null) {
			long nativeID = Bridge.createCtx(params);
			if (nativeID != 0)
				CTX = new Context(params, nativeID);
		}
		
		//TODO: verify context params
		return CTX;
	}
	
	/**
	 * Releases (native) Context
	 */
	public void close() {
		CTX = null;
		Bridge.closeCtx(this);
	}

	/**
	 * Getter for native pointer as long.
	 * 
	 * @return long integer representing native pointer
	 */
	public long getNativeID() {
		return nativeID;
	}
}
