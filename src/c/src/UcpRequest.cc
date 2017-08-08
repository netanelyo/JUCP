/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
#include "UcpRequest.h"
#include <iostream>

void UcpRequest::RequestHandler::requestInit(void *request) {
		Request* req = (Request*) request;
		req->requestID 	= 0;
		req->worker 	= nullptr;
		req->msg		= nullptr;
}

void UcpRequest::RequestHandler::commonHandler(void *request) {
	Request* req = (Request*) request;
	//assert(req); // TODO: compilation flag
	//assert(req->worker);
	//assert(req->requestID);
	req->worker->putInEventQueue(req->requestID);
	//TODO: remove
	req->worker = nullptr;
//	delete req->msg;
//	req->msg = nullptr;
	ucp_request_free(request);

}

void UcpRequest::RequestHandler::recvRequestHandler(void *request, ucs_status_t status,
		ucp_tag_recv_info_t *info) {
	commonHandler(request);



//	Request* req = (Request*) request;
//	Msg* msg = req->msg;
//	if (msg->isAllocated())
//		env->SetByteArrayRegion(msg->getJavaBuffer(), 0, msg->size(), (jbyte*)msg->getBuffer());
}

int UcpRequest::requestErrorCheck(request_t* request, Worker* worker,
								ucp_tag_recv_info* info, Msg* msg, jlong reqId)
{
	if (ucp_request_test(request, info) == UCS_INPROGRESS) {
		// Async handling - request still in progress
		request->requestID 	= (uint64_t) reqId;
		request->worker 	= worker;
		request->msg 		= msg;
		return 0;
	}
	else if (ucp_request_test(request, info) == UCS_OK) {
		// First, check if there are any events to be processed in the list
		worker->moveRequestsToEventQueue();

		// Sent successfully
		worker->putInEventQueue((uint64_t) reqId);

		return worker->getEventCnt();
	}
	else {
		return -1; // An error occurred
	}
}
