/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
package org.ucx.jucx;

import java.nio.ByteBuffer;

import org.ucx.jucx.Worker.CompletionQueue;

import sun.nio.ch.DirectBuffer;

public class Bridge {
	static {
		LoadLibrary.loadLibrary("libucp.so"); 	// UCP library
		LoadLibrary.loadLibrary("libjucp.so"); 	// JUCP native library
	}

	private static native long createCtxNative(long features, long fieldMask);

	static long createCtx(UCPParams params) {
		return createCtxNative(params.getFeatures(), params.getFieldMask());
	}

	private static native void closeCtxNative(long ptr);

	static void closeCtx(Context context) {
		closeCtxNative(context.getNativeID());
	}

	private static native long createWorkerNative(long ctxID, int maxComp,
	        CompletionQueue compQueue);

	static long createWorker(long ctxID, int maxCompletions,
	        CompletionQueue compQueue) {
		return createWorkerNative(ctxID, maxCompletions, compQueue);
	}

	static native void testerNative(byte[] arr, long id);

	private static native byte[] getWorkerAddressNative(long workerID,
	        long[] ret);

	static byte[] getWorkerAddress(long workerNativeID, long[] ret) {
		return getWorkerAddressNative(workerNativeID, ret);
	}

	private static native void releaseWorkerNative(long workerID,
	        long workerAddrID);

	static void releaseWorker(Worker worker) {
		releaseWorkerNative(worker.getNativeID(),
		        worker.getAddress().getNativeID());
	}

	private static native int recvMsgAsyncNative(long workerID, long tag,
	        long tagMask, long addr, int msgLength, long reqID);

	private static native int recvMsgAsyncNative(long workerID, long tag,
	        long tagMask, byte[] msg, int msgLength, long reqID);

	static int recvMsgAsync(Worker worker, long tag, long tagMask,
	        ByteBuffer msg, int msgLen, long reqID) {
		return recvMsgAsyncNative(worker.getNativeID(), tag, tagMask,
		        ((DirectBuffer) msg).address(), msgLen, reqID);
	}

	static int recvMsgAsync(Worker worker, long tag, long tagMask, byte[] array,
	        int msgLen, long reqID) {
		return recvMsgAsyncNative(worker.getNativeID(), tag, tagMask, array,
		        msgLen, reqID);
	}

	private static native long createEpNative(long workerID, byte[] remoteAddr);

	static long createEndPoint(Worker worker, WorkerAddress addr) {
		return createEpNative(worker.getNativeID(), addr.getWorkerAddress());
	}

	private static native int sendMsgAsyncNative(long epID, long workerID,
	        long tag, long addr, int msgLength, long reqID);

	private static native int sendMsgAsyncNative(long epID, long workerID,
	        long tag, byte[] msg, int msgLength, long reqID);

	static int sendMsgAsync(EndPoint ep, long tag, ByteBuffer msg,
	        int msgLength, long reqID) {
		return sendMsgAsyncNative(ep.getNativeID(),
		        ep.getWorker().getNativeID(), tag,
		        ((DirectBuffer) msg).address(), msgLength, reqID);
	}

	static int sendMsgAsync(EndPoint ep, long tag, byte[] msg, int msgLength,
	        long reqID) {
		return sendMsgAsyncNative(ep.getNativeID(),
		        ep.getWorker().getNativeID(), tag, msg, msgLength, reqID);
	}

	private static native void releaseEndPointNative(long epID);

	static void releaseEndPoint(EndPoint ucpEndPoint) {
		releaseEndPointNative(ucpEndPoint.getNativeID());
	}

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

	private static native int workerWaitNative(long workerID, int numEvents);

	static int workerWait(Worker worker, int numEvents) {
		return workerWaitNative(worker.getNativeID(), numEvents);
	}

	private static native void workerFlushNative(long workerID, int outstandingRequests);
	
	static void workerFlush(Worker worker, int outstandingRequests) {
		workerFlushNative(worker.getNativeID(), outstandingRequests);
	}

}
