package dom.multimap;

import static dom.AllDominatorsRunner.DATA_SET_SINGLE_FILE_NAME;
import static dom.multimap.Util_New.EMPTY;
import static dom.multimap.Util_New.carrier;
import static dom.multimap.Util_New.intersect;
import static dom.multimap.Util_New.project;
import static dom.multimap.Util_New.subtract;
import static dom.multimap.Util_New.toMultimap;
import static dom.multimap.Util_New.union;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.openjdk.jmh.infra.Blackhole;
import org.rascalmpl.interpreter.utils.Timing;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IMap;
import org.rascalmpl.value.IMapWriter;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISetWriter;
import org.rascalmpl.value.ITuple;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.value.io.BinaryValueReader;

import dom.DominatorBenchmark;
import io.usethesource.capsule.ImmutableMap;
import io.usethesource.capsule.Set;
import io.usethesource.capsule.SetMultimap;
import io.usethesource.capsule.TrieSet;
import io.usethesource.capsule.TrieSetMultimap;

public class DominatorsSetMultimap_New implements DominatorBenchmark {

	private Set.Immutable<Set.Immutable<IConstructor>> setofdomsets(SetMultimap.Immutable<IConstructor, IConstructor> dom, Set.Immutable<IConstructor> preds) {
		Set.Transient<Set.Immutable<IConstructor>> result = TrieSet.transientOf();

		for (IConstructor p : preds) {
			@SuppressWarnings("unchecked")
			Set.Immutable<IConstructor> ps = dom.apply(p).orElse(EMPTY);
			result.insert(ps);
		}

		return result.asImmutable();
	}

	public Set.Immutable<IConstructor> top(Set.Immutable<ITuple> graph) {
		return subtract(project(graph, 0), project(graph, 1));
	}

	public IConstructor getTop(Set.Immutable<ITuple> graph) {
		for (IConstructor candidate : top(graph)) {
			switch (candidate.getName()) {
			case "methodEntry":
			case "functionEntry":
			case "scriptEntry":
				return candidate;
			}
		}

		throw new NoSuchElementException("No candidate found.");
	}

	public SetMultimap.Immutable<IConstructor, IConstructor> calculateDominators(Set.Immutable<ITuple> graph) {
		
		IConstructor n0 = getTop(graph);
		Set.Immutable<IConstructor> nodes = carrier(graph);
		
		SetMultimap.Immutable<IConstructor, IConstructor> preds = toMultimap(project(graph, 1, 0));
		
		SetMultimap.Transient<IConstructor, IConstructor> w = TrieSetMultimap.transientOf();
		w.insert(n0, n0);
		for (IConstructor n : nodes.remove(n0)) {
			w.put(n, nodes);
		}
		SetMultimap.Immutable<IConstructor, IConstructor> dom = w.asImmutable();
		
		SetMultimap.Immutable<IConstructor, IConstructor> prev = TrieSetMultimap.of();
		
		/*
		 * solve (dom) 
		 *   for (n <- nodes) 
		 *     dom[n] = {n} + intersect({dom[p] | p <- preds[n]?{}});
		 */
		while (!prev.equals(dom)) {
			prev = dom;
			
			SetMultimap.Transient<IConstructor, IConstructor> newDom = TrieSetMultimap.transientOf();

			for (IConstructor n : nodes) {
				@SuppressWarnings("unchecked")
				Set.Immutable<IConstructor> ps = preds.apply(n).orElse(EMPTY);
				
				Set.Immutable<Set.Immutable<IConstructor>> sos = setofdomsets(dom, ps);
				Set.Immutable<IConstructor> intersected = intersect(sos);
				
				if (!intersected.isEmpty()) {
					Set.Immutable<IConstructor> newValue = union(intersected, TrieSet.of(n));
					newDom.put(n, newValue);
				} else {
					newDom.insert(n, n);
				}
				
				// TODO: check if structural join of preds and dom is possible
				// TODO: implement structural intersect/union that is oblivious of size/hashCode 
				// TODO: check streaming solution dom.filter(preds(n)).values().intersect().union(n)
			}

			dom = newDom.asImmutable();
		}

		return dom;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		testOne();
		// assertDominatorsEqual();
	}

	public static IMap testOne() throws IOException, FileNotFoundException {
		IValueFactory vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();

		ISet data = (ISet) new BinaryValueReader().read(vf, new FileInputStream(DATA_SET_SINGLE_FILE_NAME));

		// convert data to remove PDB dependency
		Set.Immutable<ITuple> graph = pdbSetToImmutableSet(data);

		long before = Timing.getCpuTime();
		SetMultimap.Immutable<IConstructor, IConstructor> results = new DominatorsSetMultimap_New()
				.calculateDominators(graph);
		System.err.println("PDB_LESS_IMPLEMENTATION" + "\nDuration: " + ((Timing.getCpuTime() - before) / 1000000000)
				+ " seconds\n");

//		IMap pdbResults = immutableMapToPdbMap(results);
//
//		if (LOG_BINARY_RESULTS)
//			new BinaryValueWriter().write(pdbResults,
//					new FileOutputStream("data/dominators-java-without-pdb-single.bin"));
//
//		if (LOG_TEXTUAL_RESULTS)
//			new StandardTextWriter().write(pdbResults, new FileWriter("data/dominators-java-without-pdb-single.txt"));
//
//		return pdbResults;
		
		return null;
	}

	public static ISet testAll(IMap sampledGraphs) throws IOException, FileNotFoundException {
		// convert data to remove PDB dependency
		ArrayList<Set.Immutable<ITuple>> graphs = pdbMapToArrayListOfValues(sampledGraphs);

		Set.Transient<SetMultimap.Immutable<IConstructor, IConstructor>> result = TrieSet.transientOf();
		long before = Timing.getCpuTime();
		for (Set.Immutable<ITuple> graph : graphs) {
			try {
				result.insert(new DominatorsSetMultimap_New().calculateDominators(graph));
			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
			}
		}
		System.err.println("PDB_LESS_IMPLEMENTATION" + "\nDuration: " + ((Timing.getCpuTime() - before) / 1000000000)
				+ " seconds\n");

//		// convert back to PDB for serialization
//		ISet pdbResults = ImmutableSetOfMapsToSetOfMapValues(result.asImmutable());
//
//		if (LOG_BINARY_RESULTS)
//			new BinaryValueWriter().write(pdbResults, new FileOutputStream("data/dominators-java.bin"));
//
//		if (LOG_TEXTUAL_RESULTS)
//			new StandardTextWriter().write(pdbResults, new FileWriter("data/dominators-java-without-pdb.txt"));
//
//		return pdbResults;
		
		return null;
	}

	private static ArrayList<Set.Immutable<ITuple>> pdbMapToArrayListOfValues(IMap data) {
		// convert data to remove PDB dependency
		ArrayList<Set.Immutable<ITuple>> graphs = new ArrayList<>(data.size());
		for (IValue key : data) {
			ISet value = (ISet) data.get(key);

			Set.Transient<ITuple> convertedValue = TrieSet.transientOf();
			for (IValue tuple : value) {
				convertedValue.insert((ITuple) tuple);
			}

			graphs.add(convertedValue.asImmutable());
		}

		return graphs;
	}

	private static ISet ImmutableSetOfMapsToSetOfMapValues(
			Set.Immutable<ImmutableMap<IConstructor, Set.Immutable<IConstructor>>> result) {
		// convert back to PDB for serialization
		IValueFactory vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();

		ISetWriter resultBuilder = vf.setWriter();

		for (ImmutableMap<IConstructor, Set.Immutable<IConstructor>> dominatorResult : result) {
			IMapWriter builder = vf.mapWriter();

			for (Map.Entry<IConstructor, Set.Immutable<IConstructor>> entry : dominatorResult.entrySet()) {
				builder.put(entry.getKey(), ImmutableSetToPdbSet(entry.getValue()));
			}

			resultBuilder.insert(builder.done());
		}

		return resultBuilder.done();
	}

	private static IMap immutableMapToPdbMap(ImmutableMap<IConstructor, Set.Immutable<IConstructor>> result) {
		// convert back to PDB for serialization
		IValueFactory vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();

		IMapWriter builder = vf.mapWriter();

		for (Map.Entry<IConstructor, Set.Immutable<IConstructor>> entry : result.entrySet()) {
			builder.put(entry.getKey(), ImmutableSetToPdbSet(entry.getValue()));
		}

		return builder.done();
	}

	private static <K extends IValue> ISet ImmutableSetToPdbSet(Set.Immutable<K> set) {
		IValueFactory vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();

		ISetWriter builder = vf.setWriter();

		for (K key : set) {
			builder.insert(key);
		}

		return builder.done();
	}

	private static Set.Immutable<ITuple> pdbSetToImmutableSet(ISet set) {
		Set.Transient<ITuple> builder = TrieSet.transientOf();

		for (IValue tuple : set) {
			builder.insert((ITuple) tuple);
		}

		return builder.asImmutable();
	}

	public static void assertDominatorsEqual() throws FileNotFoundException, IOException {
		IValueFactory vf = org.rascalmpl.value.impl.persistent.ValueFactory.getInstance();

		ISet dominatorsRascal = (ISet) new BinaryValueReader().read(vf,
				new FileInputStream("data/dominators-rascal.bin"));
		ISet dominatorsJava = (ISet) new BinaryValueReader().read(vf, new FileInputStream("data/dominators-java.bin"));

		if (!dominatorsRascal.equals(dominatorsJava)) {
			throw new Error("Dominator calculations do differ!");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void performBenchmark(Blackhole bh, ArrayList<?> sampledGraphsNative) {
		for (Set.Immutable<ITuple> graph : (ArrayList<Set.Immutable<ITuple>>) sampledGraphsNative) {
			try {
				bh.consume(new DominatorsSetMultimap_New().calculateDominators(graph));
			} catch (NoSuchElementException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	@Override
	public ArrayList<?> convertDataToNativeFormat(ArrayList<ISet> sampledGraphs) {
		// convert data to remove PDB dependency
		ArrayList<Set.Immutable<ITuple>> sampledGraphsNative = new ArrayList<>(sampledGraphs.size());

		for (ISet graph : sampledGraphs) {
			Set.Transient<ITuple> convertedValue = TrieSet.transientOf();

			for (IValue tuple : graph) {
				convertedValue.insert((ITuple) tuple);
			}

			sampledGraphsNative.add(convertedValue.asImmutable());
		}

		return sampledGraphsNative;
	}

}

class Util_New {

	@SuppressWarnings("rawtypes")
	public final static Set.Immutable EMPTY = TrieSet.of();	

	/*
	 * Intersect many sets.
	 */
	@SuppressWarnings("unchecked")
	public static <K> Set.Immutable<K> intersect(Set.Immutable<Set.Immutable<K>> sets) {
		if (sets == null || sets.isEmpty() || sets.contains(EMPTY)) {
			return EMPTY;
		}

		Set.Immutable<K> first = sets.iterator().next();
		sets = sets.remove(first);

		Set.Immutable<K> result = first;
		for (Set.Immutable<K> elem : sets) {
			result = Util_New.intersect(result, elem);
		}

		return result;
	}

	/*
	 * Intersect two sets.
	 */
	public static <K> Set.Immutable<K> intersect(Set.Immutable<K> set1, Set.Immutable<K> set2) {
		if (set1 == set2)
			return set1;
		if (set1 == null)
			return TrieSet.of();
		if (set2 == null)
			return TrieSet.of();

		final Set.Immutable<K> smaller;
		final Set.Immutable<K> bigger;

		final Set.Immutable<K> unmodified;

		if (set2.size() >= set1.size()) {
			unmodified = set1;
			smaller = set1;
			bigger = set2;
		} else {
			unmodified = set2;
			smaller = set2;
			bigger = set1;
		}

		final Set.Transient<K> tmp = smaller.asTransient();
		boolean modified = false;

		for (Iterator<K> it = tmp.iterator(); it.hasNext();) {
			final K key = it.next();
			if (!bigger.contains(key)) {
				it.remove();
				modified = true;
			}
		}

		if (modified) {
			return tmp.asImmutable();
		} else {
			return unmodified;
		}
	}

	/*
	 * Subtract one set from another.
	 */
	public static <K> Set.Immutable<K> subtract(Set.Immutable<K> set1, Set.Immutable<K> set2) {
		if (set1 == null && set2 == null)
			return TrieSet.of();
		if (set1 == set2)
			return TrieSet.of();
		if (set1 == null)
			return TrieSet.of();
		if (set2 == null)
			return set1;

		final Set.Transient<K> tmp = set1.asTransient();
		boolean modified = false;

		for (K key : set2) {
			if (tmp.remove(key)) {
				modified = true;
			}
		}

		if (modified) {
			return tmp.asImmutable();
		} else {
			return set1;
		}
	}

	/*
	 * Union two sets.
	 */
	public static <K> Set.Immutable<K> union(Set.Immutable<K> set1, Set.Immutable<K> set2) {
		if (set1 == null && set2 == null)
			return TrieSet.of();
		if (set1 == null)
			return set2;
		if (set2 == null)
			return set1;

		if (set1 == set2)
			return set1;

		final Set.Immutable<K> smaller;
		final Set.Immutable<K> bigger;

		final Set.Immutable<K> unmodified;

		if (set2.size() >= set1.size()) {
			unmodified = set2;
			smaller = set1;
			bigger = set2;
		} else {
			unmodified = set1;
			smaller = set2;
			bigger = set1;
		}

		final Set.Transient<K> tmp = bigger.asTransient();
		boolean modified = false;

		for (K key : smaller) {
			if (tmp.insert(key)) {
				modified = true;
			}
		}

		if (modified) {
			return tmp.asImmutable();
		} else {
			return unmodified;
		}
	}

	/*
	 * Flattening of a set (of ITuple elements). Because of the untyped nature
	 * of ITuple, the implementation is not strongly typed.
	 */
	@SuppressWarnings("unchecked")
	public static <K extends Iterable<?>, T> Set.Immutable<T> carrier(Set.Immutable<K> set1) {
		Set.Transient<Object> builder = TrieSet.transientOf();

		for (K iterable : set1) {
			for (Object nested : iterable) {
				builder.insert(nested);
			}
		}

		return (Set.Immutable<T>) builder.asImmutable();
	}

	/*
	 * Projection from a tuple to single field.
	 */
	@SuppressWarnings("unchecked")
	public static <K extends IValue> Set.Immutable<K> project(Set.Immutable<ITuple> set1, int field) {
		Set.Transient<K> builder = TrieSet.transientOf();

		for (ITuple tuple : set1) {
			builder.insert((K) tuple.select(field));
		}

		return builder.asImmutable();
	}

	/*
	 * Projection from a tuple to another tuple with (possible reordered) subset
	 * of fields.
	 */
	public static Set.Immutable<ITuple> project(Set.Immutable<ITuple> set1, int field1, int field2) {
		Set.Transient<ITuple> builder = TrieSet.transientOf();

		for (ITuple tuple : set1) {
			builder.insert((ITuple) tuple.select(field1, field2));
		}

		return builder.asImmutable();
	}

	/*
	 * Convert a set of tuples to a map; value in old map is associated with a
	 * set of keys in old map.
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> SetMultimap.Immutable<K, V> toMultimap(Set.Immutable<ITuple> st) {
		SetMultimap.Transient<K, V> mm = TrieSetMultimap.transientOf();

		for (ITuple t : st) {
			K key = (K) t.get(0);
			V val = (V) t.get(1);

			mm.insert(key, val);
		}

		return mm.asImmutable();
	}	
	
}
