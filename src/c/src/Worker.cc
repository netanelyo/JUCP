#include "Worker.h"
#include <iostream>

Worker::Worker(ucp_context_h ctx, ucp_worker_params_t params,
		void* buff, uint64_t cap) :
		workerAddress(nullptr), addressLength(0), eventCnt(0), queueSize(cap),
		eventQueue((uint64_t*) buff), pendingRequests() {

	ucs_status_t status;

	status = ucp_worker_create(ctx, &params, &ucpWorker);
	if (status != UCS_OK)
		std::cout << "Error: ucp_worker_create()" << std::endl;
}

void Worker::deleteWorker() {
	ucp_worker_release_address(ucpWorker, workerAddress);
	ucp_worker_destroy(ucpWorker);
}

ucp_address_t* Worker::initWorkerAddress(size_t& addr_len) {
	ucs_status_t status;

	status = ucp_worker_get_address(ucpWorker, &workerAddress, &addressLength);
	if (status != UCS_OK)
		std::cout << "Error: ucp_worker_get_address()" << std::endl;

	addr_len = addressLength;

	return workerAddress;
}

void Worker::putInEventQueue(uint64_t item) {
	if (eventCnt < queueSize) eventQueue[eventCnt++] = item;
	else pendingRequests.push_back(item);
}

void Worker::moveRequestsToEventQueue() {
		while (eventCnt < queueSize && this->hasPendingRequests()) {
			eventQueue[eventCnt++] = pendingRequests.front();
			pendingRequests.pop_front();
		}
	}

