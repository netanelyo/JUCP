#include "RequestHandler.h"

void RequestHandler::requestInit(void *request) {
		struct ucp_request *req = (struct ucp_request *) request;
		req->requestID = 0;
		req->worker = nullptr;
}

void RequestHandler::commonHandler(void *request) {
	Request *req = (Request *) request;
	req->worker->putInEventQueue(req->requestID);
	ucp_request_release(request);
}
