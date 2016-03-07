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
package io.usethesource.criterion;

import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.mahout.math.map.OpenIntIntHashMap;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.gs.collections.impl.map.mutable.primitive.IntIntHashMap;

import gnu.trove.map.hash.TIntIntHashMap;
import io.usethesource.capsule.ImmutableMap;
import io.usethesource.capsule.TrieMap_5Bits;
import io.usethesource.capsule.TrieMap_5Bits_Heterogeneous_BleedingEdge;
import io.usethesource.criterion.BenchmarkUtils.DataType;
import io.usethesource.criterion.FootprintUtils.Archetype;
import io.usethesource.criterion.api.JmhValue;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import objectexplorer.ObjectGraphMeasurer.Footprint;

public final class CalculateFootprintsHeterogeneous {

	private static int multimapValueCount = 1;
	
	public static void main(String[] args) {
		testOneConfiguration(2097152);
		
		String userHome = System.getProperty("user.home");
		String userHomeRelativePath = "Research/datastructures-for-metaprogramming/hamt-heterogeneous/data";
		boolean appendToFile = true;
		
		FootprintUtils.writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_exponential.csv"), appendToFile,
				FootprintUtils.createExponentialRange(0, 24).stream()
						.flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));

//		FootprintUtils.writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_tiny.csv"), appendToFile,
//				FootprintUtils.createLinearRange(0, 101, 1).stream()
//						.flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));
//		
//		FootprintUtils.writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_small.csv"), appendToFile,
//				FootprintUtils.createLinearRange(0, 10_100, 100).stream()
//						.flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));
//
//		FootprintUtils.writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_medium.csv"), appendToFile,
//				FootprintUtils.createLinearRange(10_000, 101_000, 1_000).stream()
//						.flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));
//
//		FootprintUtils.writeToFile(Paths.get(userHome, userHomeRelativePath, "map_sizes_heterogeneous_large.csv"), appendToFile,
//				FootprintUtils.createLinearRange(100_000, 8_100_000, 100_000).stream()
//						.flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));
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

	public static List<String> testOneConfiguration(int size) {
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
		System.out.println(
				String.format("BIG_INTEGER: %10d (%.2f percent)", bigCount, 100. * bigCount / (smallCount + bigCount)));
		// System.out.println(String.format("UNIQUE: %10d (%.2f percent)",
		// map.size(), 100. * map.size() / (smallCount + bigCount)));
		System.out.println();

		EnumSet<MemoryFootprintPreset> presets = EnumSet.of(
				// MemoryFootprintPreset.DATA_STRUCTURE_OVERHEAD
				// ,
				MemoryFootprintPreset.RETAINED_SIZE);

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
						/* Map[int, int] */
						createAndMeasureFastUtilInt2IntOpenHashMap(data, size, 0, preset)
//						, createAndMeasureMahoutMutableIntIntHashMap(data, size, 0, preset)
//						, createAndMeasureGsImmutableIntIntMap(data, size, 0, preset)
						
						/* SetMultimap */
//						, createAndMeasureGsImmutableSetMultimap(data, size, 0, preset)
//						, createAndMeasureGuavaImmutableSetMultimap(data, size, 0, preset)
						
//						createAndMeasureJavaUtilHashMap(data, size, 0, preset)
//						, createAndMeasureTrieMapHomogeneous(data, size, 0, preset)
//						, createAndMeasureTrieMapHeterogeneous(data, size, 0, preset, true)
//						, createAndMeasureTrieMapHeterogeneous(data, size, 0, preset, false)
//						, createAndMeasureTrove4jTIntIntHashMap(data, size, 0, preset)
				})).collect(Collectors.toList());
	}

//	public static String createAndMeasureMultiChamp(final Object[] data, int elementCount, int run,
//			MemoryFootprintPreset preset) {
//		ImmutableSetMultimap<Integer, Integer> ys = TrieSetMultimap_BleedingEdge.of();
//
//		for (Object o : data) {
//			for (int i = 0; i < multimapValueCount; i++) {
//				ys = ys.__put((Integer) o, (Integer) i);
//			}
//		}
//
//		return measureAndReport(ys, "io.usethesource.capsule.TrieSetMultimap_BleedingEdge", DataType.MULTIMAP,
//				Archetype.PERSISTENT, false, elementCount, run, preset);
//	}
	
	public static String createAndMeasureGsImmutableSetMultimap(final Object[] data, int elementCount, int run,
			MemoryFootprintPreset preset) {
		com.gs.collections.api.multimap.set.MutableSetMultimap<Integer, Integer> mutableYs = com.gs.collections.impl.factory.Multimaps.mutable.set.with();
		
		for (Object o : data) {
			for(int i = 0; i < multimapValueCount; i++) {				
				mutableYs.put((Integer) o, (Integer) i);
			}			
		}
		
		/* Note: direct creation of immutable that uses newWith(...) is tremendously slow. */		
		com.gs.collections.api.multimap.set.ImmutableSetMultimap<Integer, Integer> ys = mutableYs.toImmutable();
					
		return measureAndReport(ys, "com.gs.collections.api.multimap.set.ImmutableSetMultimap", DataType.MULTIMAP, Archetype.IMMUTABLE, false, elementCount, run, preset);
	}		

	public static String createAndMeasureFastUtilInt2IntOpenHashMap(final Object[] data, int elementCount, int run,
			MemoryFootprintPreset preset) {
		it.unimi.dsi.fastutil.ints.AbstractInt2IntMap mutableYs = new Int2IntOpenHashMap();
		
		for (Object o : data) {
			for(int i = 0; i < multimapValueCount; i++) {				
				mutableYs.put((Integer) o, (Integer) i);
			}			
		}
							
		return measureAndReport(mutableYs, "it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap", DataType.MAP, Archetype.MUTABLE, false, elementCount, run, preset);
	}
	
	public static String createAndMeasureMahoutMutableIntIntHashMap(final Object[] data, int elementCount, int run,
			MemoryFootprintPreset preset) {
		org.apache.mahout.math.map.AbstractIntIntMap mutableYs = new OpenIntIntHashMap();
		
		for (Object o : data) {
			for(int i = 0; i < multimapValueCount; i++) {				
				mutableYs.put((Integer) o, (Integer) i);
			}			
		}
							
		return measureAndReport(mutableYs, "org.apache.mahout.math.map.OpenIntIntHashMap", DataType.MAP, Archetype.MUTABLE, false, elementCount, run, preset);
	}
	
	public static String createAndMeasureGsImmutableIntIntMap(final Object[] data, int elementCount, int run,
			MemoryFootprintPreset preset) {
		com.gs.collections.api.map.primitive.MutableIntIntMap mutableYs = new IntIntHashMap();
		
		for (Object o : data) {
			for(int i = 0; i < multimapValueCount; i++) {				
				mutableYs.put((Integer) o, (Integer) i);
			}			
		}
		
		com.gs.collections.api.map.primitive.ImmutableIntIntMap ys = mutableYs.toImmutable();
					
		return measureAndReport(ys, "com.gs.collections.api.map.primitive.ImmutableIntIntMap", DataType.MAP, Archetype.IMMUTABLE, false, elementCount, run, preset);
	}	
	
	public static String createAndMeasureGuavaImmutableSetMultimap(final Object[] data, int elementCount, int run,
			MemoryFootprintPreset preset) {
		com.google.common.collect.ImmutableSetMultimap.Builder<Integer, Integer> ysBldr = com.google.common.collect.ImmutableSetMultimap.builder();
		
		for (Object o : data) {
			for (int i = 0; i < multimapValueCount; i++) { 
				ysBldr.put((Integer) o, (Integer) i);
			}
		}
		
		com.google.common.collect.ImmutableMultimap<Integer, Integer> ys = ysBldr.build();
		
		return measureAndReport(ys, "com.google.common.collect.ImmutableSetMultimap", DataType.MULTIMAP, Archetype.IMMUTABLE, false, elementCount, run, preset);
	}
	
	public static String createAndMeasureTrieMapHomogeneous(final Object[] data, int elementCount, int run,
			MemoryFootprintPreset preset) {
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

		return measureAndReport(ys, shortName, DataType.MAP, Archetype.PERSISTENT, false, elementCount, run, preset);
	}

	public static String createAndMeasureTrieMapHeterogeneous(final Object[] data, int elementCount, int run,
			MemoryFootprintPreset preset, boolean storePrimivesBoxed) {
		TrieMap_5Bits_Heterogeneous_BleedingEdge ys = (TrieMap_5Bits_Heterogeneous_BleedingEdge) TrieMap_5Bits_Heterogeneous_BleedingEdge.of();

		for (Object v : data) {
			if (v instanceof Integer && storePrimivesBoxed) {
//				PureInteger boxedValue = new PureInteger(((Integer) v).intValue());
				Integer boxedValue = (Integer) v;

				ys = (TrieMap_5Bits_Heterogeneous_BleedingEdge) ys.__put(boxedValue, boxedValue);
				assert ys.containsKey(boxedValue);
			} else if (v instanceof Integer && !storePrimivesBoxed) {
				int unboxedValue = ((Integer) v).intValue();
				
				ys = (TrieMap_5Bits_Heterogeneous_BleedingEdge) ys.__put(unboxedValue, unboxedValue);
				assert ys.containsKey(unboxedValue);
//			} else {
//				ys = (TrieMap_5Bits_Heterogeneous_BleedingEdge) ys.__put(v, v);
//				assert ys.containsKey(v);				
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

		return measureAndReport(ys, shortName, DataType.MAP, Archetype.PERSISTENT, false, elementCount, run, preset);
	}

	public static String createAndMeasureJavaUtilHashMap(final Object[] data, int elementCount, int run,
			MemoryFootprintPreset preset) {
		Map<Object, Object> ys = new HashMap<>();

		for (Object v : data) {
			ys.put(v, v);
			assert ys.containsKey(v);
		}

		String shortName = String.format("HashMap");

		String longName = String.format("java.util.HashMap");

		return measureAndReport(ys, shortName, DataType.MAP, Archetype.MUTABLE, false, elementCount, run, preset);
	}

	public static String createAndMeasureTrove4jTIntIntHashMap(final Object[] data, int elementCount, int run,
			MemoryFootprintPreset preset) {
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

		String shortName = "TIntIntHashMap";

		String longName = "gnu.trove.map.hash.TIntIntHashMap";

		return measureAndReport(ys, shortName, DataType.MAP, Archetype.MUTABLE, false, elementCount, run, preset);
	}
		
	enum MemoryFootprintPreset {
		RETAINED_SIZE, DATA_STRUCTURE_OVERHEAD
	}

	@SuppressWarnings("unchecked")
	private static String measureAndReport(final Object objectToMeasure, final String className, DataType dataType,
			Archetype archetype, boolean supportsStagedMutability, int size, int run, MemoryFootprintPreset preset) {
		final Predicate<Object> predicate;

		switch (preset) {
		case DATA_STRUCTURE_OVERHEAD:
			predicate = Predicates
					.not(Predicates.or(Predicates.instanceOf(Integer.class), Predicates.instanceOf(BigInteger.class),
							Predicates.instanceOf(JmhValue.class), Predicates.instanceOf(PureInteger.class)));
			break;
		case RETAINED_SIZE:
			predicate = Predicates.alwaysTrue();
			break;
		default:
			throw new IllegalStateException();
		}

		return measureAndReport(objectToMeasure, className, dataType, archetype, supportsStagedMutability, size, run,
				predicate);
	}

	private static String measureAndReport(final Object objectToMeasure, final String className, DataType dataType,
			Archetype archetype, boolean supportsStagedMutability, int size, int run) {
		return measureAndReport(objectToMeasure, className, dataType, archetype, supportsStagedMutability, size, run,
				MemoryFootprintPreset.DATA_STRUCTURE_OVERHEAD);
	}

	private static String measureAndReport(final Object objectToMeasure, final String className, DataType dataType,
			Archetype archetype, boolean supportsStagedMutability, int size, int run, Predicate<Object> predicate) {
		// System.out.println(GraphLayout.parseInstance(objectToMeasure).totalSize());

		long memoryInBytes = objectexplorer.MemoryMeasurer.measureBytes(objectToMeasure, predicate);
		Footprint memoryFootprint = objectexplorer.ObjectGraphMeasurer.measure(objectToMeasure, predicate);

		final String statString = String.format("%d\t %60s\t\t %s", memoryInBytes, className, memoryFootprint);
		System.out.println(statString);

		// final String statLatexString = String.format("%s & %s & %s & %b & %d
		// & %d & %d & \"%s\" \\\\", className, dataType, archetype,
		// supportsStagedMutability, memoryInBytes,
		// memoryFootprint.getObjects(), memoryFootprint.getReferences(),
		// memoryFootprint.getPrimitives());
		// System.out.println(statLatexString);

		final String statFileString = String.format("%d,%d,%s,%s,%s,%b,%d,%d,%d", size, run, className, dataType,
				archetype, supportsStagedMutability, memoryInBytes, memoryFootprint.getObjects(),
				memoryFootprint.getReferences());

		return statFileString;
		// writeToFile(statFileString);
	}

}