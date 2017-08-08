/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
package org.ucx.jucx;

import java.nio.ByteBuffer;

import org.ucx.jucx.Worker.CompletionQueue;

//import org.accelio.jxio.EventQueueHandler;
//import org.apache.commons.logging.LogFactory;

public class Bridge {
	
	// private static final Log LogFromNative =
	// LogFactory.getLog("LogFromNative");
	// private static final Log LOGBridge = LogFactory.getLog(Bridge.class
	// .getCanonicalName());
	// private static final String version_jxio;
	// private static final String version_xio;

	// private static ConcurrentMap<Long, EventQueueHandler> mapIdEQHObject =
	// new ConcurrentHashMap<Long, EventQueueHandler>();

	static {
		LoadLibrary.loadLibrary("libucp.so"); // UCP library
		LoadLibrary.loadLibrary("libjucp.so"); // JUCP native library
		// version_jxio = getVersionNative();
		// version_xio = getVersionAccelIONative();
		// setNativeLogLevel(getLogLevel());
	}

	private static native long createCtxNative(long features, long fieldMask);

	static long createCtx(UCPParams params) {
		/* Initializing ucp_context_h */
		return createCtxNative(params.getFeatures(), params.getFieldMask());
	}

	private static native void closeCtxNative(long ptr);

	static void closeCtx(Context context) {
		closeCtxNative(context.getNativeID());
	}

	private static native long createWorkerNative(long ctxID, int maxComp, CompletionQueue compQueue);

	static long createWorker(long ctxID, int maxCompletions, CompletionQueue compQueue) {
		return createWorkerNative(ctxID, maxCompletions, compQueue);
	}

	static native void testerNative(byte[] arr, long id);

	private static native byte[] getWorkerAddressNative(long workerID, long[] ret);

	static byte[] getWorkerAddress(long workerNativeID, long[] ret) {
		return getWorkerAddressNative(workerNativeID, ret);
	}

	private static native void releaseWorkerNative(long workerID, long workerAddrID);

	static void releaseWorker(Worker worker) {
		releaseWorkerNative(worker.getNativeID(), worker.getAddress().getNativeID());
	}

	private static native int recvMsgAsyncNative(long workerID, long tag, long tagMask, ByteBuffer msg,
			int msgLength, long reqID);
	
	private static native int recvMsgAsyncNative(long workerID, long tag, long tagMask, byte[] msg,
			int msgLength, long reqID);
	
	static int recvMsgAsync(Worker worker, long tag, long tagMask, ByteBuffer msg, int msgLen, long reqID) {
		return recvMsgAsyncNative(worker.getNativeID(), tag, tagMask, msg, msgLen, reqID);
	}
	
	static int recvMsgAsync(Worker worker, long tag, long tagMask, byte[] array, int msgLen, long reqID) {
		return recvMsgAsyncNative(worker.getNativeID(), tag, tagMask, array, msgLen, reqID);
	}

	private static native long createEpNative(long workerID, byte[] remoteAddr);

	static long createEndPoint(Worker worker, WorkerAddress addr) {
		return createEpNative(worker.getNativeID(), addr.getWorkerAddress());
	}

	private static native int sendMsgAsyncNative(long epID, long workerID, long tag, long addr,
											int msgLength, long reqID);
	
	private static native int sendMsgAsyncNative(long epID, long workerID, long tag, byte[] msg,
			int msgLength, long reqID);
	
	static int sendMsgAsync(EndPoint ep, long tag, long addr, int msgLength, long reqID) {
		return sendMsgAsyncNative(ep.getNativeID(), ep.getWorker().getNativeID(),
																	tag, addr, msgLength, reqID);
	}
	
	static int sendMsgAsync(EndPoint ep, long tag, byte[] msg, int msgLength, long reqID) {
		return sendMsgAsyncNative(ep.getNativeID(), ep.getWorker().getNativeID(),
																	tag, msg, msgLength, reqID);
	}
	
	private static native void releaseEndPointNative(long epID);

	static void releaseEndPoint(EndPoint ucpEndPoint) {
		releaseEndPointNative(ucpEndPoint.getNativeID());
	}

//	private static native int progressWorkerNative(long workerID, int maxEvents);
//	
//	static int progressWorker(Worker ucpWorker, int maxEvents) {
//		return progressWorkerNative(ucpWorker.getNativeID(), maxEvents);
//	}

	private static native int progressWorkerNative(long workerID);
	
	static int progressWorker(Worker worker) {
		return progressWorkerNative(worker.getNativeID());
	}

	private static native long getTimeNative();
	
	public static long getTime() {
		return getTimeNative();
	}
	
	private static native long getCycleNative();
	
	public static long getCycle() {
		return getCycleNative();
	}

}













