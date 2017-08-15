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
	
	private Context(UCPParams params, long natID) {
		this.params = params;
		nativeID = natID;
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
			long nat = Bridge.createCtx(params);
			if (nat != 0)
				CTX = new Context(params, nat);
		}
		
		return CTX;
	}
	
	/**
	 * Releases (native) Context
	 */
	public void close() {
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
