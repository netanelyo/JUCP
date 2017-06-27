package org.ucx.jucx.tests;

import java.nio.ByteBuffer;

import org.ucx.jucx.tests.PerftestUtils.*;

public class PerfContext {

	
	
	int 		maxIter 	= 100000;
	int 		port		= 12345;
	boolean 	isServer	= true;
	String 		server		= null;
	PerfParams 	params;
	ByteBuffer 	sendBuffer;
	ByteBuffer 	recvBuffer;
	PerfTime 	time;
	UcpObjects	ucpObj;
	
}
