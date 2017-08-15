/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
#include "UcpRequest.h"
#include "Worker.h"
#include <iostream>

void UcpRequest::RequestHandler::requestInit(void *request) {
		Request* req = (Request*) request;
		req->requestID 	= -1;
		req->worker 	= nullptr;
		req->done		= -1;
		req->next		= nullptr;
		req->size 		= 0;
//		req->msg		= nullptr;
}

void UcpRequest::RequestHandler::commonHandler(void *request) {
	Request* req = (Request*) request;
	//assert(req); // TODO: compilation flag
	//assert(req->worker);
	//assert(req->requestID);
//	std::cout << "in handler; req = " << (void*)req << std::endl; //TODO
//	std::cout << "in handler; worker = " << (void*)req->worker << std::endl; //TODO
//	std::cout << "in handler; reqID = 0x" << std::hex << req->requestID << std::endl; //TODO
//	std::cout << "in handler; req->done = " << std::dec << req->done << std::endl; //TODO
	if (req->worker) {
		handleCompletion(req);
	}
	else
		req->done = 0;
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
													Msg* msg, jlong reqId)
{
	if (UCS_PTR_IS_ERR(request)) {
		std::cout << "***  error  ***" << std::endl;
		return -1; // An error occurred
	}
	else if (UCS_PTR_STATUS(request) != UCS_OK) {
//		std::cout << "in errcheck; req = " << (void*)request << std::endl; //TODO
//		std::cout << "in errcheck; worker = " << (void*)worker << std::endl; //TODO
//		std::cout << "in errcheck; reqID = 0x" << std::hex << request->requestID << std::endl; //TODO
//		std::cout << "in errcheck; req->done = " << std::dec << request->done << std::endl; //TODO

		// Async handling - request still in progress
		request->requestID 	= (uint64_t) reqId;
		request->worker 	= worker;
		if (request->done == 0)
			handleCompletion(request);

		worker->addToList(request);
		return 0;
	}
	else {
//		std::cout << "***  success  ***" << std::endl;
		// First, check if there are any events to be processed in the list
		worker->moveRequestsToEventQueue();

		// Sent successfully
		worker->putInEventQueue((uint64_t) reqId);

		int cnt = worker->getEventCnt();
		return cnt;
	}
}

void UcpRequest::handleCompletion(request_t* request) {
//	std::cout << "handler comp: " << request->done << std::endl;
//	std::cout << "is worker null? " << std::boolalpha << (request->worker?false:true) << std::endl;
	request->worker->putInEventQueue(request->requestID);
	request->done = 1;
}

void UcpRequest::freeRequest(request_t* request) {
	RequestHandler::requestInit(reinterpret_cast<void*>(request));
	ucp_request_free(request);
}
