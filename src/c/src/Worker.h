#ifndef SRC_WORKER_H_
#define SRC_WORKER_H_

extern "C" {
#include <ucp/api/ucp.h>
}

#include <list>
#include <cstddef>

class Worker {
public:
	Worker(ucp_context_h ctx, ucp_worker_params_t params, void* buff, uint64_t cap);

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

	void putInEventQueue(uint64_t item);

	void moveRequestsToEventQueue();

	ucp_worker_h getUcpWorker() const {
		return ucpWorker;
	}

	void workerWait() { //TODO
		while (eventCnt == 0)
			ucp_worker_progress(ucpWorker);
	}

private:
	ucp_worker_h 		ucpWorker;
	ucp_address_t*		workerAddress;
	size_t				addressLength;
	int					eventCnt;
	uint64_t			queueSize;
	uint64_t*			eventQueue;
	std::list<uint64_t> pendingRequests;

	void deleteWorker();

	bool hasPendingRequests() {
		return !(pendingRequests.empty());
	}
};


#endif /* SRC_WORKER_H_ */
