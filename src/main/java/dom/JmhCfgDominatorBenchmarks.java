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
package dom;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import nl.cwi.swat.jmh_dscg_benchmarks.BenchmarkUtils;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.io.BinaryValueReader;
import org.eclipse.imp.pdb.facts.util.DefaultTrieSet;
import org.eclipse.imp.pdb.facts.util.ImmutableSet;
import org.eclipse.imp.pdb.facts.util.TransientSet;
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

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class JmhCfgDominatorBenchmarks {

	/*
	 * (for (i <- 0 to 23) yield
	 * s"'${Math.pow(2, i).toInt}'").mkString(", ").replace("'", "\"")
	 */
	@Param({ "1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096" })
	protected int size;

	@Param({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" })
	protected int run;

	private final String DATA_SET_FULL_FILE_NAME = "data/wordpress-cfgs-as-graphs.bin";
	private IMap DATA_SET_FULL;

	private ArrayList<ISet> sampledGraphs;
	private ArrayList<ImmutableSet<ITuple>> sampledGraphsNativeToChart;

	@Setup(Level.Trial)
	public void setUp() throws Exception {
		deseriaizeFullDataSet();
		setUpTestSetWithRandomContent(size, run);

		// convert data to remove PDB dependency
		sampledGraphsNativeToChart = convertedDataToNativeFormatOfChart(sampledGraphs);

		// OverseerUtils.setup(JmhCfgDominatorBenchmarks.class, this);
	}

	private static ArrayList<ImmutableSet<ITuple>> convertedDataToNativeFormatOfChart(
					ArrayList<ISet> sampledGraphs) {
		// convert data to remove PDB dependency
		ArrayList<ImmutableSet<ITuple>> sampledGraphsNative = new ArrayList<>(sampledGraphs.size());

		for (ISet graph : sampledGraphs) {
			TransientSet<ITuple> convertedValue = DefaultTrieSet.transientOf();

			for (IValue tuple : graph) {
				convertedValue.__insert((ITuple) tuple);
			}

			sampledGraphsNative.add(convertedValue.freeze());
		}

		return sampledGraphsNative;
	}

	protected void deseriaizeFullDataSet() {
		IValueFactory vf = org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance();

		try {
			DATA_SET_FULL = (IMap) new BinaryValueReader().read(vf, new FileInputStream(
							DATA_SET_FULL_FILE_NAME));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		System.err.println("Global data initialized.");
		System.err.println("Total number of entries: " + DATA_SET_FULL.size());
		System.err.println();

	}
	
	protected void setUpTestSetWithRandomContent(int size, int run) throws Exception {
		int seedForThisTrial = BenchmarkUtils.seedFromSizeAndRun(size, run);
		Random rand = new Random(seedForThisTrial);
		System.out.println(String.format("Seed for this trial: %d.", seedForThisTrial));

		// select sample based on random indices
		Set<Integer> sampledIndices = new HashSet<>(size * 2);

		while (sampledIndices.size() <= size) {
			sampledIndices.add(rand.nextInt(DATA_SET_FULL.size()));
		}

		// sample data
		sampledGraphs = new ArrayList<>(size);

		int dataSetCursor = 0;
		for (Iterator<IValue> dataSetIterator = DATA_SET_FULL.iterator(); dataSetIterator.hasNext(); dataSetCursor++) {
			if (sampledIndices.contains(dataSetCursor)) {
				IValue mapKey = dataSetIterator.next();
				ISet mapValue = (ISet) DATA_SET_FULL.get(mapKey);

				sampledGraphs.add(mapValue);
			} else {
				dataSetIterator.next();
			}
		}
	}

	// @TearDown(Level.Trial)
	// public void tearDown() {
	// OverseerUtils.tearDown();
	// }

	// @Setup(Level.Iteration)
	// public void setupIteration() {
	// OverseerUtils.doRecord(true);
	// }
	//
	// @TearDown(Level.Iteration)
	// public void tearDownIteration() {
	// OverseerUtils.doRecord(false);
	// }

	// @Setup(Level.Invocation)
	// public void setupInvocation() {
	// OverseerUtils.setup(JmhCfgDominatorBenchmarks.class, this);
	// OverseerUtils.doRecord(true);
	// }
	//
	// @TearDown(Level.Invocation)
	// public void tearDownInvocation() {
	// OverseerUtils.doRecord(false);
	// }

	@Benchmark
	public void timeDominatorCalculation(Blackhole bh) {
		for (ImmutableSet<ITuple> graph : sampledGraphsNativeToChart) {
			try {
				bh.consume(new DominatorsWithoutPDB().calculateDominators(graph));
			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
		.include(".*" + JmhCfgDominatorBenchmarks.class.getSimpleName() + ".(timeDominatorCalculation)")
						.warmupIterations(5).measurementIterations(5).mode(Mode.SingleShotTime).forks(1)
						.param("run", "0").build();

		new Runner(opt).run();
	}

}
