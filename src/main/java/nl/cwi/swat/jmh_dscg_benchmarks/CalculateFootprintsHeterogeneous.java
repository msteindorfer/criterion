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

import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.rascalmpl.value.IValue;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import gnu.trove.map.hash.TIntIntHashMap;
import io.usethesource.capsule.ImmutableMap;
import io.usethesource.capsule.TrieMap_5Bits_Spec0To16;
import io.usethesource.capsule.TrieMap_5Bits_Spec0To16_IntKey_IntValue;
import io.usethesource.capsule.TrieMap_5Bits_Spec0To8;
import io.usethesource.capsule.TrieMap_5Bits_Spec0To8_IntKey_IntValue;
import io.usethesource.capsule.TrieMap_Heterogeneous_BleedingEdge;
import nl.cwi.swat.jmh_dscg_benchmarks.BenchmarkUtils.DataType;
import nl.cwi.swat.jmh_dscg_benchmarks.FootprintUtils.Archetype;
import objectexplorer.ObjectGraphMeasurer.Footprint;

public final class CalculateFootprintsHeterogeneous {

	public static void main(String[] args) {
		testOneConfiguration(2097152);

		FootprintUtils.writeToFile(Paths.get("map_sizes_heterogeneous_exponential.csv"), false,
				FootprintUtils.createExponentialRange(0, 24).stream()
						.flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));

		FootprintUtils.writeToFile(Paths.get("map_sizes_heterogeneous_tiny.csv"), false,
				FootprintUtils.createLinearRange(0, 101, 1).stream()
						.flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));
		
		FootprintUtils.writeToFile(Paths.get("map_sizes_heterogeneous_small.csv"), false,
				FootprintUtils.createLinearRange(0, 10_100, 100).stream()
						.flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));

		FootprintUtils.writeToFile(Paths.get("map_sizes_heterogeneous_medium.csv"), false,
				FootprintUtils.createLinearRange(10_000, 101_000, 1_000).stream()
						.flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));

		FootprintUtils.writeToFile(Paths.get("map_sizes_heterogeneous_large.csv"), false,
				FootprintUtils.createLinearRange(100_000, 8_100_000, 100_000).stream()
						.flatMap(size -> testOneConfiguration(size).stream()).collect(Collectors.toList()));
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
						createAndMeasureJavaUtilHashMap(data, size, 0, preset),
						createAndMeasureTrieMapHeterogeneous(data, size, 0, preset, true),
						createAndMeasureTrieMapHeterogeneous(data, size, 0, preset, false),
						createAndMeasureTrove4jIntArrayList(data, size, 0, preset),
						createAndMeasureTrieMapHomogeneous(data, size, 0, preset, true) }))
				.collect(Collectors.toList());
	}

	public static String createAndMeasureTrieMapHomogeneous(final Object[] data, int elementCount, int run,
			MemoryFootprintPreset preset, boolean isSpecialized) {
		ImmutableMap<Integer, Integer> ys = null;

		if (isSpecialized) {
			ys = TrieMap_5Bits_Spec0To8_IntKey_IntValue.of();
			// ys = TrieMap_BleedingEdge.of();
		} else {
			ys = TrieMap_5Bits_Spec0To8.of();
		}

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

		String shortName = "TrieMapIntInt";

		// String longName = String.format(
		// "io.usethesource.capsule.TrieMap_5Bits_Spec0To8", isSpecialized);

		return measureAndReport(ys, shortName, DataType.MAP, Archetype.PERSISTENT, false, elementCount, run, preset);
	}

	public static String createAndMeasureTrieMapHeterogeneous(final Object[] data, int elementCount, int run,
			MemoryFootprintPreset preset, boolean storePrimivesBoxed) {
		TrieMap_Heterogeneous_BleedingEdge ys = (TrieMap_Heterogeneous_BleedingEdge) TrieMap_Heterogeneous_BleedingEdge.of();

		for (Object v : data) {
			if (v instanceof Integer && storePrimivesBoxed) {
//				PureInteger boxedValue = new PureInteger(((Integer) v).intValue());
				Integer boxedValue = (Integer) v;

				ys = (TrieMap_Heterogeneous_BleedingEdge) ys.__put(boxedValue, boxedValue);
				assert ys.containsKey(boxedValue);
			} else if (v instanceof Integer && !storePrimivesBoxed) {
				int unboxedValue = ((Integer) v).intValue();
				
				ys = (TrieMap_Heterogeneous_BleedingEdge) ys.__put(unboxedValue, unboxedValue);
				assert ys.containsKey(unboxedValue);
//			} else {
//				ys = (TrieMap_Heterogeneous_BleedingEdge) ys.__put(v, v);
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

	public static String createAndMeasureTrove4jIntArrayList(final Object[] data, int elementCount, int run,
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
							Predicates.instanceOf(IValue.class), Predicates.instanceOf(PureInteger.class)));
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