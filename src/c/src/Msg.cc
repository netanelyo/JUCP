/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
#include "Msg.h"
#include "Worker.h"


int Msg::recvMsgAsync(jlong jworker, jlong jtag, jlong jtagMask, int msgLen,
		jlong reqId) {
	ucp_tag_t tag = (ucp_tag_t) jtag;
	ucp_tag_t mask = (ucp_tag_t) jtagMask;
	Request* request = 0;
	Worker* worker = (Worker*) jworker;
	ucp_worker_h ucp_worker = worker->getUcpWorker();

	request = (Request*) ucp_tag_recv_nb(ucp_worker, this->buffer, msgLen,
			ucp_dt_make_contig(1), tag, mask, UcpRequest::RequestHandler::recvRequestHandler);

	return 0;
//	return UcpRequest::requestErrorCheck(request, worker, this, reqId);
}

int Msg::sendMsgAsync(jlong jep, jlong jworker, jlong jtag, int msgLen,
		jlong reqId) {
	ucp_ep_h ep = (ucp_ep_h) jep;
	Request* request = 0;
	ucp_tag_t tag = (ucp_tag_t) jtag;
	Worker* worker = (Worker*) jworker;

	request = (Request*) ucp_tag_send_nb(ep, this->buffer, msgLen,
			ucp_dt_make_contig(1), tag, UcpRequest::RequestHandler::sendRequestHandler);

	return 0;
//	return UcpRequest::requestErrorCheck(request, worker, this, reqId);
}

void Msg::set(void* buff, int size, jbyteArray jbuff) {
	 buffer = buff;
	 buffSize = size;
	 jbuffer = jbuff;
	 if (jbuffer) allocated = true;
}
