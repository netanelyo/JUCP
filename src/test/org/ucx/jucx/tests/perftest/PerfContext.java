package org.ucx.jucx.tests.perftest;

import org.ucx.jucx.Worker.Callback;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfMeasurements;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.UcpObjects;

public class PerfContext {
	PerfParams 			params;
	UcpObjects			ucpObj;
	Callback			cb;
	PerfMeasurements 	measure = null;
	boolean				print;

	PerfContext(PerfParams params) {
		this.params = params;
		measure = new PerfMeasurements(params.maxIter);
		print = params.print;
	}
}







