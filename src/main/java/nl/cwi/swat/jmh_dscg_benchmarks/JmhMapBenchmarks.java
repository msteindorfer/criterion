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

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import nl.cwi.swat.jmh_dscg_benchmarks.BenchmarkUtils.ValueFactoryFactory;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
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

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
public class JmhMapBenchmarks {

	public static enum DataType {
		MAP
	}

	@Param
	public DataType dataType = DataType.MAP;

	public IValueFactory valueFactory;

	@Param
	public ValueFactoryFactory valueFactoryFactory;

	@Param({ "10", "100", "1000", "10000", "100000", "1000000" })
	// "10", "100", "1000",
	protected int size;

	public IMap testMap;
	private IMap testMapRealDuplicate;
	private IMap testMapDeltaDuplicate;

	public IValue VALUE_EXISTING;
	public IValue VALUE_NOT_EXISTING;

	public static final int CACHED_NUMBERS_SIZE = 8;
	public IValue[] cachedNumbers = new IValue[CACHED_NUMBERS_SIZE];

	@Setup(Level.Trial)
	public void setUp() throws Exception {
		setUpTestSetWithRandomContent(size);

		// random data generator with fixed seed
		Random randForOperations = new Random(2147483647L); // seed == Mersenne
															// Prime #8

		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
			cachedNumbers[i] = valueFactory.integer(randForOperations.nextInt());
		}
	}

	protected void setUpTestSetWithRandomContent(int size) throws Exception {
		valueFactory = valueFactoryFactory.getInstance();

		IMapWriter writer1 = valueFactory.mapWriter();
		IMapWriter writer2 = valueFactory.mapWriter();

		// random data generator with fixed seed
		Random rand = new Random(2305843009213693951L); // seed == Mersenne
														// Prime #9

		/*
		 * randomly choose one element amongst the elements
		 */
		int existingValueIndex = rand.nextInt(size);

		for (int i = size - 1; i >= 0; i--) {
			final int j = rand.nextInt();
			final IValue current = valueFactory.integer(j);

			writer1.put(current, current);
			writer2.put(current, current);

			if (i == existingValueIndex) {
				VALUE_EXISTING = valueFactory.integer(j);
			}
		}

		testMap = writer1.done();
		testMapRealDuplicate = writer2.done();

		/*
		 * generate random values until a value not part of the data strucure is
		 * found
		 */
		while (VALUE_NOT_EXISTING == null) {
			final IValue candidate = valueFactory.integer(rand.nextInt());

			if (!testMap.containsKey(candidate)) {
				VALUE_NOT_EXISTING = candidate;
			}
		}

		testMapDeltaDuplicate = testMap.put(VALUE_EXISTING, VALUE_NOT_EXISTING).put(VALUE_EXISTING,
						VALUE_EXISTING);
	}

	@Benchmark
	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public void timeContainsKey(Blackhole bh) {
		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
			bh.consume(testMap.containsKey(cachedNumbers[i % CACHED_NUMBERS_SIZE]));
		}
	}

	@Benchmark
	public void timeIteration(Blackhole bh) {
		for (Iterator<IValue> iterator = testMap.iterator(); iterator.hasNext();) {
			bh.consume(iterator.next());
		}
	}

	@Benchmark
	public void timeEntryIteration(Blackhole bh) {
		for (Iterator<java.util.Map.Entry<IValue, IValue>> iterator = testMap.entryIterator(); iterator
						.hasNext();) {
			bh.consume(iterator.next());
		}
	}

	@Benchmark
	@OperationsPerInvocation(CACHED_NUMBERS_SIZE)
	public void timeInsert(Blackhole bh) {
		for (int i = 0; i < CACHED_NUMBERS_SIZE; i++) {
			bh.consume(testMap.put(cachedNumbers[i % CACHED_NUMBERS_SIZE], VALUE_NOT_EXISTING));

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
		Options opt = new OptionsBuilder()
						.include(".*" + JmhMapBenchmarks.class.getSimpleName() + ".*").forks(1)
						.warmupIterations(5).measurementIterations(5).build();

		new Runner(opt).run();
	}

}
