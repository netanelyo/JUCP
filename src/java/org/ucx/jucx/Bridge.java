/*
 * * Copyright (C) 2013 Mellanox Technologies** Licensed under the Apache License, Version 2.0 (the "License");* you may
 * not use this file except in compliance with the License.* You may obtain a copy of the License at:**
 * http://www.apache.org/licenses/LICENSE-2.0** Unless required by applicable law or agreed to in writing, software*
 * distributed under the License is distributed on an "AS IS" BASIS,* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,*
 * either express or implied. See the License for the specific language* governing permissions and limitations under the
 * License.*
 */
package org.ucx.jucx;

import java.nio.ByteBuffer;

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
		LoadLibrary.loadLibrary("libucp.so"); // Accelio library
		LoadLibrary.loadLibrary("libjucp.so"); // JXIO native library
		// version_jxio = getVersionNative();
		// version_xio = getVersionAccelIONative();
		// setNativeLogLevel(getLogLevel());
	}

	private static native long createCtxNative(UCPParams params);

	static long createCtx(UCPParams params) {
		/* Initializing ucp_context_h */
		return createCtxNative(params);

		// Bridge.mapIdEQHObject.put(dataFromC.getPtrCtx(), eqh);
	}

	private static native void closeCtxNative(long ptr);

	static void closeCtx(final long ptrCtx) {
		closeCtxNative(ptrCtx);
		// Bridge.mapIdEQHObject.remove(ptrCtx);
	}

	private static native long createWorkerNative(long ctxID, ByteBuffer compBuff);

	static long createWorker(long ctxID, ByteBuffer completionBuff) {
		return createWorkerNative(ctxID, completionBuff);
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
		return createEpNative(worker.getNativeID(), addr.getWorkerAddr());
	}

	private static native int sendMsgAsyncNative(long epID, long workerID, long tag, ByteBuffer msg,
											int msgLength, long reqID);
	
	private static native int sendMsgAsyncNative(long epID, long workerID, long tag, byte[] msg,
			int msgLength, long reqID);
	
	static int sendMsgAsync(EndPoint ep, long tag, ByteBuffer msg, int msgLength, long reqID) {
		return sendMsgAsyncNative(ep.getNativeID(), ep.getWorker().getNativeID(),
																	tag, msg, msgLength, reqID);
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

}
