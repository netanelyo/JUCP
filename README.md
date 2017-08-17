<h1>JUCP</h1>

TODO: add description

<h2>Build instructions:</h2>
1. Download: git clone https://github.com/netanelyo/JUCP.git</br>
2. Move into folder: cd JUCP</br>
3. Set JAVA_HOME: export JAVA_HOME=/usr/lib/jvm/java-1.x.x-openjdk (x.x - according to java version)</br>
4. Build: python build.py (pulls the relevant C level UCX library and builds everything you need)</br>

<h2>Examples:</h2>
<h3>HelloWorld over UCP's tag matching API</h3>
&bull; Server side creates a local worker, and waits for a (TCP) connection.</br>
&bull; Upon connection - server sends the worker address to the client side.</br>
&bull; Client side sends a single message to server, then exits.</br>
&bull; Server prints message and exits.</br>

<h4>Run Examples:</h4>
Server side: ./examples/runHelloWorld.sh server [OPTION]...</br>
1. ./examples/runHelloWorld.sh server
	<pre>	Waiting for connections...
	Connected to: 40.40.40.12
	Received:
	UCP Client says hello!
	[SUCCESS] Exiting...</pre>
Client side: ./examples/runHelloWorld.sh client [&lt;Host_IP_address&gt;] [OPTION]...</br>
2. ./examples/runHelloWorld.sh client 40.40.40.12
	<pre>	Connected to: 40.40.40.12
	[SUCCESS] Exiting...</pre>
	
<h2>Benchmarks:</h2>
<h3>Java UCP latency performance test</h3>
Ping-pong application with 10K warmup iterations and 1M time sampling iterations, each message - 64B (by default; can be tuned through cmd-line).</br>
&bull; Server and client exchanges worker addresses through a TCP connection.</br>
&bull; Client sends message then wait for a response from server.</br>
&bull; Client measures RTT time (time passed from send until the response), then calculates latency as &frac12;RTT
<h4>Run latency test:</h4>
1. Server: ./tests/perftest.sh ([-h for more info])
	<pre>	Waiting for connections...
	Connected to: 40.40.40.12
	*****   Latency Test   *****
	# iterations: 1000000
	Message size: 64</pre>
2. Client: ./tests/perftest.sh 40.40.40.12 ([-h for more info])
	<pre>	Connected to: 40.40.40.12
	*****   Latency Test   *****
	# iterations: 1000000
	Message size: 64</pre>
	<pre>	Latency Test Results:
	---> &lt;MAX&gt; observation    = 1323.649
	---> percentile 0.99999   = 34.325
	---> percentile 0.9999    = 4.933
	---> percentile 0.999     = 3.372
	---> percentile 0.99      = 2.212
	---> percentile 0.9       = 1.919
	---> percentile 0.5       = 1.609
	---> &lt;MIN&gt; observation    = 1.467</pre>
	<pre>	average latency (usec): 1.683
	message rate (msg/s): 297072
	bandwidth (MB/s) : 18.132</pre>
<h3>Java UCP bandwidth performance test</h3>
Bandwidth benchmark application with 10K warmup iterations and 1M time sampling iterations, each message - 64B (by default; can be tuned through cmd-line).</br>
&bull; Server waits for a TCP connection then sends worker address to client.</br>
&bull; Server waits for incoming messages and does not respond.</br>
&bull; Client sends messages in a loop without expecting a response.
<p>&bull; Client measures time difference between two consecutive sends; average latency is <code>(endTime - startTime) / #messages</code> and observations are as mentioned above.</p>
<h4>Run bandwidth test:</h4>
1. Server: ./tests/perftest.sh ([-h for more info])
	<pre>	Waiting for connections...
	Connected to: 40.40.40.12
	*****   Bandwidth Test   *****
	# iterations: 1000000
	Message size: 64</pre>
2. Client: ./tests/perftest.sh 40.40.40.12 -b ([-h for more info])
	<pre>	Connected to: 40.40.40.12
        ****   Bandwidth Test   ****
        # iterations: 1000000
        Message size: 64</pre>
	<pre>	Bandwidth Test Results:
        ---> &lt;MAX&gt; observation    = 2663.861
        ---> percentile 0.99999   = 15.586
        ---> percentile 0.9999    = 3.773
        ---> percentile 0.999     = 1.259
        ---> percentile 0.99      = 0.589
        ---> percentile 0.9       = 0.369
        ---> percentile 0.5       = 0.316
        ---> &lt;MIN&gt; observation    = 0.304</pre>
	<pre>	average latency (usec): 0.348
        message rate (msg/s): 2873997
        bandwidth (MB/s) : 175.415</pre>
