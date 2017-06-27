package org.ucx.jucx.tests;

import org.ucx.jucx.Context;
import org.ucx.jucx.EndPoint;
import org.ucx.jucx.Worker;

public class PerftestUtils {

	public static enum PerfTestType {
		UCP_TEST_PINGPONG,	// for latency test
		UCP_TEST_STREAM		// for BW test
	}
	
	public static enum PerfDataType {
		UCP_DATATYPE_CONTIG,
		UCP_DATATYPE_IOV,
	}
	
	public static class PerfParams {
//		ucs_thread_mode_t thread_mode; /* Thread mode for communication objects */
//		unsigned thread_count; /* Number of threads in the test program */
//		ucs_async_mode_t async_mode; /* how async progress and locking is done */
//		ucx_perf_wait_mode_t wait_mode; /* How to wait */
//		unsigned flags; /* See ucx_perf_test_flags. */
//
//		size_t *msg_size_list; /* Test message sizes list. The size
//		 of the array is in msg_size_cnt */
//		size_t msg_size_cnt; /* Number of message sizes in
//		 message sizes list */
//		size_t iov_stride; /* Distance between starting address
//		 of consecutive IOV entries. It is
//		 similar to UCT uct_iov_t type stride */
		
		PerfTestType 	testType; 		// Test communication type
		int 			maxOutstanding;	// Maximal number of outstanding sends
		int 			warmupIter;		// Number of warm-up iterations
		int 			maxIter;		// Iterations limit, 0 - unlimited
		int 			maxTimeSecs;	// Time limit (seconds), 0 - unlimited
		long 			reportInterval;	// Interval at which to call the report callback
		PerfDataType	sendType;		
		PerfDataType	recvType;
		
//		<T extends Number>  T sum(T a, T b) { return a.doubleValue();}
		
		//TODO: rte???
		
		
	}
	
	public static class PerfTime {
		int 	startTime;
		int 	prevTime;
		int 	endTime;
		long 	reportInterval;
	}
	
	public static class UcpObjects {
		Context 		ctx;
		Worker 			worker;
		EndPoint		endPoint;
		
	}
}







