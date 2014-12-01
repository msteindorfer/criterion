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

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import nl.cwi.swat.jmh_dscg_benchmarks.BenchmarkUtils.DataType;
import nl.cwi.swat.jmh_dscg_benchmarks.BenchmarkUtils.SampleDataSelection;
import nl.cwi.swat.jmh_dscg_benchmarks.BenchmarkUtils.ValueFactoryFactory;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.util.ImmutableMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
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

public class JmhMapBenchmark_PDB_PERSISTENT extends AbstractJmhMapBenchmark {

	private ImmutableMap testMap;
	private ImmutableMap testMapRealDuplicate;
	private ImmutableMap testMapDeltaDuplicate;

	private Object VALUE_EXISTING;
	private Object VALUE_NOT_EXISTING;

	private static Object createObject(int value) {
		return org.eclipse.imp.pdb.facts.impl.primitive.IntegerValue.newInteger(value);
	};
	
	@Setup(Level.Trial)
	public void setUp() throws Exception {
//		setUpTestMapWithRandomContent(size, run);
//
//		switch (sampleDataSelection) {
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
//						cachedNumbers[i] = createObject(rand.nextInt());
//					}
//				}
//			} else {
//				for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
//					cachedNumbers[i] = createObject(rand.nextInt());
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
//					final Object candidate = createObject(anotherRand.nextInt());
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
//			for (Object sample : cachedNumbers) {
//				if (!testMap.containsKey(sample)) {
//					throw new IllegalStateException();
//				}
//			}
//
//			// assert (not contained)
//			for (Object sample : cachedNumbersNotContained) {
//				if (testMap.containsKey(sample)) {
//					throw new IllegalStateException();
//				}
//			}
//		}
//		default: {
//			throw new IllegalStateException();
//		}
//		}
//
//		System.out.println(String.format("\n\ncachedNumbers = %s", Arrays.toString(cachedNumbers)));
//		System.out.println(String.format("cachedNumbersNotContained = %s\n\n",
//						Arrays.toString(cachedNumbersNotContained)));
	}

	protected void setUpTestMapWithRandomContent(int size, int run) throws Exception {
//		valueFactory = valueFactoryFactory.getInstance();
//
//		IMapWriter writer1 = valueFactory.mapWriter();
//		IMapWriter writer2 = valueFactory.mapWriter();
//
//		int seedForThisTrial = BenchmarkUtils.seedFromSizeAndRun(size, run);
//		Random rand = new Random(seedForThisTrial);
//
//		System.out.println(String.format("Seed for this trial: %d.", seedForThisTrial));
//
//		/*
//		 * randomly choose one element amongst the elements
//		 */
//		int existingValueIndex = new Random(seedForThisTrial + 13).nextInt(size);
//
//		for (int i = size - 1; i >= 0; i--) {
//			final int j = rand.nextInt();
//			final IValue current = valueFactory.integer(j);
//
//			writer1.put(current, current);
//			writer2.put(current, current);
//
//			if (i == existingValueIndex) {
//				VALUE_EXISTING = valueFactory.integer(j);
//			}
//		}
//
//		testMap = writer1.done();
//		testMapRealDuplicate = writer2.done();
//
//		/*
//		 * generate random values until a value not part of the data strucure is
//		 * found
//		 */
//		while (VALUE_NOT_EXISTING == null) {
//			final IValue candidate = valueFactory.integer(rand.nextInt());
//
//			if (!testMap.containsKey(candidate)) {
//				VALUE_NOT_EXISTING = candidate;
//			}
//		}
//
//		testMapDeltaDuplicate = testMap.put(VALUE_EXISTING, VALUE_NOT_EXISTING).put(VALUE_EXISTING,
//						VALUE_EXISTING);
	}

//	@Benchmark
//	public void timeContainsKeySingle(Blackhole bh) {
//		bh.consume(testMap.containsKey(VALUE_EXISTING));
//	}

	@Benchmark
	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public void timeContainsKey(Blackhole bh) {
		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
			bh.consume(testMap.containsKey(cachedNumbers[i]));
		}
		// bh.consume(testMap.containsKey(VALUE_EXISTING));
	}

	@Benchmark
	public void timeIteration(Blackhole bh) {
		for (Iterator<?> iterator = testMap.keyIterator(); iterator.hasNext();) {
			bh.consume(iterator.next());
		}
	}

	@Benchmark
	public void timeEntryIteration(Blackhole bh) {
		for (Iterator<java.util.Map.Entry<?, ?>> iterator = testMap.entryIterator(); iterator
						.hasNext();) {
			bh.consume(iterator.next());
		}
	}

	@Benchmark
	public void timeInsertSingle(Blackhole bh) {
		bh.consume(testMap.__put(VALUE_NOT_EXISTING, VALUE_NOT_EXISTING));
	}

	@Benchmark
	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public void timeInsert(Blackhole bh) {
		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
			bh.consume(testMap.put(cachedNumbersNotContained[i], VALUE_NOT_EXISTING));

		}
	}

	@Benchmark
	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public void timeRemoveKey(Blackhole bh) {
		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
			bh.consume(testMap.__remove(cachedNumbers[i]));
		}
	}

	@Benchmark
	public void timeEqualsRealDuplicate(Blackhole bh) {
		bh.consume(testMap.equals(testMapRealDuplicate));
	}

	@Benchmark
	public void timeEqualsDeltaDuplicate(Blackhole bh) {
		bh.consume(testMap.equals(testMapDeltaDuplicate));
	}

	public static void main(String[] args) throws RunnerException {
		System.out.println(JmhMapBenchmark_PDB_PERSISTENT.class.getSimpleName());
		Options opt = new OptionsBuilder()
						.include(".*" + JmhMapBenchmark_PDB_PERSISTENT.class.getSimpleName() + ".(timeIteration)")
						.forks(1).warmupIterations(5).measurementIterations(5)
						.mode(Mode.AverageTime).param("dataType", "MAP")
						.param("sampleDataSelection", "MATCH").param("size", "8388608")
						.param("valueFactoryFactory", "VF_PDB_PERSISTENT_CURRENT").build();

		new Runner(opt).run();
	}

}
