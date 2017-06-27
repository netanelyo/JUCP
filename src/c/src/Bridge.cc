/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */

#include "Bridge.h"

#include "UcpRequest.h"
#include "Worker.h"

extern "C" {
	#include <ucp/api/ucp.h>
	#include "ucs/time/time.h"
}

static jclass cached_cls;
static JavaVM *cached_jvm;

static jclass cls_data;
static jlong cached_ctx;
static jweak jweakBridge; // use weak global ref for allowing GC to unload & re-load the class and handles
static jclass jclassBridge; // just casted ref to above jweakBridge. Hence, has same life time
//static jfieldID fidPtr;
static jfieldID fidBuf;
//static jfieldID fidError;
//static jmethodID jmethodID_logToJava; // handle to java cb method
//static jmethodID jmethodID_requestForBoundMsgPool;

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void* reserved)
{
	cached_jvm = jvm;
	JNIEnv *env;

	// direct buffer requires java 1.4
	if (jvm->GetEnv((void **)&env, JNI_VERSION_1_8)) {
		std::cout << "JNI version 1.4 or higher required" << std::endl;
		return JNI_ERR;
	}

	cached_cls = env->FindClass("org/ucx/jucx/Bridge");
	if (cached_cls == NULL) {
		return JNI_ERR;
	}

	// keeps the handle valid after function exits, but still, use weak global ref
	// for allowing GC to unload & re-load the class and handles
	jweakBridge = env->NewWeakGlobalRef(cached_cls);
	if (jweakBridge == NULL) {
		return JNI_ERR;
	}
	jclassBridge = (jclass)jweakBridge;

	cls_data = env->FindClass("org/ucx/jucx/Worker$CompletionQueue");
	if (cls_data == NULL) {
		std::cout << "java class was NOT found" << std::endl;
		return JNI_ERR;
	}
//
//	if (fidPtr == NULL) {
//		fidPtr = env->GetFieldID(cls_data, "ptrCtx", "J");
//		if (fidPtr == NULL) {
//			bridge_print_error("could not get field ptrCtx");
//		}
//	}

	if (fidBuf == NULL) {
		fidBuf = env->GetFieldID(cls_data, "completionBuff","Ljava/nio/ByteBuffer;");
		if (fidBuf == NULL) {
			std::cout << "could not get field fidBuf" << std::endl;
		}
	}

	return JNI_VERSION_1_8;  //direct buffer requires java 1.4
}

extern "C" JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *jvm, void* reserved)
{
//	logs_from_xio_callback_unregister();
//	xio_shutdown();
//
//	// NOTE: We never reached this place
//	static bool alreadyCalled = false;
//	BULLSEYE_EXCLUDE_BLOCK_START
//	if (alreadyCalled) return;
//	alreadyCalled = true;
//
	JNIEnv *env;
	if (cached_jvm->GetEnv((void **)&env, JNI_VERSION_1_8)) {
		return;
	}

	if (jweakBridge != NULL) {
		env->DeleteWeakGlobalRef(jweakBridge);
		jweakBridge = NULL;
	}

	Java_org_ucx_jucx_Bridge_closeCtxNative(env, jclassBridge, cached_ctx);
	return;
}

JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_createCtxNative(JNIEnv *env,
		jclass cls, jlong feats, jlong fieldMask) {

	/* UCP temporary vars */
	ucp_params_t ucp_params;
	ucp_config_t *config;
	ucs_status_t status;

	/* UCP handler objects */
	ucp_context_h ucp_context;

	memset(&ucp_params, 0, sizeof(ucp_params));

	/* UCP initialization */
	status = ucp_config_read(NULL, NULL, &config);
	if (status != UCS_OK)
		std::cout << "Error: ucp_config_read()" << std::endl;

	ucp_params.features 	= (uint64_t) feats;
	ucp_params.field_mask 	= (uint64_t) fieldMask;
	ucp_params.request_size = sizeof(Request);
	ucp_params.request_init = UcpRequest::RequestHandler::requestInit;

	status = ucp_init(&ucp_params, config, &ucp_context);

	ucp_config_release(config);

	if (status != UCS_OK)
		return 0;

	cached_ctx = (native_ptr) ucp_context;

	return (native_ptr) ucp_context;

}

JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_closeCtxNative(JNIEnv *env, jclass cls, jlong ptrCtx)
{
	ucp_context_h ctx = (ucp_context_h) ptrCtx;
	if (ctx)
		ucp_cleanup(ctx);
	cached_ctx = 0;
}

JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_createWorkerNative(JNIEnv *env,
		jclass cls, jlong ptrCtx, jint maxComp, jobject compQueue) {
	ucp_context_h ucpContext = (ucp_context_h) ptrCtx;
	ucp_worker_params_t workerParams = { 0 };
	Worker* ucpWorker;
	ucs_status_t status;
	jobject jbyteBuff;
	uint32_t cap = (uint32_t) maxComp;

	workerParams.field_mask = UCP_WORKER_PARAM_FIELD_THREAD_MODE;
	workerParams.thread_mode = UCS_THREAD_MODE_SINGLE;

	ucpWorker = new Worker(ucpContext, workerParams, cap);

	jbyteBuff = env->NewDirectByteBuffer(ucpWorker->getBuffer(), cap);
	if (!jbyteBuff) {
		std::cout << "Error: Failed to allocate direct ByteBuffer" << std::endl;
		return -1;
	}

	env->SetObjectField(compQueue, fidBuf, jbyteBuff);

	return (native_ptr) ucpWorker;
}

JNIEXPORT jbyteArray JNICALL Java_org_ucx_jucx_Bridge_getWorkerAddressNative
		(JNIEnv *env, jclass cls, jlong workerID, jlongArray addrID) {
	ucs_status_t status;
	Worker* ucp_worker = (Worker*) workerID;
	ucp_address_t* local_addr;
	size_t local_addr_len;
	jbyteArray ret;
	jlong tmp;
	jlong* addr_ptr;
	jbyte* local_addr_wrap;

	local_addr = ucp_worker->initWorkerAddress(local_addr_len);

	local_addr_wrap = new jbyte[local_addr_len];
	memcpy(local_addr_wrap, local_addr, local_addr_len);

	ret = env->NewByteArray(local_addr_len);
	env->SetByteArrayRegion(ret, 0, local_addr_len, local_addr_wrap);

	tmp = (jlong) local_addr;
	addr_ptr = (jlong *) &tmp;
	env->SetLongArrayRegion(addrID, 0, 1, addr_ptr);

	return ret;
}

JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_releaseWorkerNative(JNIEnv *env, jclass cls,
		jlong workerID, jlong addrID) {
	Worker* ucp_worker = (Worker*) workerID;
	delete ucp_worker;
}

JNIEXPORT jint JNICALL Java_org_ucx_jucx_Bridge_probeAndProgressNative(
		JNIEnv *env, jclass cls, jlong workerID, jlong jtag, jlongArray ret) {
//	ucp_worker_h ucp_worker = (ucp_worker_h) workerID;
//	ucp_tag_recv_info_t info_tag;
//	ucp_tag_t tag = (ucp_tag_t) jtag;
//	ucp_tag_t mask = -1;
//	jlong tmp;
//	jlong* msg_tag_ptr;
//	ucp_tag_message_h msg_tag = NULL;
//
//	do {
//		ucp_worker_progress(ucp_worker);
//
//		msg_tag = ucp_tag_probe_nb(ucp_worker, tag, mask, 1, &info_tag);
//	} while (!msg_tag);
//
//	tmp = (jlong) msg_tag;
//	msg_tag_ptr = &tmp;
//	env->SetLongArrayRegion(ret, 0, 1, msg_tag_ptr);
//
//	return info_tag.length;
	return 0;
}

JNIEXPORT jint JNICALL
Java_org_ucx_jucx_Bridge_recvMsgAsyncNative__JJJLjava_nio_ByteBuffer_2IJ
		(JNIEnv *env, jclass cls, jlong jworker, jlong jtag, jlong jtagMask,
				jobject buffer, jint jmsgLen, jlong reqId) {
	int msgLen = int(jmsgLen);
	int ret;
	void* buff = env->GetDirectBufferAddress(buffer);
	if (!buff) {
		std::cout << "Error: msg is null" << std::endl;
		return -1;
	}

	Msg* msg = new Msg(buff, msgLen);

	ret = msg->recvMsgAsync(jworker, jtag, jtagMask, msgLen, reqId);

	if (ret)
		delete msg;

	return ret;
}

JNIEXPORT jint JNICALL Java_org_ucx_jucx_Bridge_recvMsgAsyncNative__JJJ_3BIJ
		(JNIEnv *env, jclass cls, jlong jworker, jlong jtag, jlong jtagMask,
				jbyteArray buffer, jint jmsgLen, jlong reqId) {
	int msgLen = int(jmsgLen);
	int ret;
	void* buff = calloc(1, msgLen);
	if (!buff) {
		std::cout << "Error: allocation failure" << std::endl;
		return -1;
	}

	Msg* msg = new Msg(buff, msgLen, true, buffer);

	ret = msg->recvMsgAsync(jworker, jtag, jtagMask, msgLen, reqId);

	if (ret)
	{
		if (ret > 0)
			env->SetByteArrayRegion(buffer, 0, msgLen, (jbyte*)buff);
		delete msg;
	}

	return ret;
}

JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_createEpNative(JNIEnv *env,
		jclass cls, jlong localWorker, jbyteArray remoteAddr) {
	Worker* worker = (Worker*) localWorker;
	ucs_status_t status;
	ucp_worker_h ucp_worker = worker->getUcpWorker();

	jsize len = env->GetArrayLength(remoteAddr);
	ucp_address_t* remote_worker_addr = (ucp_address_t*) calloc(1, len);
	env->GetByteArrayRegion(remoteAddr, 0, len, (jbyte*) remote_worker_addr);

	ucp_ep_params_t ep_params;

	ep_params.field_mask = UCP_EP_PARAM_FIELD_REMOTE_ADDRESS;
	ep_params.address = remote_worker_addr;

	ucp_ep_h ep;
	status = ucp_ep_create(ucp_worker, &ep_params, &ep);
	if (status != UCS_OK)
		std::cout << "Error: ucp_ep_create()" << std::endl;

	return (native_ptr) ep;
}

JNIEXPORT jint JNICALL
Java_org_ucx_jucx_Bridge_sendMsgAsyncNative__JJJLjava_nio_ByteBuffer_2IJ
		(JNIEnv *env, jclass cls, jlong jep, jlong jworker, jlong jtag,
				jobject jmsg, jint len, jlong reqId) {
	void* buff;
	int ret;
	int msgLen = int(len);

	buff = env->GetDirectBufferAddress(jmsg);
	if (!buff) {
		std::cout << "Error: msg is null" << std::endl;
		return -1;
	}

	Msg* msg = new Msg(buff, msgLen);

	ret = msg->sendMsgAsync(jep, jworker, jtag, msgLen, reqId);

	if (ret)
		delete msg;

	return ret;
}

JNIEXPORT jint JNICALL Java_org_ucx_jucx_Bridge_sendMsgAsyncNative__JJJ_3BIJ
		(JNIEnv *env, jclass cls, jlong jep, jlong jworker, jlong jtag,
				jbyteArray jmsg, jint len, jlong reqId) {
	void* buff;
	int ret;
	int msgLen = int(len);

	buff = calloc(1, msgLen);
	if (!buff) {
		std::cout << "Error: allocation failure" << std::endl;
		return -1;
	}
	env->GetByteArrayRegion((jbyteArray) jmsg, 0, msgLen, (jbyte*) buff);

	Msg* msg = new Msg(buff, msgLen, true);

	ret = msg->sendMsgAsync(jep, jworker, jtag, msgLen, reqId);

	if (ret)
		delete msg;

	return ret;
}

JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_releaseEndPointNative
		(JNIEnv *env, jclass cls, jlong jep) {
	ucp_ep_h ep = (ucp_ep_h) jep;
	ucp_ep_destroy(ep);
}

JNIEXPORT jint JNICALL Java_org_ucx_jucx_Bridge_progressWorkerNative
		(JNIEnv *env, jclass cls, jlong jworker) {

	Worker* worker = (Worker*) jworker;

	return worker->progress();
}

JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_getTimeNative(JNIEnv *env, jclass cls) {
	ucs_time_t time = ucs_get_time();
	return ucs_time_to_nsec(time);
}

JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_getCycleNative(JNIEnv *env, jclass cls) {
	return ucs_get_time();
}


