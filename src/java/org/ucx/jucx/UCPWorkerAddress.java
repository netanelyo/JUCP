package org.ucx.jucx;

import java.io.Serializable;

public abstract class UCPWorkerAddress implements Serializable {
	/**
	 * TODO
	 */
	private static final long serialVersionUID = 1L;
	
	protected byte[] workerAddr;
	
	public byte[] getWorkerAddr() {
		return workerAddr.clone();
	}
	
	
}