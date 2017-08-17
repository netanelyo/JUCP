<h1>JUCP</h1>

TODO: add description

<h2>Build instructions:</h2>
1. Download: git clone https://github.com/netanelyo/JUCP.git</br>
2. Move into folder: cd JUCP</br>
3. Set JAVA_HOME: export JAVA_HOME=/usr/lib/jvm/java-1.x.x-openjdk (x.x - according to java version)</br>
4. Build: python build.py (pulls the relevant C level UCX library and builds everything you need)</br>

<h2>Examples:</h2>
<h3>HelloWorld over UCP's tag matching API</h3>
&bull; Server side creates a local worker, then sends the worker address to the client side.</br>
&bull; Client side sends a single message to server, then exits.</br>
&bull; Server prints message and exits.</br>

<h4>Run Examples:</h4>
Server side: ./examples/runHelloWorld.sh server [OPTION]...</br>
1. ./examples/runHelloWorld.sh server</br>
	Waiting for connections...</br>
	Connected to: 40.40.40.12</br>
	Received:</br>
	UCP Client says hello!</br>
	[SUCCESS] Exiting...</br>
Client side: ./examples/runHelloWorld.sh client [<Host_IP_address>] [OPTION]...</br>
2. ./examples/runHelloWorld.sh client 40.40.40.12</br>
	Connected to: 40.40.40.12</br>
	[SUCCESS] Exiting...</br>

<h2>Tests:</h2>
<h3>Java UCP performance test</h3>
<h4>Run latency test:</h4>
1. Server: ./tests/perftest.sh ([-h for more info])</br>
	Waiting for connections...</br>
	Connected to: 40.40.40.12</br>
	*****   Latency Test   *****</br>
	# iterations: 1000000</br>
	Message size: 64</br>

2. Client: ./tests/perftest.sh 40.40.40.12 ([-h for more info])</br>
	Connected to: 40.40.40.12</br>
	*****   Latency Test   *****</br>
	# iterations: 1000000</br>
	Message size: 64</br>
</br>
	Latency Test Results:</br>
	---> <MAX> observation    = 1323.649</br>
	---> percentile 0.99999   = 34.325</br>
	---> percentile 0.9999    = 4.933</br>
	---> percentile 0.999     = 3.372</br>
	---> percentile 0.99      = 2.212</br>
	---> percentile 0.9       = 1.919</br>
	---> percentile 0.5       = 1.609</br>
	---> <MIN> observation    = 1.467</br>
</br>
	average latency (usec): 1.683</br>
	message rate (msg/s): 297072</br>
	bandwidth (MB/s) : 18.132</br>

<h4>Run bandwidth test:</h4>
1. Server: ./tests/perftest.sh ([-h for more info])</br>
	Waiting for connections...</br>
	Connected to: 40.40.40.12</br>
	*****   Bandwidth Test   *****</br>
	# iterations: 1000000</br>
	Message size: 64</br>

2. Client: ./tests/perftest.sh 40.40.40.12 -b ([-h for more info])</br>
	Connected to: 40.40.40.12</br>
	****   Bandwidth Test   ****</br>
	# iterations: 1000000</br>
	Message size: 64</br>
</br>
	Bandwidth Test Results:</br>
	---> <MAX> observation    = 2663.861</br>
	---> percentile 0.99999   = 15.586</br>
	---> percentile 0.9999    = 3.773</br>
	---> percentile 0.999     = 1.259</br>
	---> percentile 0.99      = 0.589</br>
	---> percentile 0.9       = 0.369</br>
	---> percentile 0.5       = 0.316</br>
	---> <MIN> observation    = 0.304</br>
</br>
	average latency (usec): 0.348</br>
	message rate (msg/s): 2873997</br>
	bandwidth (MB/s) : 175.415</br>
