/*******************************************************************************
 * Copyright (c) 2014 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package nl.cwi.swat.jmh_dscg_benchmarks;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class JmhArrayCopyBenchmarks {

	/*
	 * (for (i <- 0 to 23) yield s"'${Math.pow(2, i).toInt}'").mkString(", ").replace("'", "\"")
	 */
	@Param({ "1", "2", "4", "8", "16", "32", "64", "128", "256", "512" })
	protected int size;

	Object[] src;
	
	@Setup(Level.Trial)
	public void setUp() throws Exception {
		src = new Object[size];

		// random data generator with fixed seed
		/* seed == Mersenne Prime #8 */
		Random randForOperations = new Random(2147483647L);

		for (int i = 0; i < size; i++) {
			src[i] = randForOperations.nextInt();
		}
	}

	@Benchmark
	public void timeArraycopyAndInsertValue_Begin(Blackhole bh) {
		bh.consume(arraycopyAndInsertValue(null, src, 0, new Integer(34), new Integer(34)));
	}
	
	@Benchmark
	public void timeArraycopyAndInsertValue_Middle(Blackhole bh) {
		bh.consume(arraycopyAndInsertValue(null, src, size/2, new Integer(34), new Integer(34)));
	}

	@Benchmark
	public void timeArraycopyAndInsertValue_End(Blackhole bh) {
		bh.consume(arraycopyAndInsertValue(null, src, size, new Integer(34), new Integer(34)));
	}
	
	@Benchmark
	public void timeArraycopyAndInsertValue_SingleOperationAtEnd(Blackhole bh) {
		final java.lang.Object[] dst = new Object[src.length + 2];
		System.arraycopy(src, 0, dst, 0, src.length);
		dst[src.length] = new Integer(34);
		dst[src.length + 1] = new Integer(34);		
		bh.consume(dst);
	}	
	
	static final java.lang.Object[] arraycopyAndInsertValue(
					final AtomicReference<Thread> mutator, final java.lang.Object[] src,
					final int idx, final Object key, final Object val) {
		final java.lang.Object[] dst = new Object[src.length + 2];


		// copy 'src' and insert 2 element(s) at position 'idx'
		System.arraycopy(src, 0, dst, 0, idx);
		dst[idx] = key;
		dst[idx + 1] = val;
		System.arraycopy(src, idx, dst, idx + 2, src.length - idx);

		return dst;
	}	
	
	public static void main(String[] args) throws RunnerException {
		String includeString = "nl.cwi.swat.jmh_dscg_benchmarks." + JmhArrayCopyBenchmarks.class.getSimpleName() + ".*";
		System.out.println(includeString);
		
		Options opt = new OptionsBuilder()
						.include(includeString).forks(1)
						.warmupIterations(5).measurementIterations(5)
//						.param("size", "24000")
						.build();

		new Runner(opt).run();
	}

}
