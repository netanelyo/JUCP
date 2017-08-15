/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
package org.ucx.jucx;

public class UCPParams {
	/**
	 * Features flags to be used in UCPParams instantiation
	 */
	public static final class Features {
		public static final long UCP_FEATURE_TAG 	= 0x1;
		public static final long UCP_FEATURE_RMA 	= 0x2;
		public static final long UCP_FEATURE_AMO32 	= 0x4;
		public static final long UCP_FEATURE_AMO64 	= 0x8;
		public static final long UCP_FEATURE_WAKEUP = 0x10;
		
		private Features() {}
	}
	
	/**
	 * FieldMask flags to be used in UCPParams instantiation
	 */
	public static final class FieldMask {
		public static final long UCP_PARAM_FIELD_FEATURES 			= 0x1;
		public static final long UCP_PARAM_FIELD_REQUEST_SIZE 		= 0x2;
		public static final long UCP_PARAM_FIELD_REQUEST_INIT 		= 0x4;
		public static final long UCP_PARAM_FIELD_REQUEST_CLEANUP 	= 0x8;
		public static final long UCP_PARAM_FIELD_TAG_SENDER_MASK 	= 0x10;
		public static final long UCP_PARAM_FIELD_MT_WORKERS_SHARED 	= 0x20;
		public static final long UCP_PARAM_FIELD_ESTIMATED_NUM_EPS 	= 0x40;
		
		private FieldMask() {}
	}

	private long features;
	private long fieldMask;
	
	/**
	 * Creates a new object representing ucp_params
	 * 
	 * @param 	feats
	 * 			Features flags
	 * 
	 * @param 	mask
	 * 			Field mask flags
	 */
	public UCPParams(long feats, long mask) {
		features 	= feats;
		fieldMask 	= mask;
	}

	long getFeatures() {
		return features;
	}

	long getFieldMask() {
		return fieldMask;
	}
	
}
