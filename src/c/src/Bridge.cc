/*
 ** Copyright (C) 2013 Mellanox Technologies
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at:
 **
 ** http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 ** either express or implied. See the License for the specific language
 ** governing permissions and  limitations under the License.
 **
 */

#include "Bridge.h"

#include "UcpRequest.h"
#include "Worker.h"

extern "C" {
	#include <ucp/api/ucp.h>
}

//static jclass cls;
//static JavaVM *cached_jvm;
//
//static jclass cls_data;
//static jweak jweakBridge; // use weak global ref for allowing GC to unload & re-load the class and handles
//static jclass jclassBridge; // just casted ref to above jweakBridge. Hence, has same life time
//static jfieldID fidPtr;
//static jfieldID fidBuf;
//static jfieldID fidError;
//static jmethodID jmethodID_logToJava; // handle to java cb method
//static jmethodID jmethodID_requestForBoundMsgPool;


JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_createCtxNative(JNIEnv *env,
		jclass cls, jobject params) {

	/* JNI temporary vars */
	jfieldID fid;
	jclass params_native;

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

	params_native = env->GetObjectClass(params);

	fid = env->GetFieldID(params_native, "features", "J");
	ucp_params.features = env->GetLongField(params, fid);

	fid = env->GetFieldID(params_native, "fieldMask", "J");
	ucp_params.field_mask = env->GetLongField(params, fid);

	ucp_params.request_size = sizeof(Request);
	ucp_params.request_init = UcpRequest::RequestHandler::requestInit;

	status = ucp_init(&ucp_params, config, &ucp_context);

	ucp_config_release(config);

	return (native_ptr) ucp_context;

}

JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_closeCtxNative(JNIEnv *env, jclass cls, jlong ptrCtx)
{
	ucp_cleanup((ucp_context_h) ptrCtx);
}

JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_createWorkerNative(JNIEnv *env,
		jclass cls, jlong ptrCtx, jobject compBuff) {
	ucp_context_h ucp_context = (ucp_context_h) ptrCtx;
	ucp_worker_params_t worker_params = { 0 };
	Worker* ucp_worker;
	ucs_status_t status;
	void* tmp;
	uint64_t cap;

	worker_params.field_mask = UCP_WORKER_PARAM_FIELD_THREAD_MODE;
	worker_params.thread_mode = UCS_THREAD_MODE_SINGLE;

	tmp = env->GetDirectBufferAddress(compBuff);
	if (!tmp) {
		std::cout << "Error: Failed to extract direct ByteBuffer address" << std::endl;
		return -1;
	}

	cap = (uint64_t) env->GetDirectBufferCapacity(compBuff);

	ucp_worker = new Worker(ucp_context, worker_params, tmp, cap);

	return (native_ptr) ucp_worker;
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

//JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_testerNative
//(JNIEnv *env, jclass cls, jbyteArray jArr, jlong nativeID) {
//	std::cout << "Compare = " << memcmp(jArr, (ucp_address_t*)nativeID, env->GetArrayLength(jArr)) << std::endl;
//}

JNIEXPORT jint JNICALL Java_org_ucx_jucx_Bridge_probeAndProgressNative(
		JNIEnv *env, jclass cls, jlong workerID, jlong jtag, jlongArray ret) {
	ucp_worker_h ucp_worker = (ucp_worker_h) workerID;
	ucp_tag_recv_info_t info_tag;
	ucp_tag_t tag = (ucp_tag_t) jtag;
	ucp_tag_t mask = -1;
	jlong tmp;
	jlong* msg_tag_ptr;
	ucp_tag_message_h msg_tag = NULL;

	do {
		ucp_worker_progress(ucp_worker);

		msg_tag = ucp_tag_probe_nb(ucp_worker, tag, mask, 1, &info_tag);
	} while (!msg_tag);

	tmp = (jlong) msg_tag;
	msg_tag_ptr = &tmp;
	env->SetLongArrayRegion(ret, 0, 1, msg_tag_ptr);

	return info_tag.length;
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

	Msg* msg = new Msg(msg, msgLen);

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
		jclass cls, jlong local_worker, jbyteArray remote_addr) {
	ucp_worker_h ucp_worker = (ucp_worker_h) local_worker;
	ucs_status_t status;

	jsize len = env->GetArrayLength(remote_addr);
	ucp_address_t* remote_worker_addr = (ucp_address_t*) calloc(1, len);
	env->GetByteArrayRegion(remote_addr, 0, len, (jbyte*) remote_worker_addr);

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

	worker->moveRequestsToEventQueue();
	ucp_worker_progress(worker->getUcpWorker());

	int cnt = worker->getEventCnt();
	worker->setEventCnt(0);

	return cnt;
}

