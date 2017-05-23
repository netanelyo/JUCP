#ifndef REQUESTHANDLER_H_
#define REQUESTHANDLER_H_

#include "Worker.h"

typedef struct ucp_request {
	uint64_t requestID;
	Worker* worker;
} Request;


class RequestHandler {
public:
	static void requestInit(void *request);
	static void commonHandler(void *request);
	static void recvRequestHandler(void *request, ucs_status_t status,
									ucp_tag_recv_info_t *info)
	{
		commonHandler(request);
	}

	static void sendRequestHandler(void *request, ucs_status_t status)
	{
		commonHandler(request);
	}

private:
	RequestHandler() {}

};



#endif /* REQUESTHANDLER_H_ */
