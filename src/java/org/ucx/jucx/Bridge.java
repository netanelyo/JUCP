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

	/*
	 * TODO
	 * 
	 * unused...
	 */
	private static native int probeAndProgressNative(long worker, long tag, long[] tagMsg);

	static int probeAndProgress(Worker worker, long tag, long[] tagMsg) {
		return probeAndProgressNative(worker.getNativeID(), tag, tagMsg);
	}

	private static native ByteBuffer recvMsgNbNative(long workerID, long tag);

	static ByteBuffer recvMsgNb(Worker worker, long tag) {
		return recvMsgNbNative(worker.getNativeID(), tag);
	}

	private static native long createEpNative(long workerID, byte[] remoteAddr);

	static long createEndPoint(Worker worker, WorkerAddress addr) {
		return createEpNative(worker.getNativeID(), addr.getWorkerAddr());
	}

	private static native int sendMsgNative(long epID, long workerID, long tag, Object msg,
											int msgLength, boolean sync, boolean direct, long reqID);
	
	static int sendMsg(EndPoint ep, TagMsg msg, boolean sync, boolean direct, long reqID) {
		return sendMsgNative(ep.getNativeID(), ep.getWorker().getNativeID(),
						msg.getTag(), msg.getBuffer(), msg.getCapacity(), sync, direct, reqID);
	}
	
	private static native void releaseEndPointNative(long epID);

	static void releaseEndPoint(EndPoint ucpEndPoint) {
		releaseEndPointNative(ucpEndPoint.getNativeID());
	}

	private static native int progressWorkerNative(long workerID, int maxEvents);
	
	static int progressWorker(Worker ucpWorker, int maxEvents) {
		return progressWorkerNative(ucpWorker.getNativeID(), maxEvents);
	}

}
