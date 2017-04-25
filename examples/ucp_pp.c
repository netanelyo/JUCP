#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <string.h>
#include <inttypes.h>
#include <unistd.h>
#include <sys/epoll.h>
#include <sys/time.h>

#include <ucp/api/ucp.h>

#define GOTO_ERR_HANDLER(_cond, _func, _label) 		\
	do 	{ if (_cond)  	{ 							\
		fprintf(stderr, "Error: %s\n", _func); 		\
		goto _label; 	} 							\
		} while (0)

struct ucx_context {
    int completed;
};

struct pp_msg { //TODO size?
	uint64_t 	 cnt;
};

/* Will indicate the mode of operation */
enum ucp_test_mode_t {
    TEST_MODE_PROBE,
    TEST_MODE_WAIT,
    TEST_MODE_EVENTFD
} ucp_test_mode = TEST_MODE_PROBE;

/* To be used in several functions */
static ucp_address_t	*local_addr;
static ucp_address_t	*peer_addr;
static int				 sockfd 		= -1;
static const ucp_tag_t	 tag			= 0xa2b9c5d9e2fu;
static const ucp_tag_t	 tag_mask		= -1;
static size_t		 	 pp_msg_size	= 16;
static uint32_t			 iters			= 1000;
static int 				 num_of_threads = 1;
static size_t 			 local_addr_len;
static size_t 			 peer_addr_len = 0;


/*
 * General callback
 */
void cb_func(void* req, ucs_status_t stat, ucp_tag_recv_info_t *info) {
    struct ucx_context *ctx = (struct ucx_context *) req;
    ++ctx->completed;
    //TODO what about status? info?
}

/*
 * Send callback function
 */
void send_cb(void *req, ucs_status_t stat) {
	cb_func(req, stat, NULL);
}

/*
 * Receive callback function
 */
void recv_cb(void* req, ucs_status_t stat, ucp_tag_recv_info_t *info) {
    cb_func(req, stat, info);
}

/*
 * Progress until completion
 */
static void wait_for_completion(ucp_worker_h ucp_worker, struct ucx_context *context, int first) {
	while (context->completed == first)
		ucp_worker_progress(ucp_worker);
}

/*
 * Initializing method to be passed to ucp_init
 */
static void request_init(void *request) {
    struct ucx_context *ctx = (struct ucx_context *) request;
    ctx->completed = 0;
}

static void client_connect(char* server_name, int server_port) {
	int 				 rc			= 0;
	struct sockaddr_in 	 sockaddr 	= { 0 };
	struct hostent		*host		= NULL;

	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0)
	{
		perror("Client - socket()");
		goto err;
	}

	host = gethostbyname(server_name);
	GOTO_ERR_HANDLER(host == NULL || host->h_addr_list == NULL,
			"Client - couldn't find a host", err_socket);

	memcpy(&sockaddr.sin_addr, *(host->h_addr_list), host->h_length);
	sockaddr.sin_family 	 = host->h_addrtype;
	sockaddr.sin_port 		 = htons((uint16_t)server_port);

	rc = connect(sockfd, (struct sockaddr*)&sockaddr, sizeof(sockaddr));
	if (rc)
	{
		perror("Client - connect()");
		goto err_socket;
	}

	return;

err_socket:
	close(sockfd);
err:
	sockfd = -1;
}

static ucs_status_t client_get_peer_addr() {
	size_t 		size = 0;
	int			rc	 = 0;
	char tmp_buff[8] = { 0 };
	int readB = 0, toRead = 4;
	void* addr_buff;

	size = sizeof(peer_addr_len);
	while (readB < toRead){ 
		rc = read(sockfd, tmp_buff + readB, size - readB); readB += rc;
		if (rc < 0)
		{
			perror("Client - Error read()ing peer_addr_len");
			goto err;
		}
	}

	peer_addr_len = ntohl(*((int *) &tmp_buff));
	printf("length = %llu\n", peer_addr_len);

	peer_addr = calloc(1, peer_addr_len);
	addr_buff = calloc(1, peer_addr_len);
	if (!peer_addr)
	{
		fprintf(stderr, "Client - calloc()\n");
		goto err;
	}

	toRead = peer_addr_len;
	readB = 0;
	while (readB < toRead) {
		rc = read(sockfd, addr_buff + readB, peer_addr_len - readB);
		readB += rc;
		printf("rc = %d\n", rc);
		if (rc < 0)
		{
			perror("Client - Error read()ing peer_addr");
			goto err;
		}
	}

	memcpy(peer_addr, addr_buff, peer_addr_len);
	free(addr_buff);

	printf("after read\n");

	return UCS_OK;

err:
	return UCS_ERR_UNSUPPORTED;
}

static ucs_status_t client_exch_address(char* server_name, int server_port) {
	ucs_status_t		status		= UCS_ERR_UNSUPPORTED;

	client_connect(server_name, server_port);
	GOTO_ERR_HANDLER(sockfd < 0, "Couldn't connect to server", err);


	status = client_get_peer_addr();

err:
	return status;
}

static void server_connect(int server_port) {
	int 				listenfd	= -1;
	int 				rc			= 0;
	int 				one 		= 1;
	struct sockaddr_in 	sockaddr 	= { 0 };

	listenfd = socket(AF_INET, SOCK_STREAM, 0);
	if (listenfd < 0)
	{
		perror("Server - socket()");
		return;
	}

	/* Setting socket to be reusable */
	rc = setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &one, sizeof(one));
	if (rc)
	{
		perror("Server - setsockopt()");
		goto err_listen;
	}

	sockaddr.sin_addr.s_addr = INADDR_ANY;
	sockaddr.sin_family 	 = AF_INET;
	sockaddr.sin_port 		 = htons((uint16_t)server_port);
	rc = bind(listenfd, (struct sockaddr*)&sockaddr, sizeof(sockaddr));
	if (rc)
	{
		perror("Server - bind()");
		goto err_listen;
	}

	rc = listen(listenfd, 10);
	if (rc)
	{
		perror("Server - listen()");
		goto err_listen;
	}

	fprintf(stdout, "Server - waiting for connections...\n");

	sockfd = accept(listenfd, NULL, NULL);
	if (sockfd < 0)
	{
		perror("Server - accept()");
		goto err_listen;
	}

err_listen:
	close(listenfd);
}

static ucs_status_t server_send_peer_addr() {
	size_t 		size = 0;
	int			rc	 = 0;

	size = sizeof(local_addr_len);
	rc = write(sockfd, &local_addr_len, size);
	if (rc != size)
	{
		perror("Server - Error send()ing local_addr_len");
		goto err;
	}

	rc = write(sockfd, local_addr, local_addr_len);
	if (rc != local_addr_len)
	{
		perror("Server - Error send()ing local_addr");
		goto err;
	}

	return UCS_OK;

err:
	return UCS_ERR_UNSUPPORTED;
}

static ucs_status_t server_exch_address(int server_port) {
	ucs_status_t		status		= UCS_ERR_UNSUPPORTED;

	server_connect(server_port);
	GOTO_ERR_HANDLER(sockfd < 0, "Couldn't connect to client", err);

	status = server_send_peer_addr();

err:
	return status;
}

static ucs_status_t ucp_init_tag_match(ucp_config_t *config, ucp_context_h *ucp_context) {
	ucs_status_t status;
	ucp_params_t ucp_params;

    /* Set ucp_params */
    ucp_params.features = UCP_FEATURE_TAG; 					/* Tag-matching feature 				*/
    if (ucp_test_mode == TEST_MODE_WAIT ||
    	ucp_test_mode == TEST_MODE_EVENTFD)
    	ucp_params.features |= UCP_FEATURE_WAKEUP;			/* If thread should sleep on events		*/
    ucp_params.request_size = sizeof(struct ucx_context);	/* Size to reserve for initialization	*/
    ucp_params.request_init = request_init;					/* Pointer to the CTX initializer		*/
    ucp_params.field_mask =									/* Masking parameters to update 		*/
    		UCP_PARAM_FIELD_FEATURES 		|
			UCP_PARAM_FIELD_REQUEST_INIT	|
			UCP_PARAM_FIELD_REQUEST_SIZE;

    /* Must be called before any other UCP function */
    /* Initializing CTX */
    status = ucp_init(&ucp_params, config, ucp_context);

    /* No need for config after initialization */
    ucp_config_release(config);

    return status;
}

static int pp_send_data_nb(ucp_worker_h worker, ucp_ep_h ep, void *buff, size_t buff_size, int first) {
	struct ucx_context * req_ptr;

	req_ptr = ucp_tag_send_nb(ep, buff, buff_size,
								ucp_dt_make_contig(1),
								tag, send_cb);
	/* Checking send request */
    if (UCS_PTR_IS_ERR(req_ptr))
    {
        fprintf(stderr, "Unable to send UCX address message\n");
        free(buff);
        return -1;
    }
    else if (UCS_PTR_STATUS(req_ptr) != UCS_OK)
    {
//        fprintf(stderr, "UCX PP message was scheduled for send\n"); //TODO
        wait_for_completion(worker, req_ptr, first);
        req_ptr->completed = 0;			/* Reset request state 	*/
        ucp_request_release(req_ptr); 	/* Ready to release 	*/
    }

    return 0;
}

static int pp_send_address_nb(ucp_ep_h ep, void *buff, size_t buff_size) {
	struct ucx_context * req_ptr;

	req_ptr = ucp_tag_send_nb(ep, buff, buff_size,
								ucp_dt_make_contig(1),
								tag, send_cb);
	/* Checking send request */
    if (UCS_PTR_IS_ERR(req_ptr))
    {
        fprintf(stderr, "Unable to send UCX address message\n");
        free(buff);
        return -1;
    }
    else if (UCS_PTR_STATUS(req_ptr) != UCS_OK)
    {
        fprintf(stderr, "UCX address message was scheduled for send\n");
        ucp_request_release(req_ptr); 	/* Ready to release */
    }

    return 0;
}

static int pp_recv_data_nb(ucp_worker_h worker, size_t buff_len,
							ucp_tag_message_h msg_tag, void* buff, int probe) {
	struct ucx_context *req_ptr = NULL;

	if (probe) {
		req_ptr = ucp_tag_msg_recv_nb(worker, buff, buff_len,
									ucp_dt_make_contig(1),
									msg_tag, recv_cb);
	}
	else {
		req_ptr = ucp_tag_recv_nb(worker, buff, buff_len,
								ucp_dt_make_contig(1), tag,
								tag_mask, recv_cb);
	}

    if (UCS_PTR_IS_ERR(req_ptr)) {
        fprintf(stderr, "unable to receive UCX data message (%u)\n",
                UCS_PTR_STATUS(req_ptr));
        free(buff);
        GOTO_ERR_HANDLER(1, "UCP Client - ucp_tag_recv_nb()", err);
    } else {
        wait_for_completion(worker, req_ptr, 0);
        req_ptr->completed = 0;			/* Reset request state 	*/
        ucp_request_release(req_ptr);	/* Ready to release 	*/
    }

    return 0;

err:
	return -1;
}

static ucs_status_t pp_check_mode(ucp_worker_h worker) {
	ucs_status_t status;

    if (ucp_test_mode == TEST_MODE_WAIT) {
        /* Waiting for incoming events (blocking) */
        status = ucp_worker_wait(worker);
    } else if (ucp_test_mode == TEST_MODE_EVENTFD) {
//        status = test_poll_wait(worker); //TODO implement
    }

	return status;
}

static ucs_status_t pp_wait_and_probe(ucp_worker_h worker, ucp_tag_recv_info_t *info, ucp_tag_message_h *msg_tag) {
	ucs_status_t 		status	= UCS_OK;

	do {
        /* Following blocked methods used to polling internal file descriptor
         * to make CPU idle and don't spin loop
         */
        status = pp_check_mode(worker);
        GOTO_ERR_HANDLER(status != UCS_OK, "pp_check_mode()", err);

        /* Progressing before probe to update the state */
        ucp_worker_progress(worker);

        /* Probing incoming events in non-blocking mode */
        *msg_tag = ucp_tag_probe_nb(worker, tag, tag_mask, 1, info);
    } while (*msg_tag == NULL);

err:
	return status;
}

/*
 * Probe for events and receive message from server
 */
static int pp_recv_wo_probe(ucp_worker_h worker, struct pp_msg *in_msg, int iter, size_t msg_len) {
	ucs_status_t 		status;
	int					rc 		= -1;

	status = pp_check_mode(worker);
	GOTO_ERR_HANDLER(status != UCS_OK, "pp_check_mode()", err);

	rc = pp_recv_data_nb(worker, msg_len, NULL, in_msg, 0);
	GOTO_ERR_HANDLER(rc || (in_msg->cnt ^ iter), "UCP Client - pp_recv_data_nb()", err);

	return 0;

err:
	return -1;

}

static struct pp_msg* pp_probe_and_recv(ucp_worker_h worker, size_t *in_msg_len) {
	ucs_status_t 		status;
	ucp_tag_recv_info_t info 	= { 0 };
	ucp_tag_message_h	msg_tag = NULL;
	size_t 				msg_len = 0;
	int					rc 		= 0;
	struct pp_msg	   *in_msg 	= NULL;

	status = pp_wait_and_probe(worker, &info, &msg_tag);
	GOTO_ERR_HANDLER(status != UCS_OK, "pp_wait_and_probe()", err);

	/* Message in queue, need to receive it */

	/* Allocate sufficient memory according to incoming data */
	msg_len = info.length;
	in_msg 	= calloc(1, msg_len);
	GOTO_ERR_HANDLER(!in_msg, "PP probe&recv - calloc()", err);

	rc = pp_recv_data_nb(worker, msg_len, msg_tag, in_msg, 1);
	GOTO_ERR_HANDLER(rc, "UCP Client (first) - pp_recv_data_nb()", err_recv);
	if (in_msg_len != NULL)
		*in_msg_len = msg_len;

	return in_msg;

err_recv:
	free(in_msg);
err:
	return NULL;
}

static int pp_run_ucp_server(ucp_worker_h worker) {
	ucs_status_t 	status		= UCS_ERR_LAST;
	struct pp_msg  *in_msg		= NULL;
	struct pp_msg  *out_msg		= NULL;
	void 		   *in_addr		= NULL;
	size_t			size		= 0;
	size_t			in_msg_size = 0;
	size_t			msg_size	= 0;
	int 			rc 			= -1;
	int 			i 			= 0;
	ucp_ep_params_t ep_params 	= { 0 };
	ucp_ep_h 		peer_ep		= NULL;
	char 			srvr_msg[]	= "Server\n";
	uint64_t 	   *cnt			= NULL;

	printf("IN pp_run_ucp_server()\n");

	/* Get address of client's worker */
	in_addr = pp_probe_and_recv(worker, NULL);

	size = sizeof(peer_addr_len);
	memcpy(&peer_addr_len, in_addr, size);
	peer_addr = calloc(1, peer_addr_len);
	GOTO_ERR_HANDLER(!peer_addr, "UCP Server - peer_addr calloc()", err);
	memcpy(peer_addr, in_addr + size, peer_addr_len);

	free(in_addr);

	/* Create end point that's connected to client's worker */
	ep_params.address 		= peer_addr;
	ep_params.field_mask 	= UCP_EP_PARAM_FIELD_REMOTE_ADDRESS;

	status = ucp_ep_create(worker, &ep_params, &peer_ep);
	GOTO_ERR_HANDLER(status != UCS_OK, "ucp_ep_create()", err);

	/* Start Ping-Pong */
    msg_size = pp_msg_size + sizeof(*out_msg);
    out_msg  = calloc(1, msg_size);
    GOTO_ERR_HANDLER(!out_msg, "UCP Server - calloc()", err_ep);
    cnt 	 = &out_msg->cnt;

	/* Ping-Pong loop */
	for (i = 0; i < iters; ++i) {
    	if (!i) {
    		in_msg = pp_probe_and_recv(worker, &in_msg_size);
    		GOTO_ERR_HANDLER(!in_msg, "UCP Server - pp_probe_and_recv()", err_msg);
    	}
    	else {
    		rc = pp_recv_wo_probe(worker, in_msg, i, in_msg_size);
    		GOTO_ERR_HANDLER(rc, "UCP Server - pp_recv_wo_probe()", err_msg);
    	}

    	GOTO_ERR_HANDLER(in_msg->cnt != i, "UCP Server - wrong msg received", err_msg);

		memcpy(out_msg + 1, srvr_msg, sizeof(srvr_msg));
		*cnt = i;

    	rc = pp_send_data_nb(worker, peer_ep, out_msg, msg_size, !i);
    	GOTO_ERR_HANDLER(rc, "UCP Server - pp_send_data_nb()", err_msg);
	}

err_msg:
	free(in_msg);
	free(out_msg);
err_ep:
	ucp_ep_destroy(peer_ep);
err:
	return rc;

}

static int pp_run_ucp_client(ucp_worker_h worker) {
	int 			 	rc 			= -1;
	ucp_ep_params_t  	ep_params 	= { 0 };
	ucs_status_t 	 	status		= UCS_ERR_LAST;
	ucp_ep_h 		 	peer_ep		= NULL;
	void			   *addr		= NULL;
	size_t			 	msg_size 	= 0;
	size_t			 	size		= sizeof(local_addr_len);
	size_t				in_msg_size = 0;
	struct pp_msg	   *out_msg		= NULL;
	struct pp_msg	   *in_msg		= NULL;
	uint64_t 		   *cnt			= NULL;
	char		   		clnt_msg[]	= "Client\n";
	int 				i			= 0;
	struct timeval		start		= { 0 };
	struct timeval		end			= { 0 };

	printf("IN pp_run_ucp_client()\n");

	/* Creating an End Point that will be connected to remote worker */
	ep_params.address 		= peer_addr;
	ep_params.field_mask 	= UCP_EP_PARAM_FIELD_REMOTE_ADDRESS;

	status = ucp_ep_create(worker, &ep_params, &peer_ep);
	GOTO_ERR_HANDLER(status != UCS_OK, "ucp_ep_create()", err);

	/* Send local address to remote worker */

	/* Allocating msg to be sent */
	msg_size = size + local_addr_len;
	addr	 = calloc(1, msg_size);
	GOTO_ERR_HANDLER(!addr, "UCP client - calloc()", err_ep);

	/* Constructing msg */
	memcpy(addr, &local_addr_len, size);
	memcpy(addr + size, local_addr, local_addr_len);

	rc = pp_send_address_nb(peer_ep, addr, msg_size);
	GOTO_ERR_HANDLER(rc, "Client - pp_send_address_nb()", err_ep);

    free(addr);

    /***************************************/

    /* Start Ping-Pong */

    rc = gettimeofday(&start, NULL);
    if (rc) {
    	perror("UCP Client - gettimeofday()");
    	goto err_ep;
    }

    msg_size = pp_msg_size + sizeof(*out_msg);
    printf("In C: msg size = %d\n", msg_size);
    out_msg  = calloc(1, msg_size);
    GOTO_ERR_HANDLER(!out_msg, "UCP Client - calloc()", err_ep);
    cnt 	 = &out_msg->cnt;

    /* Ping-Pong loop */
    for (i = 0; i < iters; ++i) {
		memcpy(out_msg + 1, clnt_msg, sizeof(clnt_msg));
		*cnt = i;

    	rc = pp_send_data_nb(worker, peer_ep, out_msg, msg_size, !i);
    	GOTO_ERR_HANDLER(rc, "UCP Client - pp_send_data_nb()", err_msg);

	
	/*
    	if (!i) {
    		in_msg = pp_probe_and_recv(worker, &in_msg_size);
    		GOTO_ERR_HANDLER(!in_msg, "UCP Client - pp_probe_and_recv()", err_msg);
    	}
    	else {
    		rc = pp_recv_wo_probe(worker, in_msg, i, in_msg_size);
    		GOTO_ERR_HANDLER(rc, "UCP Client - pp_recv_wo_probe()", err_msg);
    	}

    	GOTO_ERR_HANDLER(in_msg->cnt != i, "UCP Client - wrong msg received", err_msg);
	
	*/
	}

    rc = gettimeofday(&end, NULL);
    if (rc) {
    	perror("UCP Client - gettimeofday()");
    	goto err_msg;
    }

	{	// Calculating time passed
        float usec = (end.tv_sec - start.tv_sec) * 1000000 +
                (end.tv_usec - start.tv_usec);
        long long bytes = (long long) msg_size * iters * 2;

        printf("%lld bytes in %.4f seconds = %.4f Mbit/sec\n",
               bytes, usec / 1000000., bytes * 8. / usec);
        printf("%d iters in %.4f seconds = %.4f usec/iter\n",
               iters, usec / 1000000., usec / iters);
	}

err_msg:
	free(in_msg);
	free(out_msg);
err_ep:
	ucp_ep_destroy(peer_ep);
err:
	return rc;
}

static int pp_run_test(int is_server, ucp_worker_h worker) {
	int rc = -1;

	if (is_server)
		rc = pp_run_ucp_server(worker);
	else
		rc = pp_run_ucp_client(worker);

	return rc;
}

static void usage(char* arg0) {
    fprintf(stderr, "Usage:\n");
    fprintf(stderr, "\t%s [OPTION]...	start a server and wait for connection\n", arg0);
    fprintf(stderr, "\nOptions are:\n");
    fprintf(stderr, "  -w      	  Select test mode \"wait\" to test "
            "ucp_worker_wait function\n");
    fprintf(stderr, "  -f      	  Select test mode \"event fd\" to test "
            "ucp_worker_get_efd function with later poll\n");
    fprintf(stderr, "  -b      	  Select test mode \"busy polling\" to test "
            "ucp_tag_probe_nb and ucp_worker_progress (default)\n");
    fprintf(stderr, "  -t threads Select number of threads (default:1)");
    fprintf(stderr, "  -n name 	  Set node name or IP address "
            "of the server (required for client and should be ignored "
            "for server)\n");
    fprintf(stderr, "  -p port 	  Set alternative server port (default:29592)\n");
    fprintf(stderr, "  -s size 	  Set test string length (default:16)\n");
    fprintf(stderr, "  -I iter 	  Set number of iterations of PP (default:1000)\n");
    fprintf(stderr, "\n");
}

int parse_cmd(int argc, char *const argv[], char **server_name, int *server_port)
{
    int c = 0;
    opterr = 0;
    while ((c = getopt(argc, argv, "wfbn:p:s:h:I:t:")) != -1) {
        switch (c) {
        case 'w':
            ucp_test_mode = TEST_MODE_WAIT;
            break;
        case 'f':
            ucp_test_mode = TEST_MODE_EVENTFD;
            break;
        case 'b':
            ucp_test_mode = TEST_MODE_PROBE;
            break;
        case 'n':
            *server_name = optarg;
            break;
        case 'p':
            *server_port = strtol(optarg, NULL, 0);
            if (*server_port <= 0 || *server_port > UINT16_MAX) {
                fprintf(stderr, "Wrong server port number %d\n", *server_port);
                return UCS_ERR_UNSUPPORTED;
            }
            break;
        case 's':
            pp_msg_size = strtol(optarg, NULL, 0);
            if (pp_msg_size <= 0) {
                fprintf(stderr, "Wrong string size %zu\n", pp_msg_size);
                return UCS_ERR_UNSUPPORTED;
            }
            break;
        case 'I':
        	iters = strtol(optarg, NULL, 0);
        	if (iters <= 0) {
        		fprintf(stderr, "Invalid number of iterations: %d\n", iters);
        		return UCS_ERR_UNSUPPORTED;
        	}
        	break;
        case 't':
        	num_of_threads = strtol(optarg, NULL, 0);
        	if (num_of_threads <= 0) {
        		fprintf(stderr, "Invalid number of threads: %d\n", num_of_threads);
        		return UCS_ERR_UNSUPPORTED;
        	}
        	break;
        case 'h':
        default:
        	usage(argv[0]);
        	return UCS_ERR_UNSUPPORTED;
        }
    }

	if (optind == argc - 1)
		*server_name = argv[optind];
	else if (optind < argc)
	{
		usage(argv[0]);
		return 1;
	}

    return UCS_OK;
}

void pp_sync_before_close(int is_server) {
	int buff = 0;
	if (is_server)
		read(sockfd, &buff, 0);
	else
		write(sockfd, &buff, 0);
}

int main(int argc, char** argv) {
    /* UCP temporary vars */
    ucp_worker_params_t  worker_params 	= { 0 };
    ucp_config_t 		*config 		= NULL;
    ucs_status_t 		 status;

    /* UCP handler objects */
    ucp_context_h 		 ucp_context;
    ucp_worker_h 		 ucp_worker;

    /* OOB connection vars */
    char 				*server_name 	= NULL;
    int 				 server_port	= 29592;
    int 				 rc 			= -1;
    int					 is_server		= 0;

    status = parse_cmd(argc, argv, &server_name, &server_port);
    GOTO_ERR_HANDLER(status != UCS_OK, "parse_cmd()", err_config);

    is_server = !server_name;

    /* UCP initialization */
    status = ucp_config_read(NULL, NULL, &config);
	GOTO_ERR_HANDLER(status != UCS_OK, "ucp_config_read()", err_config);

    status = ucp_init_tag_match(config, &ucp_context);
    GOTO_ERR_HANDLER(status != UCS_OK, "ucp_init()", err_ctx_init);

//    ucp_context_print_info(ucp_context, stdout); //TODO delete

	// TODO single/multi
	worker_params.thread_mode = UCS_THREAD_MODE_SINGLE;

	/* Creating worker dedicated to CTX ucp_context */
	status = ucp_worker_create(ucp_context, &worker_params, &ucp_worker);
	GOTO_ERR_HANDLER(status != UCS_OK, "ucp_worker_create()", err_worker_init);

	/* Acquiring address of worker - to be sent to distant UCP ep */
	status = ucp_worker_get_address(ucp_worker, &local_addr, &local_addr_len);
	GOTO_ERR_HANDLER(status != UCS_OK, "ucp_worker_get_address()", err_worker_addr);

	if (is_server)
		status = server_exch_address(server_port);
	else
		status = client_exch_address(server_name, server_port);

	GOTO_ERR_HANDLER(status != UCS_OK, "address exchange", err_exch);

	rc = pp_run_test(is_server, ucp_worker);

	pp_sync_before_close(is_server);
	close(sockfd);

err_exch:
	ucp_worker_release_address(ucp_worker, local_addr);
	free(peer_addr);

err_worker_addr:
	ucp_worker_destroy(ucp_worker);

err_worker_init:
	ucp_cleanup(ucp_context);

err_ctx_init:
err_config:
	return rc;

}









