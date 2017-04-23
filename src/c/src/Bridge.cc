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

//#include "bullseye.h"
//#include "Client.h"
//#include "ServerPortal.h"
//#include "Context.h"
//#include "Utils.h"
//#include "MsgPool.h"
#include "Bridge.h"

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


/****************************************
 * 		Taken from ucp_hello_world.c	*
 ****************************************/
struct ucx_context {
	int completed;
};

static void request_init(void *request)
{
    struct ucx_context *ctx = (struct ucx_context *) request;
    ctx->completed = 0;
}

static void recv_handle(void *request, ucs_status_t status,
                        ucp_tag_recv_info_t *info)
{
    struct ucx_context *context = (struct ucx_context *) request;

    context->completed = 1;
}
/****************************************
 ****************************************
 ****************************************/


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
	//memset(&worker_params, 0, sizeof(worker_params));

	/* UCP initialization */
	status = ucp_config_read(NULL, NULL, &config);

	params_native = env->GetObjectClass(params);

	fid = env->GetFieldID(params_native, "features", "J");
	ucp_params.features = env->GetLongField(params, fid);
//	std::cout << "Features = " << ucp_params.features << std::endl;

	fid = env->GetFieldID(params_native, "fieldMask", "J");
	ucp_params.field_mask = env->GetLongField(params, fid);
//	std::cout << "Mask = " << ucp_params.field_mask << std::endl;

    ucp_params.request_size    = sizeof(struct ucx_context);
    ucp_params.request_init    = request_init;

	status = ucp_init(&ucp_params, config, &ucp_context);

//	ucp_config_print(config, stdout, NULL, UCS_CONFIG_PRINT_CONFIG);

	ucp_config_release(config);

//	printf("c ptr = %llu\n", (unsigned long long) ucp_context);

	return (unsigned long long) ucp_context;

}

JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_closeCtxNative(JNIEnv *env, jclass cls, jlong ptrCtx)
{
	ucp_cleanup((ucp_context_h) ptrCtx);
}

JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_createWorkerNative(JNIEnv *env, jclass cls,
																		jlong ptrCtx)
{
	ucp_context_h ucp_context = (ucp_context_h) ptrCtx;
	ucp_worker_params_t worker_params = { 0 };
	ucp_worker_h ucp_worker;
	ucs_status_t status;

    worker_params.field_mask  = UCP_WORKER_PARAM_FIELD_THREAD_MODE;
    worker_params.thread_mode = UCS_THREAD_MODE_SINGLE;

    status = ucp_worker_create(ucp_context, &worker_params, &ucp_worker);
    if (status != UCS_OK)
    	std::cout << "ucp_worker_create()" << std::endl;

    ucp_worker_print_info(ucp_worker, stdout);

    return (native_ptr)ucp_worker;
}

JNIEXPORT jbyteArray JNICALL Java_org_ucx_jucx_Bridge_getWorkerAddressNative(JNIEnv *env,
																jclass cls, jlong workerID, jlongArray addrID)
{
	ucs_status_t status;
	ucp_worker_h ucp_worker = (ucp_worker_h)workerID;
	ucp_address_t* local_addr;
	size_t local_addr_len;
	jbyteArray ret;
	jlong tmp;
	jlong* addr_ptr;
	jbyte* local_addr_wrap;

    status = ucp_worker_get_address(ucp_worker, &local_addr, &local_addr_len);
    if (status != UCS_OK)
        	std::cout << "ucp_worker_get_address()" << std::endl;

    local_addr_wrap = new jbyte[local_addr_len];
    memcpy(local_addr_wrap, local_addr, local_addr_len);

    ret = env->NewByteArray(local_addr_len);
    env->SetByteArrayRegion(ret, 0, local_addr_len, local_addr_wrap);

    std::cout << "C address length = "
    		<< local_addr_len << std::endl;

    tmp = (jlong)local_addr;
    addr_ptr = (jlong *) &tmp;
    env->SetLongArrayRegion(addrID, 0, 1, addr_ptr);


//    char* h = new char[local_addr_len + 1];
//    memcpy(h, local_addr, local_addr_len);
//    h[local_addr_len] = '\0';
//    std::cout << "C address = " << h << std::endl;

//    std::cout << "C worker = " << (native_ptr)ucp_worker << std::endl;
//    std::cout << "C addr_len = " << local_addr_len << std::endl;

    return ret;
}

JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_releaseWorkerNative(JNIEnv *env, jclass cls,
																	jlong workerID, jlong addrID) {
	ucp_worker_h ucp_worker = (ucp_worker_h)workerID;
	ucp_address_t* worker_addr = (ucp_address_t*)addrID;

	ucp_worker_print_info(ucp_worker, stdout);

    ucp_worker_release_address(ucp_worker, worker_addr);

    ucp_worker_destroy(ucp_worker);
}

JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_testerNative
  (JNIEnv *env, jclass cls, jbyteArray jArr, jlong nativeID) {
	std::cout << "Compare = " << memcmp(jArr, (ucp_address_t*)nativeID, env->GetArrayLength(jArr)) << std::endl;
}

JNIEXPORT jint JNICALL Java_org_ucx_jucx_Bridge_probeAndProgressNative
  (JNIEnv *env, jclass cls, jlong workerID, jlong jtag, jlongArray ret)
{
	ucp_worker_h ucp_worker = (ucp_worker_h) workerID;
	ucp_tag_recv_info_t info_tag;
	ucp_tag_message_h msg_tag;
	ucp_tag_t tag = (ucp_tag_t) jtag;
	jlong tmp;
	jlong* msg_tag_ptr;

	do
	{
		ucp_worker_progress(ucp_worker);

		msg_tag = ucp_tag_probe_nb(ucp_worker, tag, -1, 1, &info_tag);
	} while (msg_tag == NULL);

	tmp = (jlong)msg_tag;
	msg_tag_ptr = &tmp;
	env->SetLongArrayRegion(ret, 0, 1, msg_tag_ptr);

	return info_tag.length;
}

JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_recvMsgNbNative
  (JNIEnv *env, jclass cls, jlong workerID, jlong jtagMsg, jobject buff, jint buffSize)
{
	char* msg = (char*)(env->GetDirectBufferAddress(buff));
	struct ucx_context* request = NULL;
	ucp_worker_h ucp_worker = (ucp_worker_h) workerID;
	ucp_tag_message_h msg_tag = (ucp_tag_message_h) jtagMsg;

	request = (struct ucx_context*)ucp_tag_msg_recv_nb(ucp_worker, msg, (size_t)(int)buffSize,
	                                  ucp_dt_make_contig(1), msg_tag, recv_handle);
}













