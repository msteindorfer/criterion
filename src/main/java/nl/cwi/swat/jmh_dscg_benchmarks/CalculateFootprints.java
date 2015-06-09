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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import objectexplorer.ObjectGraphMeasurer.Footprint;

import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory;
import org.eclipse.imp.pdb.facts.util.ImmutableMap;
import org.eclipse.imp.pdb.facts.util.ImmutableSet;
import org.eclipse.imp.pdb.facts.util.TrieMap_5Bits;
import org.eclipse.imp.pdb.facts.util.TrieSet_5Bits;

import scala.Tuple2;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentSet;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;


public final class CalculateFootprints {

	public enum Archetype {
		MUTABLE,
		IMMUTABLE,
		PERSISTENT
	}

	public enum DataType {
		MAP,
		SET
	}

	private static final IValueFactory valueFactory = ValueFactory.getInstance();

	private static boolean reportSet = true;
	private static boolean reportMap = true;
	
	private final static String csvHeader = "elementCount,run,className,dataType,archetype,supportsStagedMutability,footprintInBytes,footprintInObjects,footprintInReferences";
	
	public static ISet setUpTestSetWithRandomContent(int size, int run) throws Exception {
		// TrieMapVsOthersFootprint.size = size;

		ISetWriter setWriter = valueFactory.setWriter();
	
		int seedForThisTrial = BenchmarkUtils.seedFromSizeAndRun(size, run);
		Random rand = new Random(seedForThisTrial);

		System.out.println(String.format("Seed for this trial: %d.", seedForThisTrial));

		for (int i = size; i > 0; i--) {
			final int j = rand.nextInt();
			final IValue current = valueFactory.integer(j); 
						
			setWriter.insert(current);
		}

		return setWriter.done();
	}
	
	public static void timeTrieSet(final ISet testSet, int elementCount, int run) {			
		ImmutableSet<IValue> xs = TrieSet_5Bits.<IValue>of();
		ImmutableMap<IValue, IValue> ys = TrieMap_5Bits.<IValue, IValue>of();
				
		for (IValue v : testSet) {
			if (reportSet) xs = xs.__insert(v);
			if (reportMap) ys = ys.__put(v, v);
		}
				
		if (reportSet) measureAndReport(xs, "org.eclipse.imp.pdb.facts.util.TrieSet_5Bits", DataType.SET, Archetype.PERSISTENT, false, elementCount, run);
		if (reportMap) measureAndReport(ys, "org.eclipse.imp.pdb.facts.util.TrieMap_5Bits", DataType.MAP, Archetype.PERSISTENT, false, elementCount, run);
	}

	public static void timeClojurePersistent(final ISet testSet, int elementCount, int run) {
		IPersistentSet xs = (IPersistentSet) PersistentHashSet.EMPTY;
		IPersistentMap ys = (IPersistentMap) PersistentHashMap.EMPTY;
		
		for (IValue v : testSet) {
			if (reportSet) xs = (IPersistentSet) xs.cons(v);
			if (reportMap) ys = (IPersistentMap) ys.assoc(v, v);
		}
		
		if (reportSet) measureAndReport(xs, "clojure.lang.PersistentHashSet", DataType.SET, Archetype.PERSISTENT, true, elementCount, run);
		if (reportMap) measureAndReport(ys, "clojure.lang.PersistentHashMap", DataType.MAP, Archetype.PERSISTENT, true, elementCount, run);
	}
	
	public static void timeScalaPersistent(final ISet testSet, int elementCount, int run) {
		scala.collection.immutable.HashSet<IValue> xs = new scala.collection.immutable.HashSet<>();
		scala.collection.immutable.HashMap<IValue, IValue> ys = new scala.collection.immutable.HashMap<>();
		
		for (IValue v : testSet) {
			if (reportSet) xs = xs.$plus(v);
			if (reportMap) ys = ys.$plus(new Tuple2<>(v, v));
		}

		if (reportSet) measureAndReport(xs, "scala.collection.immutable.HashSet", DataType.SET, Archetype.PERSISTENT, false, elementCount, run);
		if (reportMap) measureAndReport(ys, "scala.collection.immutable.HashMap", DataType.MAP, Archetype.PERSISTENT, false, elementCount, run);
	}
	
	private static void measureAndReport(final Object objectToMeasure, final String className, DataType dataType, Archetype archetype, boolean supportsStagedMutability, int size, int run) {
		/*
		 * ITuple, etc should be measured. Only excluding IInteger (actual elements).
		 */
		Predicate<Object> jointPredicate = Predicates.not(Predicates.instanceOf(IInteger.class));
		
		long memoryInBytes = objectexplorer.MemoryMeasurer.measureBytes(objectToMeasure,
						jointPredicate);
		Footprint memoryFootprint = objectexplorer.ObjectGraphMeasurer.measure(objectToMeasure,
						jointPredicate);

		final String statString = String.format("%d\t %60s\t\t %s", memoryInBytes, className,
						memoryFootprint);
		System.out.println(statString);
		
		final String statFileString = String.format("%d,%d,%s,%s,%s,%b,%d,%d,%d", size, run, className, dataType, archetype, supportsStagedMutability, memoryInBytes, memoryFootprint.getObjects(), memoryFootprint.getReferences());		
		
		writeToFile(statFileString);
	}
	
	private static void writeToFile(String line) {
		writeToFile(true, line);
	}
	
	private static void writeToFile(boolean isAppendingToFile, String line) {
		final List<String> lines = new ArrayList<>();
		lines.add(line);
		
		writeToFile(isAppendingToFile, lines);
	}
	
	private static void writeToFile(boolean isAppendingToFile, List<String> lines) {
		final Path file = Paths.get(String.format("map-sizes-and-statistics.csv"));
				
		// write stats to file
		try {
			if (isAppendingToFile) {
				Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
			} else {
				Files.write(file, lines, StandardCharsets.UTF_8);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void footprint_Random_Persistent() {	
		
		for (int exp = 0; exp <= 23; exp += 1) {
			final int count = (int) Math.pow(2, exp);
			
			for (int run = 0; run < 4; run++) {
			
				ISet tmpSet = null;
				
				try {
					tmpSet = setUpTestSetWithRandomContent(count, run);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				final ISet testSet = tmpSet;
				final int currentRun = run;
				
				timeTrieSet(testSet, count, currentRun);
				timeClojurePersistent(testSet, count, currentRun);
				timeScalaPersistent(testSet, count, currentRun);
			}
		}		
	}
	
	public static void main(String[] args) throws Exception {
		writeToFile(false, csvHeader);		
		footprint_Random_Persistent();
	}
	
}
