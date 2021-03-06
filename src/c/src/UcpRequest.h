/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
#ifndef UCPREQUEST_H_
#define UCPREQUEST_H_

#include "Msg.h"
#include <cstdint>

extern "C" {
#include <ucs/type/status.h>
#include <ucp/api/ucp_def.h>
}

class Worker;



/**
 * class to hold the request struct and request handlers
 */
class UcpRequest {
public:

	/**
	 * request struct
	 *
	 * Members:
	 * 	requestID 	- the requestID set by the user
	 * 	worker		- wrapper pointer to ucp_worker that carries out the request
	 * 	msg			- wrapper pointer to the send/receive buffer
	 */
	struct request_t {
		uint64_t 	requestID;
		Worker* 	worker;
//		Msg* 		msg;
		int			done;
		request_t*	next;
		int 		size;
	};

	/**
	 * request handlers (callbacks) class
	 */
	class RequestHandler {
	public:
		static void requestInit(void *request);
		static void recvRequestHandler(void *request, ucs_status_t status,
				ucp_tag_recv_info_t *info);

		static void sendRequestHandler(void *request, ucs_status_t status) {
			commonHandler(request);
		}

	private:
		RequestHandler() {}

		static void commonHandler(void *request);
	};

	static int requestErrorCheck(request_t* request, Worker* worker,
				Msg* msg, jlong reqId);

	static void freeRequest(request_t* request);

private:
	static void handleCompletion(request_t* request);
	UcpRequest() {}
};

using Request = UcpRequest::request_t; // TODO: c++11


#endif /* UCPREQUEST_H_ */
