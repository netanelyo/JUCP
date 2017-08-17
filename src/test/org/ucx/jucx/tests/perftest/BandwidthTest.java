package org.ucx.jucx.tests.perftest;

import java.nio.ByteBuffer;

import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;

public abstract class BandwidthTest extends PerftestBase {

	protected ByteBuffer buffer;
	
	@Override
	protected void run(PerfParams params) {
		ctx = new PerfContext(params);
		ctx.cb = new PerftestCallback();
		initBuffer();
		super.run(params);
	}
	
	protected void initBuffer() {
		buffer = ByteBuffer.allocateDirect(ctx.params.size);
	}
}






