#include "Worker.h"
#include <iostream>

Worker::Worker(ucp_context_h ctx, ucp_worker_params_t params, void* buff, uint64_t cap)
		: worker_address(nullptr), address_length(0),
		  event_cnt(0), queue_size(cap), event_queue((uint64_t*) buff) {

	ucs_status_t status;

	status = ucp_worker_create(ctx, &params, &ucp_worker);
	if (status != UCS_OK)
		std::cout << "Error: ucp_worker_create()" << std::endl;
}

void Worker::deleteWorker() {
	ucp_worker_release_address(ucp_worker, worker_address);
	ucp_worker_destroy(ucp_worker);
}

ucp_address_t* Worker::initWorkerAddress(size_t& addr_len) {
	ucs_status_t status;

	status = ucp_worker_get_address(ucp_worker, &worker_address, &address_length);
	if (status != UCS_OK)
		std::cout << "Error: ucp_worker_get_address()" << std::endl;

	addr_len = address_length;

	return worker_address;
}
