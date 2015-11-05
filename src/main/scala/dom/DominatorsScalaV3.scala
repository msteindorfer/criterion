package dom

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.ArrayList

import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.asScalaSetConverter
import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.collection.immutable.HashMap
import scala.collection.immutable.HashSet
import scala.collection.mutable.Builder

import org.rascalmpl.value.IConstructor
import org.rascalmpl.value.IMap
import org.rascalmpl.value.ISet
import org.rascalmpl.value.ITuple
import org.rascalmpl.value.IValue
import org.rascalmpl.value.io.BinaryValueReader
import org.rascalmpl.value.io.BinaryValueWriter
import org.rascalmpl.value.io.StandardTextWriter
import org.rascalmpl.interpreter.utils.Timing

import dom.AllDominatorsRunner.CURRENT_DATA_SET
import dom.AllDominatorsRunner.DATA_SET_SINGLE_FILE_NAME
import dom.AllDominatorsRunner.LOG_BINARY_RESULTS
import dom.AllDominatorsRunner.LOG_TEXTUAL_RESULTS

import dom.DominatorsScalaV3._

/**
 * Purely using immutable data structures without Builders.
 * Port from CHART implementation. Uses immutable.{HashSet,HashMap} instead of CHART's {TrieSet,TrieMap}. 
 */
class DominatorsScalaV3 {

	def setofdomsets(dom: HashMap[IConstructor, HashSet[IConstructor]], ps: HashSet[IConstructor]): HashSet[HashSet[IConstructor]] = {
		var result = HashSet.empty[HashSet[IConstructor]]
		
		for (p <- ps) {
			result += dom.getOrElse(p, HashSet.empty[IConstructor])
		}

		result
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

		throw new RuntimeException("no entry?")
	}

	def calculateDominators(graph: HashSet[ITuple]): HashMap[IConstructor, HashSet[IConstructor]] = {
		val n0: IConstructor = getTop(graph)
		val nodes: HashSet[IConstructor] = carrier(graph)
		val preds: HashMap[IConstructor, HashSet[IConstructor]] = toMap(project(graph, 1, 0))

		// desired way to write, but does not work ...
		// val dom: HashMap[IConstructor, HashSet[IConstructor]] = for (n <- (nodes - n0)) yield (n, nodes);

		var dom = HashMap.empty[IConstructor, HashSet[IConstructor]]
		dom += ((n0, HashSet(n0)))
		for (n <- (nodes - n0)) {
			dom += ((n, nodes))
		}
	
		var prev: HashMap[IConstructor, HashSet[IConstructor]] = HashMap.empty

		while (prev != dom) {
			prev = dom

			var domNew = HashMap.empty[IConstructor, HashSet[IConstructor]]

			for (n <- nodes) {
				val ps = preds.getOrElse(n, HashSet.empty)

				val sos = setofdomsets(dom, ps)

				val intersected = if (sos == null || sos.isEmpty || sos.contains(HashSet.empty)) HashSet.empty[IConstructor] else sos reduce { _ intersect _ }

				val newValue = intersected union HashSet(n)

				domNew += ((n, newValue))
			}

			dom = domNew
		}

		dom
	}

}

object DominatorsScalaV3 {

	def main(args: Array[String]) {
		testOne
		//		testAll
	}

	def testOne: IMap = {
		val vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance

		val data = new BinaryValueReader().read(vf, new FileInputStream(DATA_SET_SINGLE_FILE_NAME)).asInstanceOf[ISet]

		// convert data to remove PDB dependency
		val graph: HashSet[ITuple] = pdbSetToImmutableSet(data);

		val before = Timing.getCpuTime();
		val results: HashMap[IConstructor, HashSet[IConstructor]] = new DominatorsScalaV3().calculateDominators(graph);

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

	def testAll: ISet = {
		// convert data to remove PDB dependency
		val graphs: ArrayList[HashSet[ITuple]] = pdbMapToArrayListOfValues(CURRENT_DATA_SET);

		val resultBldr = HashSet.newBuilder[HashMap[IConstructor, HashSet[IConstructor]]]

		val before = Timing.getCpuTime();
		for (graph <- graphs.asScala) {
			try {
				resultBldr += new DominatorsScalaV3().calculateDominators(graph)
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
		val vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance

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
		val vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance

		val builder = vf.setWriter

		for (key <- set) {
			builder.insert(key)
		}

		return builder.done
	}

	def immutableMapToPdbMap(
		result: HashMap[IConstructor, HashSet[IConstructor]]): IMap = {
		// convert back to PDB for serialization
		val vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance

		val builder = vf.mapWriter

		for (entry <- result) {
			builder.put(entry._1, immutableSetToPdbSet(entry._2))
		}

		builder.done
	}

	//	@SuppressWarnings("rawtypes")
	//	public final static ImmutableSet EMPTY = DefaultTrieSet.of();
	//
	//	/*
	//	 * Intersect many sets.
	//	 */
	//	@SuppressWarnings("unchecked")
	//	public static <K> ImmutableSet<K> intersect(ImmutableSet<ImmutableSet<K>> sets) {
	//		if (sets == null || sets.isEmpty() || sets.contains(EMPTY)) {
	//			return EMPTY;
	//		}
	//
	//		ImmutableSet<K> first = sets.iterator().next();
	//		sets = sets.__remove(first);
	//
	//		ImmutableSet<K> result = first;
	//		for (ImmutableSet<K> elem : sets) {
	//			result = Util.intersect(result, elem);
	//		}
	//
	//		return result;
	//	}

	/*
	 * Intersect two sets.
	 */
	//	public static <K> ImmutableSet<K> intersect(ImmutableSet<K> set1, ImmutableSet<K> set2) {
	//		if (set1 == set2)
	//			return set1;
	//		if (set1 == null)
	//			return DefaultTrieSet.of();
	//		if (set2 == null)
	//			return DefaultTrieSet.of();
	//
	//		final ImmutableSet<K> smaller;
	//		final ImmutableSet<K> bigger;
	//
	//		final ImmutableSet<K> unmodified;
	//
	//		if (set2.size() >= set1.size()) {
	//			unmodified = set1;
	//			smaller = set1;
	//			bigger = set2;
	//		} else {
	//			unmodified = set2;
	//			smaller = set2;
	//			bigger = set1;
	//		}
	//
	//		final TransientSet<K> tmp = smaller.asTransient();
	//		boolean modified = false;
	//
	//		for (Iterator<K> it = tmp.iterator(); it.hasNext();) {
	//			final K key = it.next();
	//			if (!bigger.contains(key)) {
	//				it.remove();
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
	 * Subtract one set from another.
	 */
	//	public static <K> ImmutableSet<K> subtract(ImmutableSet<K> set1, ImmutableSet<K> set2) {
	//		if (set1 == null && set2 == null)
	//			return DefaultTrieSet.of();
	//		if (set1 == set2)
	//			return DefaultTrieSet.of();
	//		if (set1 == null)
	//			return DefaultTrieSet.of();
	//		if (set2 == null)
	//			return set1;
	//
	//		final TransientSet<K> tmp = set1.asTransient();
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
	//	public static <K> ImmutableSet<K> union(ImmutableSet<K> set1, ImmutableSet<K> set2) {
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
	//		final TransientSet<K> tmp = bigger.asTransient();
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
		var result = HashSet.empty[T]

		for (iterable <- set1; nested <- iterable.asScala) {
			result += nested.asInstanceOf[T]
		}

		result
	}

	/*
	 * Projection from a tuple to single field.
	 */
	def project[K <: IValue](set1: HashSet[ITuple], field: Int): HashSet[K] = {
		var result = HashSet.empty[K]

		set1 foreach {
			tuple => result += tuple.select(field).asInstanceOf[K]
		}

		result
	}

	/*
	 * Projection from a tuple to another tuple with (possible reordered) subset
	 * of fields.
	 */
	def project[K <: IValue](set1: HashSet[ITuple], field1: Int, field2: Int): HashSet[K] = {
		var result = HashSet.empty[K]

		set1 foreach {
			tuple => result += tuple.select(field1, field2).asInstanceOf[K]
		}

		result
	}

	/*
	 * Convert a set of tuples to a map; value in old map is associated with a
	 * set of keys in old map.
	 */
	def toMap[K, V](st: HashSet[ITuple]): HashMap[K, HashSet[V]] = {
		val hm: java.util.HashMap[K, HashSet[V]] = new java.util.HashMap

		for (t <- st) {
			val key = t.get(0).asInstanceOf[K]
			val value = t.get(1).asInstanceOf[V]

			var wValSet = hm.get(key)
			if (wValSet == null) {
				wValSet = HashSet(value)
				hm.put(key, wValSet)
			}
			hm.put(key, wValSet + value)

		}

		var result = HashMap.empty[K, HashSet[V]]

		for (k <- hm.keySet.asScala) {
			result += ((k, hm.get(k)))
		}

		result
	}

}