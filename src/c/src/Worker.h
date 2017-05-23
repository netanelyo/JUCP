#ifndef SRC_WORKER_H_
#define SRC_WORKER_H_

extern "C" {
#include <ucp/api/ucp.h>
}

#include <cstddef>

class Worker {
public:
	Worker(ucp_context_h ctx, ucp_worker_params_t params, void* buff, uint64_t cap);

	~Worker() { deleteWorker(); }

	ucp_address_t* initWorkerAddress(size_t& addr_len);

	int getEventCnt() const {
		return event_cnt;
	}

	void setEventCnt(int eventCnt) {
		event_cnt = eventCnt;
	}

	uint64_t *getEventQueue() const {
		return event_queue;
	}

	void putInEventQueue(uint64_t item) {
		if (event_cnt < queue_size) event_queue[event_cnt++] = item;
	}

	ucp_worker_h getUcpWorker() const {
		return ucp_worker;
	}

	void workerWait() {
		while (event_cnt == 0)
			ucp_worker_progress(ucp_worker);
	}

private:
	ucp_worker_h 	ucp_worker;
	ucp_address_t*	worker_address;
	size_t			address_length;
	int				event_cnt;
	uint64_t		queue_size;
	uint64_t*		event_queue;

	void deleteWorker();
};


#endif /* SRC_WORKER_H_ */
