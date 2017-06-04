/*
 * UcpUtils.h
 *
 *  Created on: Jun 4, 2017
 *      Author: netanelyo
 */

#ifndef UCPUTILS_H_
#define UCPUTILS_H_

class UcpUtils {
public:
	static int requestErrorCheck(Request* request, Worker* worker, Msg* msg,
			jlong reqId);

	static int recvMsgAsync(jlong jworker, jlong jtag, jlong jtagMask,
			Msg* buff, int msgLen, jlong reqId);

	static int sendMsgAsync(jlong jep, jlong jworker, jlong jtag, Msg* msg,
			int msgLen, jlong reqId);
private:
	UcpUtils() {}
};



#endif /* UCPUTILS_H_ */
