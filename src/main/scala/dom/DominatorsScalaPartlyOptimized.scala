/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package dom

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.ArrayList

import scala.collection.JavaConverters.asScalaSetConverter
import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.collection.immutable.HashMap
import scala.collection.immutable.HashSet
import scala.collection.mutable.Builder

import io.usethesource.vallang.IConstructor
import io.usethesource.vallang.IMap
import io.usethesource.vallang.ISet
import io.usethesource.vallang.ITuple
import io.usethesource.vallang.IValue
import io.usethesource.vallang.io.old.BinaryValueReader
import io.usethesource.vallang.io.old.BinaryValueWriter
import io.usethesource.vallang.io.StandardTextWriter
import org.openjdk.jmh.infra.Blackhole
import org.rascalmpl.interpreter.utils.Timing

import dom.AllDominatorsRunner.DATA_SET_SINGLE_FILE_NAME
import dom.AllDominatorsRunner.LOG_BINARY_RESULTS
import dom.AllDominatorsRunner.LOG_TEXTUAL_RESULTS

import dom.DominatorsScalaPartlyOptimized._

/**
 * Port from CHART implementation. Uses immutable.{HashSet,HashMap} instead of CHART's {TrieSet,TrieMap}.
 * Transients became Builders.
 */
class DominatorsScalaPartlyOptimized extends DominatorBenchmark {

	def setofdomsets(dom: HashMap[IConstructor, HashSet[IConstructor]], ps: HashSet[IConstructor]): HashSet[HashSet[IConstructor]] = {
//		if (!(ps subsetOf dom.keySet))
//			return HashSet.empty[HashSet[IConstructor]]
//		
//		val bldr = HashSet.newBuilder[HashSet[IConstructor]]
//		
//		for (entry <- dom) {
//			if (ps contains entry._1)
//				bldr += entry._2
//		}
//		
//		bldr.result
			
//		val bldr = collection.mutable.HashSet.empty[HashSet[IConstructor]]
//
//		for (p <- ps) {
//			bldr += dom.getOrElse(p, HashSet.empty[IConstructor])
//		}
//		
//		HashSet.empty[HashSet[IConstructor]] ++ bldr
		
		val bldr = HashSet.newBuilder[HashSet[IConstructor]]

		for (p <- ps) {
			bldr += dom.getOrElse(p, HashSet.empty)
		}

		bldr.result
	}

	def listofdomsets(dom: HashMap[IConstructor, HashSet[IConstructor]], ps: HashSet[IConstructor]): Option[ArrayList[HashSet[IConstructor]]] = {
		val resultList = new ArrayList[HashSet[IConstructor]](dom.size)

		for (p <- ps) {
			val value = dom.get(p)

			if (value.isDefined && !value.get.isEmpty) {
				resultList add value.get
			} else {
				return None
			}
		}

		Some(resultList)
	}

	def top(graph: HashSet[ITuple]): HashSet[IConstructor] = project(graph, 0) -- project(graph, 1)

	def getTop(graph: HashSet[ITuple]): IConstructor = {
		top(graph) foreach {
			candidate =>
				candidate.getName match {
					case "methodEntry" | "functionEntry" | "scriptEntry" => return candidate
					case _: String => {}
				}
		}

		throw new NoSuchElementException("No candidate found.")
	}

	def calculateDominators(graph: HashSet[ITuple]): HashMap[IConstructor, HashSet[IConstructor]] = {
		val n0: IConstructor = getTop(graph)
		val nodes: HashSet[IConstructor] = carrier(graph)
		val preds: HashMap[IConstructor, HashSet[IConstructor]] = toMap(project(graph, 1, 0))

		// desired way to write, but does not work ...
		// val dom: HashMap[IConstructor, HashSet[IConstructor]] = for (n <- (nodes - n0)) yield (n, nodes);

		var dom: HashMap[IConstructor, HashSet[IConstructor]] = {
			val domBldr = HashMap.newBuilder[IConstructor, HashSet[IConstructor]]
			domBldr += ((n0, HashSet(n0)))
			for (n <- (nodes - n0)) {
				domBldr += ((n, nodes))
			}
			domBldr.result
		}

		var prev: HashMap[IConstructor, HashSet[IConstructor]] = HashMap.empty

		while (prev != dom) {
			prev = dom

			val domBldr = HashMap.newBuilder[IConstructor, HashSet[IConstructor]]

//			val it = nodes.iterator
//			while (it.hasNext) {
//				val n = it.next
				
			for (n <- nodes) {
				val ps = preds.getOrElse(n, HashSet.empty)

//				val sos = setofdomsets(dom, ps)
//				val intersected = if (sos == null || sos.isEmpty || sos.contains(HashSet.empty)) HashSet.empty[IConstructor] else intersect(sos)
//				val newValue = intersected union HashSet(n)
				
				val alos = listofdomsets(dom, ps)
				val intersected = if (alos.isEmpty || alos.get.isEmpty) HashSet.empty[IConstructor] else intersect(alos.get) 
				val newValue = intersected + n

				domBldr += ((n, newValue))
			}

			dom = domBldr.result
		}

		dom
	}

	def performBenchmark(bh: Blackhole, sampledGraphsNative: java.util.List[_]): Unit = {
		for (graph <- sampledGraphsNative.asInstanceOf[ArrayList[HashSet[ITuple]]].asScala) {
		  try {
				bh.consume(new DominatorsScalaPartlyOptimized().calculateDominators(graph))
			} catch {
				case e: NoSuchElementException => System.err.println(e.getMessage)
			}
		}
	}

	def convertDataToNativeFormat(sampledGraphs: java.util.List[ISet]): java.util.List[_] = {
		val graphs: ArrayList[HashSet[ITuple]] = new ArrayList(sampledGraphs.size())

		for (graph <- sampledGraphs.asScala) {
			val convertedValueBldr = HashSet.newBuilder[ITuple]
			for (tuple <- graph.asScala) {
				convertedValueBldr += tuple.asInstanceOf[ITuple]
			}
			graphs add convertedValueBldr.result
		}

		graphs
	}

}

object DominatorsScalaPartlyOptimized {

	def main(args: Array[String]) {
		testOne
		//		testAll
	}

	def testOne: IMap = {
		val vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance

		val data = new BinaryValueReader().read(vf, new FileInputStream(DATA_SET_SINGLE_FILE_NAME)).asInstanceOf[ISet]

		// convert data to remove PDB dependency
		val graph: HashSet[ITuple] = pdbSetToImmutableSet(data);

		val before = Timing.getCpuTime();
		val results: HashMap[IConstructor, HashSet[IConstructor]] = new DominatorsScalaPartlyOptimized().calculateDominators(graph);

		System.err.println("SCALA" + "\nDuration: "
			+ ((Timing.getCpuTime() - before) / 1000000000) + " seconds\n");

		val pdbResults: IMap = immutableMapToPdbMap(results)

		if (LOG_BINARY_RESULTS)
			new BinaryValueWriter().write(pdbResults, new FileOutputStream(
				"data/dominators-java-without-pdb-single.bin"));

		if (LOG_TEXTUAL_RESULTS)
			new StandardTextWriter().write(pdbResults, new FileWriter(
				"data/dominators-java-without-pdb-single.txt"));

		return pdbResults;
	}

	def testAll(sampledGraphs: IMap): ISet = {
		// convert data to remove PDB dependency
		val graphs: ArrayList[HashSet[ITuple]] = pdbMapToArrayListOfValues(sampledGraphs);

		val resultBldr = HashSet.newBuilder[HashMap[IConstructor, HashSet[IConstructor]]]

		val before = Timing.getCpuTime();
		for (graph <- graphs.asScala) {
		  try {
				resultBldr += new DominatorsScalaPartlyOptimized().calculateDominators(graph)
			} catch {
				case e: RuntimeException => System.err.println(e.getMessage)
			}
		}

		System.err.println("SCALA" + "\nDuration: "
			+ ((Timing.getCpuTime() - before) / 1000000000) + " seconds\n")

		// convert back to PDB for serialization
		val pdbResults = immutableSetOfMapsToSetOfMapValues(resultBldr.result);

		if (LOG_BINARY_RESULTS)
			new BinaryValueWriter().write(pdbResults, new FileOutputStream(
				"data/dominators-scala.bin"));

		if (LOG_TEXTUAL_RESULTS)
			new StandardTextWriter().write(pdbResults, new FileWriter(
				"data/dominators-scala.txt"));

		return pdbResults;
	}

	def pdbMapToArrayListOfValues(data: IMap): ArrayList[HashSet[ITuple]] = {
		// convert data to remove PDB dependency
		val graphs: ArrayList[HashSet[ITuple]] = new ArrayList(data.size())

		for (key <- data.asScala) {
			val value = data.get(key).asInstanceOf[ISet]
			val convertedValueBldr = HashSet.newBuilder[ITuple]
			for (tuple <- value.asScala) {
				convertedValueBldr += tuple.asInstanceOf[ITuple]
			}
			graphs add convertedValueBldr.result
		}

		graphs
	}

	def immutableSetOfMapsToSetOfMapValues(result: HashSet[HashMap[IConstructor, HashSet[IConstructor]]]): ISet = {
		// convert back to PDB for serialization
		val vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance

		val resultBuilder = vf.setWriter

		for (dominatorResult <- result) {
			val builder = vf.mapWriter
			for (entry <- dominatorResult) {
				builder.put(entry._1, immutableSetToPdbSet(entry._2))
			}
			resultBuilder insert builder.done
		}

		resultBuilder.done
	}

	def pdbSetToImmutableSet(set: ISet): HashSet[ITuple] = {
		val bldr = HashSet.newBuilder[ITuple]

		for (tuple <- set.asScala) {
			bldr += tuple.asInstanceOf[ITuple]
		}

		return bldr.result
	}

	def immutableSetToPdbSet[K <: IValue](set: HashSet[K]): ISet = {
		val vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance

		val builder = vf.setWriter

		for (key <- set) {
			builder.insert(key)
		}

		return builder.done
	}

	def immutableMapToPdbMap(
		result: HashMap[IConstructor, HashSet[IConstructor]]): IMap = {
		// convert back to PDB for serialization
		val vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance

		val builder = vf.mapWriter

		for (entry <- result) {
			builder.put(entry._1, immutableSetToPdbSet(entry._2))
		}

		builder.done
	}

	/*
	 * Intersect many sets.
	 */
	def intersect[K](sets: HashSet[HashSet[K]]): HashSet[K] = {
		if (sets == null || sets.isEmpty || sets.contains(HashSet.empty[K])) {
			return HashSet.empty[K];
		}

		val first = sets.iterator.next
		var result = first
		for (elem <- (sets - first)) {
			result = result intersect elem
		}

		result
	}

	/*
	 * Intersect many sets.
	 */
	def intersect[K](sets: ArrayList[HashSet[K]]): HashSet[K] = {
		val first = sets.get(0)
		
		var result = first
		
		for (i <- 1 until sets.size) {
			result = intersect(result, sets.get(i))
		}
		
//		for (elem <- sets.asScala.tail) {
//			result = intersect(result, elem)
//		}

		result
	}

	/*
	 * Intersect two sets.
	 */
	def intersect[K](set1: HashSet[K], set2: HashSet[K]): HashSet[K] = {
		if (set1 == set2)
			return set1
		if (set1 == null)
			return HashSet.empty[K]
		if (set2 == null)
			return HashSet.empty[K]

		var smaller = HashSet.empty[K]
		var bigger = HashSet.empty[K]

		if (set2.size >= set1.size) {
			smaller = set1
			bigger = set2
		} else {
			smaller = set2
			bigger = set1
		}

		var tmp = HashSet.newBuilder[K]
		var modified = false

		for (key <- smaller) {
			if (bigger contains key) {
				modified = true
			} else {
				tmp += key
			}
		}

		if (modified) {
			return tmp.result
		} else {
			if (set2.size >= set1.size) {
				return set1
			} else {
				return set2
			}
		}
	}

	/*
	 * Subtract one set from another.
	 */
	//	public static <K> Immutable<K> subtract(Immutable<K> set1, Immutable<K> set2) {
	//		if (set1 == null && set2 == null)
	//			return DefaultTrieSet.of();
	//		if (set1 == set2)
	//			return DefaultTrieSet.of();
	//		if (set1 == null)
	//			return DefaultTrieSet.of();
	//		if (set2 == null)
	//			return set1;
	//
	//		final Transient<K> tmp = set1.asTransient();
	//		boolean modified = false;
	//
	//		for (K key : set2) {
	//			if (tmp.__remove(key)) {
	//				modified = true;
	//			}
	//		}
	//
	//		if (modified) {
	//			return tmp.freeze();
	//		} else {
	//			return set1;
	//		}
	//	}

	/*
	 * Union two sets.
	 */
	//	public static <K> Immutable<K> union(Immutable<K> set1, Immutable<K> set2) {
	//		if (set1 == null && set2 == null)
	//			return DefaultTrieSet.of();
	//		if (set1 == null)
	//			return set2;
	//		if (set2 == null)
	//			return set1;
	//
	//		if (set1 == set2)
	//			return set1;
	//
	//		final ImmutableSet<K> smaller;
	//		final ImmutableSet<K> bigger;
	//
	//		final ImmutableSet<K> unmodified;
	//
	//		if (set2.size() >= set1.size()) {
	//			unmodified = set2;
	//			smaller = set1;
	//			bigger = set2;
	//		} else {
	//			unmodified = set1;
	//			smaller = set2;
	//			bigger = set1;
	//		}
	//
	//		final Transient<K> tmp = bigger.asTransient();
	//		boolean modified = false;
	//
	//		for (K key : smaller) {
	//			if (tmp.__insert(key)) {
	//				modified = true;
	//			}
	//		}
	//
	//		if (modified) {
	//			return tmp.freeze();
	//		} else {
	//			return unmodified;
	//		}
	//	}

	/*
	 * Flattening of a set (of ITuple elements).
	 * 
	 * Because of the untyped nature of ITuple, the implementation is not
	 * strongly typed.
	 */
	def carrier[K <: java.lang.Iterable[IValue], T](set1: HashSet[K]): HashSet[T] = {
		val bldr = HashSet.newBuilder[T]

		for (iterable <- set1; nested <- iterable.asScala) {
			bldr += nested.asInstanceOf[T]
		}

		bldr.result
	}

	/*
	 * Projection from a tuple to single field.
	 */
	def project[K <: IValue](set1: HashSet[ITuple], field: Int): HashSet[K] = {
		val bldr = HashSet.newBuilder[K]

		set1 foreach {
			tuple => bldr += tuple.select(field).asInstanceOf[K]
		}

		bldr.result
	}

	/*
	 * Projection from a tuple to another tuple with (possible reordered) subset
	 * of fields.
	 */
	def project[K <: IValue](set1: HashSet[ITuple], field1: Int, field2: Int): HashSet[K] = {
		val bldr = HashSet.newBuilder[K]

		set1 foreach {
			tuple => bldr += tuple.select(field1, field2).asInstanceOf[K]
		}

		bldr.result
	}

	/*
	 * Convert a set of tuples to a map; value in old map is associated with a
	 * set of keys in old map.
	 */
	def toMap[K, V](st: HashSet[ITuple]): HashMap[K, HashSet[V]] = {
		val hm: java.util.HashMap[K, Builder[V, HashSet[V]]] = new java.util.HashMap

		for (t <- st) {
			val key = t.get(0).asInstanceOf[K]
			val value = t.get(1).asInstanceOf[V]

			var wValSet = hm.get(key)
			if (wValSet == null) {
				wValSet = HashSet.newBuilder[V]
				hm.put(key, wValSet)
			}
			wValSet += value

		}

		val bldr = HashMap.newBuilder[K, HashSet[V]]

		for (k <- hm.keySet.asScala) {
			bldr += ((k, hm.get(k).result))
		}

		bldr.result
	}

}
