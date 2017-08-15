/*
 * Copyright (C) Mellanox Technologies Ltd. 2001-2017.  ALL RIGHTS RESERVED.
 * See file LICENSE for terms.
 */
#ifndef SRC_WORKER_H_
#define SRC_WORKER_H_

extern "C" {
#include <ucp/api/ucp.h>
}

#include <list>
#include <unordered_set>
#include <cstddef>
#include <jni.h>

//TODO
#include <iostream>


#include "UcpRequest.h"

class Worker {
public:
	Worker(ucp_context_h ctx, ucp_worker_params_t params, uint32_t cap);

	~Worker() { deleteWorker(); }

	ucp_address_t* initWorkerAddress(size_t& addr_len);

	int getEventCnt() const {
		return eventCnt;
	}

	void setEventCnt(int eventCnt) {
		this->eventCnt = eventCnt;
	}

	uint64_t *getEventQueue() const {
		return eventQueue;
	}

	int progress();

	void putInEventQueue(uint64_t item);

	void moveRequestsToEventQueue();

	ucp_worker_h getUcpWorker() const {
		return ucpWorker;
	}

	uint64_t* getBuffer() const {
		return eventQueue;
	}

	void workerWait() { //TODO
		while (eventCnt == 0)
			ucp_worker_progress(ucpWorker);
	}

//	bool inUse(jlong addr) { return buffersInUse.find(addr) != buffersInUse.end(); }
//
//	void addBuffer(jlong addr) { buffersInUse.insert(addr); }

	void addToList(Request* req);

	int numOfRequests() const;

	int wait(int events);

	void flush(int events);

private:
	ucp_worker_h 		ucpWorker;
	ucp_address_t*		workerAddress;
	size_t				addressLength;
	int					eventCnt;
	uint32_t			queueSize;
	uint64_t*			eventQueue;
	std::list<uint64_t> pendingRequests;
	Request*			head;
//	std::unordered_set<jlong>	buffersInUse;

	void freeRequests();

	void deleteWorker();

	int freeAndSetCnt();

	bool hasPendingRequests() {
		return !(pendingRequests.empty());
	}
};


#endif /* SRC_WORKER_H_ */
