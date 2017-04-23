package org.ucx.jucx;

public class UCPParams {

	private long features;
	private long requestSize;
//	private //init and cleanup callback
	private long fieldMask;
	
	public UCPParams(long feats, long reqSize, long mask) {
		features 	= feats;
		requestSize = reqSize;
		fieldMask 	= mask;
		
		// TODO callbacks
	}

	public long getFeatures() {
		return features;
	}

	public long getRequestSize() {
		return requestSize;
	}

	public long getFieldMask() {
		return fieldMask;
	}
	
}
