/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
#ifndef Bridge__H___
#define Bridge__H___

#include <cerrno>
#include <cstdlib>
#include <cstring>
#include <cstddef>
#include <iostream>
#include <jni.h>

typedef uintptr_t native_ptr;

extern "C" {

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    createCtxNative
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_createCtxNative
  (JNIEnv *, jclass, jlong, jlong);


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
 * Signature: (JILorg/ucx/jucx/Worker/CompletionQueue;)J
 */
JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_createWorkerNative
  (JNIEnv *, jclass, jlong, jint, jobject);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    testerNative
 * Signature: ([BJ)V
 */
JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_testerNative
  (JNIEnv *, jclass, jbyteArray, jlong);

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

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    recvMsgAsyncNative
 * Signature: (JJJLjava/nio/ByteBuffer;IJ)I
 */
JNIEXPORT jint JNICALL Java_org_ucx_jucx_Bridge_recvMsgAsyncNative__JJJLjava_nio_ByteBuffer_2IJ
  (JNIEnv *, jclass, jlong, jlong, jlong, jobject, jint, jlong);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    recvMsgAsyncNative
 * Signature: (JJJ[BIJ)I
 */
JNIEXPORT jint JNICALL Java_org_ucx_jucx_Bridge_recvMsgAsyncNative__JJJ_3BIJ
  (JNIEnv *, jclass, jlong, jlong, jlong, jbyteArray, jint, jlong);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    createEpNative
 * Signature: (J[B)J
 */
JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_createEpNative
  (JNIEnv *, jclass, jlong, jbyteArray);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    sendMsgAsyncNative
 * Signature: (JJJLjava/nio/ByteBuffer;IJ)I
 */
JNIEXPORT jint JNICALL Java_org_ucx_jucx_Bridge_sendMsgAsyncNative__JJJLjava_nio_ByteBuffer_2IJ
  (JNIEnv *, jclass, jlong, jlong, jlong, jobject, jint, jlong);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    sendMsgAsyncNative
 * Signature: (JJJ[BIJ)I
 */
JNIEXPORT jint JNICALL Java_org_ucx_jucx_Bridge_sendMsgAsyncNative__JJJ_3BIJ
  (JNIEnv *, jclass, jlong, jlong, jlong, jbyteArray, jint, jlong);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    releaseEndPointNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_ucx_jucx_Bridge_releaseEndPointNative
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    progressWorkerNative
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_ucx_jucx_Bridge_progressWorkerNative
  (JNIEnv *, jclass, jlong);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    getTimeNative
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_getTimeNative
  (JNIEnv *, jclass);

/*
 * Class:     org_ucx_jucx_Bridge
 * Method:    getCycleNative
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_ucx_jucx_Bridge_getCycleNative
  (JNIEnv *, jclass);


}

//class Context;
//
//void Bridge_invoke_logToJava_callback(const int severity, const char* log_message);
//void Bridge_invoke_requestForBoundMsgPool_callback(Context* ctx, int inSize, int outSize);

#endif
