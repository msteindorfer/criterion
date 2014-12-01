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

import java.util.concurrent.TimeUnit;

import nl.cwi.swat.jmh_dscg_benchmarks.BenchmarkUtils.DataType;
import nl.cwi.swat.jmh_dscg_benchmarks.BenchmarkUtils.SampleDataSelection;

import org.eclipse.imp.pdb.facts.IValue;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public abstract class AbstractJmhMapBenchmark {

	@Param({ "MAP" })
	protected DataType dataType;

	@Param({ "MATCH" })
	protected SampleDataSelection sampleDataSelection;

	/*
	 * (for (i <- 0 to 23) yield
	 * s"'${Math.pow(2, i).toInt}'").mkString(", ").replace("'", "\"")
	 */
	@Param({ "1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096",
					"8192", "16384", "32768", "65536", "131072", "262144", "524288", "1048576",
					"2097152", "4194304", "8388608" })
	protected int size;

	@Param({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" })
	protected int run;

	protected static final int CACHED_NUMBERS_SIZE = 8;
	protected Object[] cachedNumbers = new IValue[CACHED_NUMBERS_SIZE];
	protected Object[] cachedNumbersNotContained = new IValue[CACHED_NUMBERS_SIZE];

//	@Setup(Level.Trial)
//	public void setUp() throws Exception {
//		setUpTestMapWithRandomContent(size, run);
//
//		switch (sampleDataSelection) {
//
//		/*
//		 * random integers might or might not be in the dataset
//		 */
//		case RANDOM: {
//			// random data generator with fixed seed
//			/* seed == Mersenne Prime #8 */
//			Random randForOperations = new Random(2147483647L);
//
//			for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
//				cachedNumbers[i] = valueFactory.integer(randForOperations.nextInt());
//			}
//		}
//
//		/*
//		 * random integers are in the dataset
//		 */
//		case MATCH: {
//			// random data generator with fixed seed
//			int seedForThisTrial = BenchmarkUtils.seedFromSizeAndRun(size, run);
//			Random rand = new Random(seedForThisTrial);
//
//			if (CACHED_NUMBERS_SIZE < size) {
//				for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
//					if (i >= size) {
//						cachedNumbers[i] = cachedNumbers[i % size];
//					} else {
//						cachedNumbers[i] = valueFactory.integer(rand.nextInt());
//					}
//				}
//			} else {
//				for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
//					cachedNumbers[i] = valueFactory.integer(rand.nextInt());
//				}
//			}
//
//			// random data generator with fixed seed
//			/* seed == Mersenne Prime #8 */
//			Random anotherRand = new Random(2147483647L);
//
//			for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
//				/*
//				 * generate random values until a value not part of the data
//				 * strucure is found
//				 */
//				boolean found = false;
//				while (!found) {
//					final IValue candidate = valueFactory.integer(anotherRand.nextInt());
//
//					if (testMap.containsKey(candidate)) {
//						continue;
//					} else {
//						cachedNumbersNotContained[i] = candidate;
//						found = true;
//					}
//				}
//			}
//
//			// assert (contained)
//			for (IValue sample : cachedNumbers) {
//				if (!testMap.containsKey(sample)) {
//					throw new IllegalStateException();
//				}
//			}
//
//			// assert (not contained)
//			for (IValue sample : cachedNumbersNotContained) {
//				if (testMap.containsKey(sample)) {
//					throw new IllegalStateException();
//				}
//			}
//		}
//		}
//
//		System.out.println(String.format("\n\ncachedNumbers = %s", Arrays.toString(cachedNumbers)));
//		System.out.println(String.format("cachedNumbersNotContained = %s\n\n",
//						Arrays.toString(cachedNumbersNotContained)));
//	}

//	@Benchmark
//	public void timeContainsKeySingle(Blackhole bh) {
//		bh.consume(testMap.containsKey(VALUE_EXISTING));
//	}

	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public abstract void timeContainsKey(Blackhole bh);

	public abstract void timeIteration(Blackhole bh);

	public abstract void timeEntryIteration(Blackhole bh);

//	@Benchmark
//	public void timeInsertSingle(Blackhole bh) {
//		bh.consume(testMap.put(VALUE_NOT_EXISTING, VALUE_NOT_EXISTING));
//	}

	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public abstract void timeInsert(Blackhole bh);
	
	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public abstract void timeRemoveKey(Blackhole bh);

	public abstract void timeEqualsRealDuplicate(Blackhole bh);

	public abstract void timeEqualsDeltaDuplicate(Blackhole bh);

}
