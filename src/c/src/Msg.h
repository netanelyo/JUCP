/*
 * Msg.h
 *
 *  Created on: Jun 4, 2017
 *      Author: netanelyo
 */

#ifndef MSG_H_
#define MSG_H_

#include <jni.h>
#include <jni_md.h>
#include <cstdlib>

class Msg {
public:
	Msg(void* buff, int size, bool alloc = false, jbyteArray jbuff = nullptr) :
		buffer(buff), jbuffer(jbuff), buffSize(size), allocated(alloc) {}

	~Msg() { if (allocated) free(buffer); }

	void* getBuffer() { return buffer; }

	jbyteArray getJavaBuffer() { return jbuffer; }

	int size() { return buffSize; }

	int recvMsgAsync(jlong jworker, jlong jtag, jlong jtagMask, int msgLen,
			jlong reqId);

	int sendMsgAsync(jlong jep, jlong jworker, jlong jtag, int msgLen, jlong reqId);

	bool isAllocated() { return allocated; }


private:
	void* 		buffer;
	jbyteArray 	jbuffer;
	int			buffSize;
	bool 		allocated;
};



#endif /* MSG_H_ */
