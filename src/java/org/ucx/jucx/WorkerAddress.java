/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
package org.ucx.jucx;

import java.io.Serializable;

/**
 * An object representing a Worker's native address structure.
 * Implements {@link Serializable} so one can send an instance over
 * ObjectOutputStream.
 */
public final class WorkerAddress implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final byte[] 	workerAddr;
	private long 			nativeID;
	
	/**
	 * Getter for WorkerAddress as a byte array
	 * 
	 * @return clone of address (for safety reasons)
	 */
	public byte[] getWorkerAddress() {
		return workerAddr.clone();
	}
	
	/**
	 * Creates a new WorkerAddress object - holding the worker's UCP address
	 * 
	 * @param 	worker
	 * 			Worker that (this) WorkerAddress represents his address
	 */
	WorkerAddress(Worker worker) {
		long[] retValue = new long[1];
		workerAddr = Bridge.getWorkerAddress(worker.getNativeID(), retValue);
		nativeID = retValue[0];
	}
	
	WorkerAddress(byte[] addr) {
		nativeID = -1;
		workerAddr = addr;
	}

	long getNativeID() {
		return nativeID;
	}
	
	
}