/*******************************************************************************
 * Copyright (c) 2014 CWI All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package dom;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IMap;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISetWriter;
import org.rascalmpl.value.ITuple;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.value.io.BinaryValueReader;
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

import dom.DominatorBenchmarkUtils.DominatorBenchmarkEnum;
import io.usethesource.criterion.BenchmarkUtils;
import joptsimple.internal.Strings;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class JmhCfgDominatorBenchmarks {

  @Param
  public DominatorBenchmarkEnum dominatorBenchmarkEnum;

  /*
   * (for (i <- 0 to 23) yield s"'${Math.pow(2, i).toInt}'").mkString(", ").replace("'", "\"")
   * 
   * Note: total entries in DATA_SET_FULL_FILE_NAME: 5018
   */
  @Param({"4096", "2048", "1024", "512", "256", "128"})
  protected int size;

  // @Param({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" })
  protected int run = 0;

  private DominatorBenchmark dominatorBenchmark;

  private final String DATA_SET_FULL_FILE_NAME = "data/wordpress-cfgs-as-graphs.bin";
  private IMap DATA_SET_FULL;

  private ArrayList<ISet> sampledGraphs;
  private ArrayList<?> sampledGraphsNative;

  @Setup(Level.Trial)
  public void setUp() throws Exception {
    deseriaizeFullDataSet();
    setUpTestSetWithRandomContent(size, run);

    // convert data to remove PDB dependency
    dominatorBenchmark = dominatorBenchmarkEnum.getBenchmark();
    sampledGraphsNative = dominatorBenchmark.convertDataToNativeFormat(sampledGraphs);
  }

  protected void deseriaizeFullDataSet() {
    IValueFactory vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();

    try {
      int bufferSize = 512 * 1024 * 1024;

      DATA_SET_FULL = (IMap) new BinaryValueReader().read(vf,
          new BufferedInputStream(new FileInputStream(DATA_SET_FULL_FILE_NAME), bufferSize));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // System.err.println("Global data initialized.");
    // System.err.println("Total number of entries: " +
    // DATA_SET_FULL.size());
    // System.err.println();
  }

  protected void setUpTestSetWithRandomContent(int size, int run) throws Exception {
    // int seedForThisTrial = BenchmarkUtils.seedFromSizeAndRun(size, run);

    // same seed for different sizes to achieve subsume relationship
    int seedForThisTrial = BenchmarkUtils.seedFromSizeAndRun(0, run);

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
    for (Iterator<IValue> dataSetIterator = DATA_SET_FULL.iterator(); dataSetIterator
        .hasNext(); dataSetCursor++) {
      if (sampledIndices.contains(dataSetCursor)) {
        IValue mapKey = dataSetIterator.next();
        ISet mapValue = (ISet) DATA_SET_FULL.get(mapKey);

        // /**** REMOVE ANNOTATIONS ***/
        // IValueFactory vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();
        //
        // ISetWriter bldr = vf.setWriter();
        // for (IValue untypedTuple : mapValue) {
        // ITuple tuple = (ITuple) untypedTuple;
        //
        // IConstructor keyCleaned = ((IConstructor)
        // tuple.get(0)).asAnnotatable().removeAnnotations();
        // IConstructor valCleaned = ((IConstructor)
        // tuple.get(1)).asAnnotatable().removeAnnotations();
        //
        // ITuple cleanedTuple = vf.tuple(keyCleaned, valCleaned);
        //
        // bldr.insert(cleanedTuple);
        // }
        // mapValue = bldr.done();
        // /***************************/

        sampledGraphs.add(mapValue);
      } else {
        dataSetIterator.next();
      }
    }
  }

  public static long memoryInBytes_multimap = 0;
  public static long memoryInBytes_mapWithNestedSets = 0;

  public static long unique = 0;
  public static long tuples = 0;
  public static long tuples_one2one = 0;

  @Benchmark
  public void timeDominatorCalculation(Blackhole bh) {
    dominatorBenchmark.performBenchmark(bh, sampledGraphsNative);
  }

  @Benchmark
  public void timeDominatorCalculationInstrumented(Blackhole bh) {
    memoryInBytes_multimap = 0;
    memoryInBytes_mapWithNestedSets = 0;

    unique = 0;
    tuples = 0;
    tuples_one2one = 0;

    dominatorBenchmark.performBenchmark(bh, sampledGraphsNative);

    System.out.println("memoryInBytes_multimap:          " + memoryInBytes_multimap);
    System.out.println("memoryInBytes_mapWithNestedSets: " + memoryInBytes_mapWithNestedSets);

    // System.out.println("unique:" + unique);
    // System.out.println("tuples:" + tuples);
    // System.out.println("tuples_one2one:" + tuples_one2one);
    //
    // System.out.println("ratio:" + 1.0 * tuples / unique);

    System.out.println(String.format("csv;unique;tuples;tuples_one2one"));
    System.out.println(String.format("csv;%d;%d;%d", unique, tuples, tuples_one2one));
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
        .include(
            ".*" + JmhCfgDominatorBenchmarks.class.getSimpleName() + ".(timeDominatorCalculation$)")
        .warmupIterations(0).measurementIterations(1).mode(Mode.AverageTime).forks(1)
        .timeUnit(TimeUnit.SECONDS)
        // .param("size", "16")
        // .param("size", "128")
        .param("size", "4096").param("run", "0")
        // .param("dominatorBenchmarkEnum", "CHART")
        // .param("dominatorBenchmarkEnum", "CLOJURE_LAZY")
        // .param("dominatorBenchmarkEnum", "SCALA")
        // .param("dominatorBenchmarkEnum", "VF_CHAMP_MULTIMAP_INSTRUMENTED")
        .param("dominatorBenchmarkEnum", "VF_CHAMP_MULTIMAP_HHAMT_NEW")
        .param("dominatorBenchmarkEnum", "VF_CHAMP_MULTIMAP_HHAMT")
        // .param("dominatorBenchmarkEnum", "VF_CHAMP_MULTIMAP_HHAMT_INTERLINKED")
        // .param("dominatorBenchmarkEnum", "VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED")
        // .param("dominatorBenchmarkEnum", "VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED_INTERLINKED")
        // .param("dominatorBenchmarkEnum", "VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED_INTERLINKED_INSTR")
        // .output("JmhCfgDominatorBenchmarks.log")
        .shouldDoGC(true).build();

    new Runner(opt).run();
  }

}
