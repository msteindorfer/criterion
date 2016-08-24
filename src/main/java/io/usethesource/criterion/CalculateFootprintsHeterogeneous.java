/*******************************************************************************
 * Copyright (c) 2014 CWI All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package io.usethesource.criterion;

import static io.usethesource.criterion.FootprintUtils.createExponentialRange;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.mahout.math.map.OpenIntIntHashMap;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.gs.collections.impl.map.mutable.primitive.IntIntHashMap;

import gnu.trove.map.hash.TIntIntHashMap;
import io.usethesource.capsule.ImmutableMap;
import io.usethesource.capsule.ImmutableSet;
import io.usethesource.capsule.TrieMap_5Bits;
import io.usethesource.capsule.experimental.heterogeneous.TrieMap_5Bits_Heterogeneous_BleedingEdge;
import io.usethesource.capsule.experimental.specialized.TrieSet_5Bits_Spec0To8_IntKey;
import io.usethesource.criterion.BenchmarkUtils.DataType;
import io.usethesource.criterion.BenchmarkUtils.ValueFactoryFactory;
import io.usethesource.criterion.FootprintUtils.Archetype;
import io.usethesource.criterion.api.JmhValue;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import objectexplorer.ObjectGraphMeasurer.Footprint;

public final class CalculateFootprintsHeterogeneous {

  static final String memoryArchitecture;

  static {
    /*
     * http://stackoverflow.com/questions/1518213/read-java-jvm-startup- parameters-eg-xmx
     */
    RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
    List<String> args = bean.getInputArguments();

    if (args.contains("-XX:-UseCompressedOops")) {
      memoryArchitecture = "64bit";
    } else {
      memoryArchitecture = "32bit";
    }
  }

  private static int multimapValueSize = 2;

  private static int stepSizeOneToOneSelector = 2;

  public static void main(String[] args) {
    // testOneConfiguration(2097152);

    String userHome = System.getProperty("user.home");
    String userHomeRelativePath =
        "Research/datastructures-for-metaprogramming/hamt-heterogeneous/data";
    boolean appendToFile = false;

    createExponentialRange(20, 21).stream().flatMap(size -> testOneConfiguration(size, 0).stream())
        .collect(Collectors.toList());

    // // createExponentialRangeWithIntermediatePoints()
    // writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_exponential_"
    // + memoryArchitecture + "_latest.csv"),
    // appendToFile,
    // createExponentialRangeWithIntermediatePoints().stream()
    // .flatMap(size -> createLinearRange(0, 5, 1).stream().flatMap(run ->
    // testOneConfiguration(size, run).stream()))
    // .collect(Collectors.toList()));
    //
    // writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_exponential_"
    // + memoryArchitecture + "_primitive_latest.csv"),
    // appendToFile,
    // createExponentialRangeWithIntermediatePoints().stream()
    // .flatMap(size -> createLinearRange(0, 5, 1).stream().flatMap(run ->
    // testOnePrimitiveConfiguration(size, run).stream()))
    // .collect(Collectors.toList()));

    // writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_exponential_"
    // + memoryArchitecture + "_latest.csv"), appendToFile,
    // createExponentialRange(0, 24).stream()
    // .flatMap(size -> testOneConfiguration(size, 0).stream()).collect(Collectors.toList()));

    // writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_exponential_"
    // + memoryArchitecture + "_latest.csv"), appendToFile,
    // createLinearRange(0, 5, 1).stream().flatMap(run -> FootprintUtils
    // .createExponentialRange(0, 24).stream().flatMap(size -> testOneConfiguration(size,
    // run).stream())).collect(Collectors.toList()));

    // writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_exponential_"
    // + memoryArchitecture + "_primitive_latest.csv"), appendToFile,
    // createLinearRange(0, 1, 1).stream().flatMap(run -> FootprintUtils
    // .createExponentialRange(0, 24).stream().flatMap(size -> testOnePrimitiveConfiguration(size,
    // run).stream())).collect(Collectors.toList()));

    // writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_tiny.csv"),
    // appendToFile,
    // createLinearRange(0, 101, 1).stream()
    // .flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));
    //
    // writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_small.csv"),
    // appendToFile,
    // createLinearRange(0, 10_100, 100).stream()
    // .flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));
    //
    // writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_medium.csv"),
    // appendToFile,
    // createLinearRange(10_000, 101_000, 1_000).stream()
    // .flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));
    //
    // writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_large.csv"),
    // appendToFile,
    // createLinearRange(100_000, 8_100_000, 100_000).stream()
    // .flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));
  }

  // public static void testPrintStatsRandomSmallAndBigIntegers() {
  // int measurements = 4;
  //
  // for (int exp = 0; exp <= 23; exp += 1) {
  // final int thisExpSize = (int) Math.pow(2, exp);
  // final int prevExpSize = (int) Math.pow(2, exp-1);
  //
  // int stride = (thisExpSize - prevExpSize) / measurements;
  //
  // if (stride == 0) {
  // measurements = 1;
  // }
  //
  // for (int m = measurements - 1; m >= 0; m--) {
  // int size = thisExpSize - m * stride;
  // }

  public static List<Integer> createExponentialRangeWithIntermediatePoints() {
    List<Integer> tmpExponentialRange1 = createExponentialRange(0, 24);
    List<Integer> tmpExponentialRange2 = createExponentialRange(-1, 23);

    List<Integer> tmpExponentialRange = new ArrayList<>(2 * tmpExponentialRange1.size());
    for (int i = 0; i < tmpExponentialRange1.size(); i++) {
      tmpExponentialRange.add(tmpExponentialRange1.get(i));
      tmpExponentialRange.add(tmpExponentialRange1.get(i) + tmpExponentialRange2.get(i));
    }

    List<Integer> exponentialRange = tmpExponentialRange.stream().skip(1)
        .limit(2 * tmpExponentialRange1.size() - 2).collect(Collectors.toList());

    return exponentialRange;
  }

  public static List<String> testOneConfiguration(int size, int run) {
    /*
     * // int size = 32; double percentageOfPrimitives = 1.00;
     * 
     * Object[] data = new Object[size];
     * 
     * int countForPrimitives = (int) ((percentageOfPrimitives) * size); int smallCount = 0; int
     * bigCount = 0;
     * 
     * Random rand = new Random(13); for (int i = 0; i < size; i++) { final int j = rand.nextInt();
     * final BigInteger bigJ = BigInteger.valueOf(j).multiply(BigInteger.valueOf(j));
     * 
     * if (i < countForPrimitives) { // System.out.println("SMALL"); smallCount++; data[i] = j; }
     * else { // System.out.println("BIG"); bigCount++; data[i] = bigJ; } }
     * 
     * System.out.println(); System.out.println(String.format("PRIMITIVE:   %10d (%.2f percent)",
     * smallCount, 100. * smallCount / (smallCount + bigCount))); System.out.println(
     * String.format("BIG_INTEGER: %10d (%.2f percent)", bigCount, 100. * bigCount / (smallCount +
     * bigCount))); // System.out.println(String.format("UNIQUE: %10d (%.2f percent)", //
     * map.size(), 100. * map.size() / (smallCount + bigCount))); System.out.println();
     * 
     */

    EnumSet<MemoryFootprintPreset> presets =
        EnumSet.of(MemoryFootprintPreset.DATA_STRUCTURE_OVERHEAD
        // ,
        // MemoryFootprintPreset.RETAINED_SIZE
        );

    // for (MemoryFootprintPreset preset : presets) {
    //// createAndMeasureTrieMapHomogeneous(data, size, 0, preset, true);
    //// createAndMeasureTrieMapHomogeneous(data, size, 0, preset, false);
    // createAndMeasureJavaUtilHashMap(data, size, 0, preset);
    // createAndMeasureTrieMapHeterogeneous(data, size, 0, preset, true);
    // createAndMeasureTrieMapHeterogeneous(data, size, 0, preset, false);
    // createAndMeasureTrove4jIntArrayList(data, size, 0, preset);
    // System.out.println();
    // }

    return presets.stream()
        .flatMap(preset -> Arrays.stream(new String[] {
            createAndMeasurePersistentMap(ValueFactoryFactory.VF_CHAMP, size, run, preset),

            /* Map<K, V> 3rd party libraries containing persistent data structures */
            createAndMeasurePersistentMap(ValueFactoryFactory.VF_UNCLEJIM, size, run, preset),
            createAndMeasurePersistentMap(ValueFactoryFactory.VF_DEXX, size, run, preset),
            createAndMeasurePersistentMap(ValueFactoryFactory.VF_JAVASLANG, size, run, preset),
            createAndMeasurePersistentMap(ValueFactoryFactory.VF_PCOLLECTIONS, size, run, preset),

        // /* Map<K, V> vs Multimap<K, V> */
        // createAndMeasureTrieMap(ValueFactoryFactory.VF_CHAMP_MAP_AS_MULTIMAP, size, run, preset),
        // createAndMeasureTrieMap(ValueFactoryFactory.VF_CHAMP_MULTIMAP_HCHAMP, size, run, preset),
        // createAndMeasureTrieMap(ValueFactoryFactory.VF_CHAMP_MULTIMAP_HHAMT, size, run, preset),
        // createAndMeasureTrieMap(ValueFactoryFactory.VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED, size,
        // run, preset),
        //
        // /* Multimap<K, V> */
        // createAndMeasureTrieSetMultimap(ValueFactoryFactory.VF_CHAMP_MULTIMAP_HHAMT, size,
        // multimapValueSize, stepSizeOneToOneSelector, run, preset),
        // // TODO: investigate why it's failing
        // //
        // createAndMeasureTrieSetMultimap(ValueFactoryFactory.VF_CHAMP_MULTIMAP_HHAMT_INTERLINKED,
        // size, multimapValueSize, stepSizeOneToOneSelector, run, preset),
        // createAndMeasureTrieSetMultimap(ValueFactoryFactory.VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED,
        // size, multimapValueSize, stepSizeOneToOneSelector, run, preset),
        // createAndMeasureTrieSetMultimap(ValueFactoryFactory.VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED_INTERLINKED,
        // size, multimapValueSize, stepSizeOneToOneSelector, run, preset),
        // createAndMeasureTrieSetMultimap(ValueFactoryFactory.VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED_PATH_INTERLINKED,
        // size, multimapValueSize, stepSizeOneToOneSelector, run, preset),
        // createAndMeasureTrieSetMultimap(ValueFactoryFactory.VF_SCALA, size, multimapValueSize,
        // stepSizeOneToOneSelector, run, preset),
        // createAndMeasureTrieSetMultimap(ValueFactoryFactory.VF_CLOJURE, size, multimapValueSize,
        // stepSizeOneToOneSelector, run, preset),

        // /* Map[int, int] */
        // createAndMeasureTrieMapHeterogeneous(data, size, run, preset, false),
        //
        // createAndMeasureFastUtilInt2IntOpenHashMap(data, size, run, preset),
        // createAndMeasureMahoutMutableIntIntHashMap(data, size, run, preset),
        // createAndMeasureTrove4jTIntIntHashMap(data, size, run, preset),
        // createAndMeasureGsImmutableIntIntMap(data, size, run, preset),

        /* SetMultimap */
        // , createAndMeasureGsImmutableSetMultimap(data, size, 0, preset)
        // , createAndMeasureGuavaImmutableSetMultimap(data, size, 0, preset)

        // createAndMeasureJavaUtilHashMap(data, size, 0, preset)
        // , createAndMeasureTrieMapHomogeneous(data, size, 0, preset)
        // , createAndMeasureTrieMapHeterogeneous(data, size, 0, preset, true)
        // , createAndMeasureTrieMapHeterogeneous(data, size, 0, preset, false)
        // , createAndMeasureTrove4jTIntIntHashMap(data, size, 0, preset)
        })).collect(Collectors.toList());
  }

  public static List<String> testOnePrimitiveConfiguration(int size, int run) {
    // int size = 32;
    double percentageOfPrimitives = 1.00;

    Object[] data = new Object[size];

    int countForPrimitives = (int) ((percentageOfPrimitives) * size);
    int smallCount = 0;
    int bigCount = 0;

    Random rand = new Random(13);
    for (int i = 0; i < size; i++) {
      final int j = rand.nextInt();
      final BigInteger bigJ = BigInteger.valueOf(j).multiply(BigInteger.valueOf(j));

      if (i < countForPrimitives) {
        // System.out.println("SMALL");
        smallCount++;
        data[i] = j;
      } else {
        // System.out.println("BIG");
        bigCount++;
        data[i] = bigJ;
      }
    }

    System.out.println();
    System.out.println(String.format("PRIMITIVE:   %10d (%.2f percent)", smallCount,
        100. * smallCount / (smallCount + bigCount)));
    System.out.println(String.format("BIG_INTEGER: %10d (%.2f percent)", bigCount,
        100. * bigCount / (smallCount + bigCount)));
    // System.out.println(String.format("UNIQUE: %10d (%.2f percent)",
    // map.size(), 100. * map.size() / (smallCount + bigCount)));
    System.out.println();

    // for (MemoryFootprintPreset preset : presets) {
    //// createAndMeasureTrieMapHomogeneous(data, size, 0, preset, true);
    //// createAndMeasureTrieMapHomogeneous(data, size, 0, preset, false);
    // createAndMeasureJavaUtilHashMap(data, size, 0, preset);
    // createAndMeasureTrieMapHeterogeneous(data, size, 0, preset, true);
    // createAndMeasureTrieMapHeterogeneous(data, size, 0, preset, false);
    // createAndMeasureTrove4jIntArrayList(data, size, 0, preset);
    // System.out.println();
    // }

    final MemoryFootprintPreset preset =
        MemoryFootprintPreset.RETAINED_SIZE_WITH_BOXED_INTEGER_FILTER;
    return Arrays.stream(new String[] {
        /* Map[int, int] */
        createAndMeasureGuavaImmutableMap(data, size, run, preset), // Reference
        createAndMeasureTrieMapHeterogeneous_asMap(data, size, run, preset),
        /* ******************* */
        createAndMeasureTrove4jTIntIntHashMap(data, size, 0, preset),
        createAndMeasureMahoutMutableIntIntHashMap(data, size, 0, preset),
        createAndMeasureFastUtilInt2IntOpenHashMap(data, size, 0, preset),
        createAndMeasureGsImmutableIntIntMap(data, size, 0, preset),
        /* ******************* */

        /* Multimap[int, int] */
        // createAndMeasureGuavaImmutableSetMultimap(data, size, run, preset), // Reference
        // createAndMeasureTrieMapHeterogeneous_asMultimap(data, size, run, preset),
        /* ******************* */
        /** NOTHING AVAILABLE **/
        /* ******************* */

        // /* SetMultimap */
        // createAndMeasureGsImmutableSetMultimap(data, size, 0, preset),
        // createAndMeasureGuavaImmutableSetMultimap(data, size, 0, preset)

        // createAndMeasureJavaUtilHashMap(data, size, 0, preset)
        // , createAndMeasureTrieMapHomogeneous(data, size, 0, preset)
        // , createAndMeasureTrieMapHeterogeneous(data, size, 0, preset, true)
        // , createAndMeasureTrieMapHeterogeneous(data, size, 0, preset, false)
        // , createAndMeasureTrove4jTIntIntHashMap(data, size, 0, preset)
    }).collect(Collectors.toList());
  }

  // public static String createAndMeasureMultiChamp(final Object[] data, int elementCount, int run,
  // MemoryFootprintPreset preset) {
  // ImmutableSetMultimap<Integer, Integer> ys = TrieSetMultimap_BleedingEdge.of();
  //
  // for (Object o : data) {
  // for (int i = 0; i < multimapValueCount; i++) {
  // ys = ys.__put((Integer) o, (Integer) i);
  // }
  // }
  //
  // return measureAndReport(ys, "io.usethesource.capsule.TrieSetMultimap_BleedingEdge",
  // DataType.MULTIMAP,
  // Archetype.PERSISTENT, false, elementCount, run, preset);
  // }

  public static String createAndMeasureGsImmutableSetMultimap(final Object[] data, int elementCount,
      int run, MemoryFootprintPreset preset) {
    com.gs.collections.api.multimap.set.MutableSetMultimap<Integer, Integer> mutableYs =
        com.gs.collections.impl.factory.Multimaps.mutable.set.with();

    for (Object o : data) {
      for (int i = 0; i < multimapValueSize; i++) {
        mutableYs.put((Integer) o, (Integer) i);
      }
    }

    /* Note: direct creation of immutable that uses newWith(...) is tremendously slow. */
    com.gs.collections.api.multimap.set.ImmutableSetMultimap<Integer, Integer> ys =
        mutableYs.toImmutable();

    return measureAndReport(ys, "com.gs.collections.api.multimap.set.ImmutableSetMultimap",
        DataType.SET_MULTIMAP, Archetype.IMMUTABLE, false, elementCount, run, preset);
  }

  public static String createAndMeasureFastUtilInt2IntOpenHashMap(final Object[] data,
      int elementCount, int run, MemoryFootprintPreset preset) {
    it.unimi.dsi.fastutil.ints.AbstractInt2IntMap mutableYs = new Int2IntOpenHashMap();

    for (Object o : data) {
      for (int i = 0; i < multimapValueSize; i++) {
        mutableYs.put((Integer) o, (Integer) i);
      }
    }

    return measureAndReport(mutableYs, "it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap",
        DataType.MAP, Archetype.MUTABLE, false, elementCount, run, preset);
  }

  public static String createAndMeasureMahoutMutableIntIntHashMap(final Object[] data,
      int elementCount, int run, MemoryFootprintPreset preset) {
    org.apache.mahout.math.map.AbstractIntIntMap mutableYs = new OpenIntIntHashMap();

    for (Object o : data) {
      for (int i = 0; i < multimapValueSize; i++) {
        mutableYs.put((Integer) o, (Integer) i);
      }
    }

    return measureAndReport(mutableYs, "org.apache.mahout.math.map.OpenIntIntHashMap", DataType.MAP,
        Archetype.MUTABLE, false, elementCount, run, preset);
  }

  public static String createAndMeasureGsImmutableIntIntMap(final Object[] data, int elementCount,
      int run, MemoryFootprintPreset preset) {
    com.gs.collections.api.map.primitive.MutableIntIntMap mutableYs = new IntIntHashMap();

    for (Object o : data) {
      for (int i = 0; i < multimapValueSize; i++) {
        mutableYs.put((Integer) o, (Integer) i);
      }
    }

    com.gs.collections.api.map.primitive.ImmutableIntIntMap ys = mutableYs.toImmutable();

    return measureAndReport(ys, "com.gs.collections.api.map.primitive.ImmutableIntIntMap",
        DataType.MAP, Archetype.IMMUTABLE, false, elementCount, run, preset);
  }

  private final static JmhValue box(int i) {
    return new PureIntegerWithCustomHashCode(i);
  }

  public static String createAndMeasureGuavaImmutableMap(final Object[] data, int elementCount,
      int run, MemoryFootprintPreset preset) {
    com.google.common.collect.ImmutableMap.Builder<JmhValue, JmhValue> ysBldr =
        com.google.common.collect.ImmutableMap.builder();

    // filters duplicates (because builder can't handle them)
    Set<Object> seenKeys = new HashSet<>(data.length);

    for (Object o : data) {
      if (!seenKeys.contains(o)) {
        seenKeys.add(o);
        ysBldr.put(box((Integer) o), box((Integer) o));
      }
    }

    com.google.common.collect.ImmutableMap<JmhValue, JmhValue> ys = ysBldr.build();

    return measureAndReport(ys, "com.google.common.collect.ImmutableMap", DataType.MAP,
        Archetype.IMMUTABLE, false, elementCount, run, preset);
  }

  public static String createAndMeasureGuavaImmutableSetMultimap(final Object[] data,
      int elementCount, int run, MemoryFootprintPreset preset) {
    com.google.common.collect.ImmutableSetMultimap.Builder<JmhValue, JmhValue> ysBldr =
        com.google.common.collect.ImmutableSetMultimap.builder();

    for (int keyIdx = 0; keyIdx < data.length; keyIdx++) {
      Object o = data[keyIdx];

      if (keyIdx % stepSizeOneToOneSelector == 0) {
        ysBldr.put(box((Integer) o), box((Integer) o));
      } else {
        for (int i = 0; i < multimapValueSize; i++) {
          ysBldr.put(box((Integer) o), box((Integer) i));
        }
      }
    }

    com.google.common.collect.ImmutableMultimap<JmhValue, JmhValue> ys = ysBldr.build();

    return measureAndReport(ys, "com.google.common.collect.ImmutableSetMultimap",
        DataType.SET_MULTIMAP, Archetype.IMMUTABLE, false, elementCount, run, preset);
  }

  public static String createAndMeasureTrieMapHeterogeneous_asMap(final Object[] data,
      int elementCount, int run, MemoryFootprintPreset preset) {
    TrieMap_5Bits_Heterogeneous_BleedingEdge ys =
        (TrieMap_5Bits_Heterogeneous_BleedingEdge) TrieMap_5Bits_Heterogeneous_BleedingEdge.of();

    // simulating a multimap with a specialized nested integer set
    for (int keyIdx = 0; keyIdx < data.length; keyIdx++) {
      Object o = data[keyIdx];
      int intValue = ((Integer) o).intValue();

      ys = (TrieMap_5Bits_Heterogeneous_BleedingEdge) ys.__put(intValue, intValue);
    }

    final String shortName = "HHAMT_AS_MAP<int, int>";

    return measureAndReport(ys, shortName, DataType.MAP, Archetype.PERSISTENT, false, elementCount,
        run, preset);
  }

  public static String createAndMeasureTrieMapHeterogeneous_asMultimap(final Object[] data,
      int elementCount, int run, MemoryFootprintPreset preset) {
    TrieMap_5Bits_Heterogeneous_BleedingEdge ys =
        (TrieMap_5Bits_Heterogeneous_BleedingEdge) TrieMap_5Bits_Heterogeneous_BleedingEdge.of();

    // simulating a multimap with a specialized nested integer set
    for (int keyIdx = 0; keyIdx < data.length; keyIdx++) {
      Object o = data[keyIdx];
      int intValue = ((Integer) o).intValue();

      if (keyIdx % stepSizeOneToOneSelector == 0) {
        ys = (TrieMap_5Bits_Heterogeneous_BleedingEdge) ys.__put(intValue, intValue);
      } else {
        ImmutableSet<Integer> nestedSet = TrieSet_5Bits_Spec0To8_IntKey.of();

        for (int i = 0; i < multimapValueSize; i++) {
          nestedSet = nestedSet.__insert(i);
        }

        ys = (TrieMap_5Bits_Heterogeneous_BleedingEdge) ys.__put(o, nestedSet);
      }
    }

    final String shortName = "HHAMT_AS_SET_MULTIMAP<int, int>";

    return measureAndReport(ys, shortName, DataType.SET_MULTIMAP, Archetype.PERSISTENT, false,
        elementCount, run, preset);
  }

  public static String createAndMeasurePersistentMap(ValueFactoryFactory valueFactoryFactory,
      int elementCount, int run, MemoryFootprintPreset preset) {
    try {
      final Object mapInstance = JmhMapBenchmarks.generateMap(valueFactoryFactory.getInstance(),
          ElementProducer.PDB_INTEGER, false, elementCount, run);

      return measureAndReport(mapInstance, valueFactoryFactory.name(), DataType.MAP,
          Archetype.PERSISTENT, false, elementCount, run, preset);
    } catch (Exception e) {
      e.printStackTrace();
      return "ERROR";
    }
  }

  /*
   * TODO: check where this is used; misnomer.
   */
  @Deprecated
  public static String createAndMeasureTrieMap(ValueFactoryFactory valueFactoryFactory,
      int elementCount, int run, MemoryFootprintPreset preset) {
    try {
      final int fixedMultimapValueSize = 1;
      final int fixedStepSizeOneToOneSelector = 1;

      final Object setMultimapInstance = JmhSetMultimapBenchmarks.generateSetMultimap(
          valueFactoryFactory.getInstance(), ElementProducer.PDB_INTEGER, false, elementCount,
          fixedMultimapValueSize, fixedStepSizeOneToOneSelector, run);

      return measureAndReport(setMultimapInstance, valueFactoryFactory.name(), DataType.MAP,
          Archetype.PERSISTENT, false, elementCount, run, preset);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return "ERROR";
  }

  public static String createAndMeasureTrieSetMultimap(ValueFactoryFactory valueFactoryFactory,
      int elementCount, int multimapValueSize, int stepSizeOneToOneSelector, int run,
      MemoryFootprintPreset preset) {
    try {
      final Object setMultimapInstance = JmhSetMultimapBenchmarks.generateSetMultimap(
          valueFactoryFactory.getInstance(), ElementProducer.PDB_INTEGER, false, elementCount,
          multimapValueSize, stepSizeOneToOneSelector, run);

      return measureAndReport(setMultimapInstance, valueFactoryFactory.name(),
          DataType.SET_MULTIMAP, Archetype.PERSISTENT, false, elementCount, run, preset);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return "ERROR";
  }

  public static String createAndMeasureTrieMapHomogeneous(final Object[] data, int elementCount,
      int run, MemoryFootprintPreset preset) {
    ImmutableMap<Integer, Integer> ys = TrieMap_5Bits.of();

    // for (Object v : data) {
    // ys = ys.__put(v, v);
    // assert ys.containsKey(v);
    // }

    int[] convertedData = new int[elementCount];

    for (int i = 0; i < elementCount; i++) {
      final Object v = data[i];
      final int convertedValue;

      if (v instanceof Integer) {
        convertedValue = (Integer) v;
      } else if (v instanceof BigInteger) {
        convertedValue = ((BigInteger) v).intValue();
      } else {
        throw new IllegalStateException("Expecting input data of type Integer or BigInteger.");
      }

      convertedData[i] = convertedValue;
    }

    for (int value : convertedData) {
      ys = ys.__put(value, value);
      assert ys.containsKey(value);
    }

    String shortName = "TrieMap [Boxed]";

    // String longName = String.format(
    // "io.usethesource.capsule.TrieMap_5Bits_Spec0To8", isSpecialized);

    return measureAndReport(ys, shortName, DataType.MAP, Archetype.PERSISTENT, false, elementCount,
        run, preset);
  }

  public static String createAndMeasureTrieMapHeterogeneous(final Object[] data, int elementCount,
      int run, MemoryFootprintPreset preset, boolean storePrimivesBoxed) {
    TrieMap_5Bits_Heterogeneous_BleedingEdge ys =
        (TrieMap_5Bits_Heterogeneous_BleedingEdge) TrieMap_5Bits_Heterogeneous_BleedingEdge.of();

    for (Object v : data) {
      if (v instanceof Integer && storePrimivesBoxed) {
        // PureInteger boxedValue = new PureInteger(((Integer) v).intValue());
        Integer boxedValue = (Integer) v;

        ys = (TrieMap_5Bits_Heterogeneous_BleedingEdge) ys.__put(boxedValue, boxedValue);
        assert ys.containsKey(boxedValue);
      } else if (v instanceof Integer && !storePrimivesBoxed) {
        int unboxedValue = ((Integer) v).intValue();

        ys = (TrieMap_5Bits_Heterogeneous_BleedingEdge) ys.__put(unboxedValue, unboxedValue);
        assert ys.containsKey(unboxedValue);
        // } else {
        // ys = (TrieMap_5Bits_Heterogeneous_BleedingEdge) ys.__put(v, v);
        // assert ys.containsKey(v);
      }
    }

    final String shortName = storePrimivesBoxed ? "HTrieMap [Boxed]" : "HTrieMap [Primitive]";

    // String shortName = String.format("TrieMap[%13s, storePrimivesBoxed =
    // %5s]",
    // "heterogeneous", storePrimivesBoxed);
    //
    // String longName = String.format(
    // "io.usethesource.capsule.TrieMap_Heterogeneous[storePrimivesBoxed =
    // %5s]",
    // storePrimivesBoxed);

    return measureAndReport(ys, shortName, DataType.MAP, Archetype.PERSISTENT, false, elementCount,
        run, preset);
  }

  public static String createAndMeasureJavaUtilHashMap(final Object[] data, int elementCount,
      int run, MemoryFootprintPreset preset) {
    Map<Object, Object> ys = new HashMap<>();

    for (Object v : data) {
      ys.put(v, v);
      assert ys.containsKey(v);
    }

    String shortName = String.format("HashMap");

    String longName = String.format("java.util.HashMap");

    return measureAndReport(ys, shortName, DataType.MAP, Archetype.MUTABLE, false, elementCount,
        run, preset);
  }

  public static String createAndMeasureTrove4jTIntIntHashMap(final Object[] data, int elementCount,
      int run, MemoryFootprintPreset preset) {
    TIntIntHashMap ys = new TIntIntHashMap(elementCount);

    int[] convertedData = new int[elementCount];

    for (int i = 0; i < elementCount; i++) {
      final Object v = data[i];
      final int convertedValue;

      if (v instanceof Integer) {
        convertedValue = (Integer) v;
      } else if (v instanceof BigInteger) {
        convertedValue = ((BigInteger) v).intValue();
      } else {
        throw new IllegalStateException("Expecting input data of type Integer or BigInteger.");
      }

      convertedData[i] = convertedValue;
    }

    for (int value : convertedData) {
      ys.put(value, value);
      assert ys.containsKey(value);
    }

    return measureAndReport(ys, "gnu.trove.map.hash.TIntIntHashMap", DataType.MAP,
        Archetype.MUTABLE, false, elementCount, run, preset);
  }

  enum MemoryFootprintPreset {
    RETAINED_SIZE, DATA_STRUCTURE_OVERHEAD, RETAINED_SIZE_WITH_BOXED_INTEGER_FILTER
  }

  @SuppressWarnings("unchecked")
  private static String measureAndReport(final Object objectToMeasure, final String className,
      DataType dataType, Archetype archetype, boolean supportsStagedMutability, int size, int run,
      MemoryFootprintPreset preset) {
    final Predicate<Object> predicate;

    switch (preset) {
      case DATA_STRUCTURE_OVERHEAD:
        // TODO: create JmhLeaf
        // predicate = Predicates
        // .not(Predicates.or(Predicates.instanceOf(Integer.class),
        // Predicates.instanceOf(BigInteger.class),
        // Predicates.instanceOf(JmhValue.class), Predicates.instanceOf(PureInteger.class)));
        predicate = Predicates.not(Predicates.or(Predicates.instanceOf(PureInteger.class),
            Predicates.instanceOf(PureIntegerWithCustomHashCode.class)));
        break;
      case RETAINED_SIZE:
        predicate = Predicates.alwaysTrue();
        break;
      case RETAINED_SIZE_WITH_BOXED_INTEGER_FILTER:
        predicate = Predicates.not(Predicates.instanceOf(Integer.class));
        break;
      default:
        throw new IllegalStateException();
    }

    return measureAndReport(objectToMeasure, className, dataType, archetype,
        supportsStagedMutability, size, run, predicate);
  }

  private static String measureAndReport(final Object objectToMeasure, final String className,
      DataType dataType, Archetype archetype, boolean supportsStagedMutability, int size, int run) {
    return measureAndReport(objectToMeasure, className, dataType, archetype,
        supportsStagedMutability, size, run, MemoryFootprintPreset.DATA_STRUCTURE_OVERHEAD);
  }

  private static String measureAndReport(final Object objectToMeasure, final String className,
      DataType dataType, Archetype archetype, boolean supportsStagedMutability, int size, int run,
      Predicate<Object> predicate) {
    // System.out.println(GraphLayout.parseInstance(objectToMeasure).totalSize());

    long memoryInBytes = objectexplorer.MemoryMeasurer.measureBytes(objectToMeasure, predicate);
    Footprint memoryFootprint =
        objectexplorer.ObjectGraphMeasurer.measure(objectToMeasure, predicate);

    final String statString = String.format("%d (%d@%d)\t %60s\t\t %s", size, run, memoryInBytes,
        className, memoryFootprint);
    System.out.println(statString);

    // final String statLatexString = String.format("%s & %s & %s & %b & %d
    // & %d & %d & \"%s\" \\\\", className, dataType, archetype,
    // supportsStagedMutability, memoryInBytes,
    // memoryFootprint.getObjects(), memoryFootprint.getReferences(),
    // memoryFootprint.getPrimitives());
    // System.out.println(statLatexString);

    final String statFileString = String.format("%d,%d,%s,%s,%s,%b,%d,%d,%d", size, run, className,
        dataType, archetype, supportsStagedMutability, memoryInBytes, memoryFootprint.getObjects(),
        memoryFootprint.getReferences());

    return statFileString;
    // writeToFile(statFileString);
  }

}
