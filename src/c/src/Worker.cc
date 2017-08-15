/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
#include "Worker.h"
#include "UcpRequest.h"
#include <byteswap.h>
#include <iostream>

#define MAX_REQUESTS 8

Worker::Worker(ucp_context_h ctx, ucp_worker_params_t params, uint32_t cap) :
		workerAddress(nullptr), addressLength(0), eventCnt(0), queueSize(cap),
		eventQueue(new uint64_t[cap]), head(nullptr) {

	ucs_status_t status;

	status = ucp_worker_create(ctx, &params, &ucpWorker);
	if (status != UCS_OK)
		std::cout << "Error: ucp_worker_create()" << std::endl;
}

void Worker::deleteWorker() {
	delete[] eventQueue;
	freeRequests();
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
	while (eventCnt < queueSize && hasPendingRequests()) {
		eventQueue[eventCnt++] = pendingRequests.front();
		pendingRequests.pop_front();
	}
}

int Worker::progress() {
	moveRequestsToEventQueue();

	ucp_worker_progress(ucpWorker);

	return freeAndSetCnt();
}

void Worker::addToList(Request* req) {
	if (head) {
		req->next = head;
		req->size = head->size + 1;
	}
	else
		req->size = 1;
	head = req;
}

void Worker::freeRequests() {
	while (head && head->done == 1) {
		Request* req = head;
		head = head->next;
		UcpRequest::freeRequest(req);
	}
}

int Worker::numOfRequests() const {
	if (head)
		return head->size;
	return 0;
}

int Worker::wait(int events) {
	moveRequestsToEventQueue();

	while (eventCnt < events)
		ucp_worker_progress(ucpWorker);

	return freeAndSetCnt();
}

int Worker::freeAndSetCnt() {
	if (numOfRequests() >= MAX_REQUESTS)
		freeRequests();

	int cnt = eventCnt;
	setEventCnt(0);

	return cnt;
}

void Worker::flush(int events) {
	wait(events);
	freeRequests();
}






