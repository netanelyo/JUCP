/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
#include "Worker.h"
#include <byteswap.h>
#include <iostream>

Worker::Worker(ucp_context_h ctx, ucp_worker_params_t params, uint64_t cap) :
		workerAddress(nullptr), addressLength(0), eventCnt(0), queueSize(cap),
		eventQueue(new uint64_t[cap]) {

	ucs_status_t status;

	status = ucp_worker_create(ctx, &params, &ucpWorker);
	if (status != UCS_OK)
		std::cout << "Error: ucp_worker_create()" << std::endl;
}

void Worker::deleteWorker() {
	delete[] eventQueue;
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
	uint64_t swapped = __bswap_64(item);
	if (eventCnt < queueSize) eventQueue[eventCnt++] = swapped;
	else pendingRequests.push_back(swapped);
}

void Worker::moveRequestsToEventQueue() {
	while (eventCnt < queueSize && this->hasPendingRequests()) {
		eventQueue[eventCnt++] = pendingRequests.front();
		pendingRequests.pop_front();
	}
}

int Worker::progress() {
	moveRequestsToEventQueue();
	ucp_worker_progress(ucpWorker);

	int cnt = this->eventCnt;
	setEventCnt(0);

	return cnt;
}

