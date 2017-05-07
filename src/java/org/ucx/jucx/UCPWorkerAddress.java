package org.ucx.jucx;

import java.io.Serializable;

public final class UCPWorkerAddress implements Serializable {
	/**
	 * TODO
	 */
	private static final long serialVersionUID = 1L;
	
	private final byte[] 	workerAddr;
	private long 			nativeID;
	
	public byte[] getWorkerAddr() {
		return workerAddr.clone();
	}
	
	UCPWorkerAddress(long workerNativeID) {
		long[] retValue = new long[1];
		workerAddr = Bridge.getWorkerAddress(workerNativeID, retValue);
		nativeID = retValue[0];
	}

	long getNativeID() {
		return nativeID;
	}
	
	
}