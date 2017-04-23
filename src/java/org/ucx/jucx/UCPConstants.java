package org.ucx.jucx;

public abstract class UCPConstants {
	
	public static abstract class UCPFeature {
		public static final long UCP_FEATURE_TAG 	= 0x1;
		public static final long UCP_FEATURE_RMA 	= 0x2;
		public static final long UCP_FEATURE_AMO32 	= 0x4;
		public static final long UCP_FEATURE_AMO64 	= 0x8;
		public static final long UCP_FEATURE_WAKEUP = 0x10;
	}
	
	public static abstract class UCPParamsField {
		public static final long UCP_PARAM_FIELD_FEATURES 			= 0x1;
		public static final long UCP_PARAM_FIELD_REQUEST_SIZE 		= 0x2;
		public static final long UCP_PARAM_FIELD_REQUEST_INIT 		= 0x4;
		public static final long UCP_PARAM_FIELD_REQUEST_CLEANUP 	= 0x8;
		public static final long UCP_PARAM_FIELD_TAG_SENDER_MASK 	= 0x10;
		public static final long UCP_PARAM_FIELD_MT_WORKERS_SHARED 	= 0x20;
		public static final long UCP_PARAM_FIELD_ESTIMATED_NUM_EPS 	= 0x40;
	}
	
}