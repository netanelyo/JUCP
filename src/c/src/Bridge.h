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
#ifndef Bridge__H___
#define Bridge__H___

#include <cerrno>
#include <cstdlib>
#include <cstring>
#include <map>
#include <iostream>
#include <jni.h>

typedef unsigned long long native_ptr;

extern "C" {
#include <ucp/api/ucp.h>

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    createCtxNative
 * Signature: (Lorg/ucx/jucx/UCPParams;)J
 */
JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_createCtxNative
  (JNIEnv *, jclass, jobject);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    closeCtxNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_closeCtxNative
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    createWorkerNative
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_createWorkerNative
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    getWorkerAddressNative
 * Signature: (J[J)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_ucx_jucx_Bridge_getWorkerAddressNative
  (JNIEnv *, jclass, jlong, jlongArray);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    releaseWorkerNative
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_releaseWorkerNative
  (JNIEnv *, jclass, jlong, jlong);


//TODO
JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_testerNative
  (JNIEnv *, jclass, jbyteArray, jlong);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    probeAndProgressNative
 * Signature: (JJ[J)I
 */
JNIEXPORT jint JNICALL Java_org_ucx_jucx_Bridge_probeAndProgressNative
  (JNIEnv *, jclass, jlong, jlong, jlongArray);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    recvMsgNbNative
 * Signature: (JJ)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_org_ucx_jucx_Bridge_recvMsgNbNative
  (JNIEnv *, jclass, jlong, jlong);

}

//class Context;
//
//void Bridge_invoke_logToJava_callback(const int severity, const char* log_message);
//void Bridge_invoke_requestForBoundMsgPool_callback(Context* ctx, int inSize, int outSize);

#endif
