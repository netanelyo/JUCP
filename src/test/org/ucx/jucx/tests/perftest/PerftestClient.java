package org.ucx.jucx.tests.perftest;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.LongStream;

import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfMeasurements;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfParams;
import org.ucx.jucx.tests.perftest.PerftestDataStructures.PerfTestType;
import org.ucx.jucx.utils.Time;

public interface PerftestClient {

	default public void printResults(PerfContext ctx) {
		PerfParams params = ctx.params;
		PerfMeasurements meas = ctx.measure;
		long[] results = meas.timeSamples();
		int iters = meas.iters;
		int size = params.size;
		Function<Double, String> printable = (d) -> new DecimalFormat("#0.000").format(d);
		
		int factor = 1;
		if (params.testType == PerfTestType.UCP_TEST_PINGPONG)
			factor = 2;
		long total = LongStream.of(results).sum();
		double[] percentile = { 0.99999, 0.9999, 0.999, 0.99, 0.90, 0.50 };
		
		Arrays.sort(results);
		String format = "%-25s = %-10s";
		System.out.println(String.format(format, "---> <MAX> observation", printable.apply(Time.nanosToUsecs(results[results.length - 1]) / factor)));
		for (double per : percentile) {
			int index = (int)(0.5 + per*iters) - 1;
			System.out.println(String.format(format, "---> percentile " + per, printable.apply(Time.nanosToUsecs(results[index]) / factor)));
		}
		System.out.println(String.format(format, "---> <MIN> observation", printable.apply(Time.nanosToUsecs(results[0]) / factor)));
		
		System.out.println();
		
		double secs = Time.nanosToSecs(total);
		double totalMBytes = (double)size * iters / Math.pow(2, 20);
		System.out.println("average latency (usec): " + printable.apply(Time.nanosToUsecs(total) / iters / factor));
		System.out.println("message rate (msg/s): " + (int)(iters/secs));
		System.out.println("bandwidth (MB/s) : " + printable.apply(totalMBytes/secs));
	}
}
