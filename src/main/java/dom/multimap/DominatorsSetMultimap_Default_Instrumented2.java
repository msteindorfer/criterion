/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package dom.multimap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import dom.DominatorBenchmark;
import dom.JmhCfgDominatorBenchmarks;
import io.usethesource.capsule.DefaultTrieMap;
import io.usethesource.capsule.DefaultTrieSet;
import io.usethesource.capsule.SetMultimapFactory;
import io.usethesource.capsule.api.Set;
import io.usethesource.capsule.api.SetMultimap;
import io.usethesource.capsule.experimental.multimap.TrieSetMultimap_HHAMT_Specialized_Path_Interlinked;
import org.openjdk.jmh.infra.Blackhole;
import org.rascalmpl.interpreter.utils.Timing;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.IMapWriter;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISetWriter;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.io.old.BinaryValueReader;

import static dom.AllDominatorsRunner.DATA_SET_SINGLE_FILE_NAME;
import static dom.multimap.Util_Default_Instrumented2.EMPTY;
import static dom.multimap.Util_Default_Instrumented2.carrier;
import static dom.multimap.Util_Default_Instrumented2.intersect;
import static dom.multimap.Util_Default_Instrumented2.project;
import static dom.multimap.Util_Default_Instrumented2.subtract;
import static dom.multimap.Util_Default_Instrumented2.toMap;
import static dom.multimap.Util_Default_Instrumented2.toMultimap;
import static dom.multimap.Util_Default_Instrumented2.union;

public class DominatorsSetMultimap_Default_Instrumented2 implements DominatorBenchmark {

  private final SetMultimapFactory setMultimapFactory;

  public DominatorsSetMultimap_Default_Instrumented2(SetMultimapFactory setMultimapFactory) {
    this.setMultimapFactory = setMultimapFactory;
  }

  @SuppressWarnings("unchecked")
  private Set.Immutable<Set.Immutable<IConstructor>> setofdomsets(
      SetMultimap.Immutable<IConstructor, IConstructor> dom, Set.Immutable<IConstructor> preds) {
    Set.Transient<Set.Immutable<IConstructor>> result = DefaultTrieSet.transientOf();

    for (Object p : preds) {
      Set.Immutable<IConstructor> ps = dom.get(p);

      result.__insert(ps == null ? EMPTY : ps);
    }

    return result.freeze();
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

  @SuppressWarnings("unchecked")
  public SetMultimap.Immutable<IConstructor, IConstructor> calculateDominators(
      Set.Immutable<ITuple> graph) {

    IConstructor n0 = getTop(graph);
    Set.Immutable<IConstructor> nodes = carrier(graph);

    SetMultimap.Immutable<IConstructor, IConstructor> preds =
        toMultimap(setMultimapFactory, project(graph, 1, 0));

    {

      Predicate<Object> predicate = Predicates.not(Predicates.instanceOf(IValue.class));

      JmhCfgDominatorBenchmarks.memoryInBytes_multimap +=
          objectexplorer.MemoryMeasurer.measureBytes(preds, predicate);
      // Footprint memoryFootprint_multimap = objectexplorer.ObjectGraphMeasurer.measure(preds,
      // predicate);

      Object mapWithNestedSets = toMap(project(graph, 1, 0));

      JmhCfgDominatorBenchmarks.memoryInBytes_mapWithNestedSets +=
          objectexplorer.MemoryMeasurer.measureBytes(mapWithNestedSets, predicate);
      // Footprint memoryFootprint_mapWithNestedSets =
      // objectexplorer.ObjectGraphMeasurer.measure(mapWithNestedSets, predicate);
    }

    SetMultimap.Transient<IConstructor, IConstructor> w = setMultimapFactory.transientOf();
    w.__insert(n0, n0);
    for (IConstructor n : nodes.__remove(n0)) {
      w.__put(n, nodes);
    }
    SetMultimap.Immutable<IConstructor, IConstructor> dom = w.freeze();

    SetMultimap.Immutable<IConstructor, IConstructor> prev = setMultimapFactory.of();

    /*
     * solve (dom) for (n <- nodes) dom[n] = {n} + intersect({dom[p] | p <- preds[n]?{}});
     */
    while (!prev.equals(dom)) {
      prev = dom;

      SetMultimap.Transient<IConstructor, IConstructor> newDom = setMultimapFactory.transientOf();

      for (IConstructor n : nodes) {
        Set.Immutable<IConstructor> ps = preds.get(n);
        if (ps == null) {
          ps = EMPTY;
        }
        Set.Immutable<Set.Immutable<IConstructor>> sos = setofdomsets(dom, ps);
        Set.Immutable<IConstructor> intersected = intersect(sos);

        if (!intersected.isEmpty()) {
          Set.Immutable<IConstructor> newValue = union(intersected, DefaultTrieSet.of(n));
          newDom.__put(n, newValue);
        } else {
          newDom.__insert(n, n);
        }
      }

      dom = newDom.freeze();
    }

    return dom;
  }

  private static SetMultimapFactory DEFAULT_SET_MULTIMAP_FACTORY =
      new SetMultimapFactory(TrieSetMultimap_HHAMT_Specialized_Path_Interlinked.class);

  public static void main(String[] args) throws FileNotFoundException, IOException {
    testOne();
    // assertDominatorsEqual();
  }

  public static IMap testOne() throws IOException, FileNotFoundException {
    IValueFactory vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance();

    ISet data =
        (ISet) new BinaryValueReader().read(vf, new FileInputStream(DATA_SET_SINGLE_FILE_NAME));

    // convert data to remove PDB dependency
    Set.Immutable<ITuple> graph = pdbSetToImmutableSet(data);

    long before = Timing.getCpuTime();
    SetMultimap.Immutable<IConstructor, IConstructor> results =
        new DominatorsSetMultimap_Default_Instrumented2(DEFAULT_SET_MULTIMAP_FACTORY)
            .calculateDominators(graph);
    System.err.println("PDB_LESS_IMPLEMENTATION" + "\nDuration: "
        + ((Timing.getCpuTime() - before) / 1000000000) + " seconds\n");

    // IMap pdbResults = immutableMapToPdbMap(results);
    //
    // if (LOG_BINARY_RESULTS)
    // new BinaryValueWriter().write(pdbResults,
    // new FileOutputStream("data/dominators-java-without-pdb-single.bin"));
    //
    // if (LOG_TEXTUAL_RESULTS)
    // new StandardTextWriter().write(pdbResults, new
    // FileWriter("data/dominators-java-without-pdb-single.txt"));
    //
    // return pdbResults;

    return null;
  }

  public static ISet testAll(IMap sampledGraphs) throws IOException, FileNotFoundException {
    // convert data to remove PDB dependency
    ArrayList<Set.Immutable<ITuple>> graphs = pdbMapToArrayListOfValues(sampledGraphs);

    Set.Transient<SetMultimap.Immutable<IConstructor, IConstructor>> result =
        DefaultTrieSet.transientOf();
    long before = Timing.getCpuTime();
    for (Set.Immutable<ITuple> graph : graphs) {
      try {
        result
            .__insert(new DominatorsSetMultimap_Default_Instrumented2(DEFAULT_SET_MULTIMAP_FACTORY)
                .calculateDominators(graph));
      } catch (RuntimeException e) {
        System.err.println(e.getMessage());
      }
    }
    System.err.println("PDB_LESS_IMPLEMENTATION" + "\nDuration: "
        + ((Timing.getCpuTime() - before) / 1000000000) + " seconds\n");

    // // convert back to PDB for serialization
    // ISet pdbResults = immutableSetOfMapsToSetOfMapValues(result.freeze());
    //
    // if (LOG_BINARY_RESULTS)
    // new BinaryValueWriter().write(pdbResults, new FileOutputStream("data/dominators-java.bin"));
    //
    // if (LOG_TEXTUAL_RESULTS)
    // new StandardTextWriter().write(pdbResults, new
    // FileWriter("data/dominators-java-without-pdb.txt"));
    //
    // return pdbResults;

    return null;
  }

  private static ArrayList<Set.Immutable<ITuple>> pdbMapToArrayListOfValues(IMap data) {
    // convert data to remove PDB dependency
    ArrayList<Set.Immutable<ITuple>> graphs = new ArrayList<>(data.size());
    for (IValue key : data) {
      ISet value = (ISet) data.get(key);

      Set.Transient<ITuple> convertedValue = DefaultTrieSet.transientOf();
      for (IValue tuple : value) {
        convertedValue.__insert((ITuple) tuple);
      }

      graphs.add(convertedValue.freeze());
    }

    return graphs;
  }

  private static ISet immutableSetOfMapsToSetOfMapValues(
      Set.Immutable<io.usethesource.capsule.api.Map.Immutable<IConstructor, Set.Immutable<IConstructor>>> result) {
    // convert back to PDB for serialization
    IValueFactory vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance();

    ISetWriter resultBuilder = vf.setWriter();

    for (io.usethesource.capsule.api.Map.Immutable<IConstructor, Set.Immutable<IConstructor>> dominatorResult : result) {
      IMapWriter builder = vf.mapWriter();

      for (Map.Entry<IConstructor, Set.Immutable<IConstructor>> entry : dominatorResult.entrySet()) {
        builder.put(entry.getKey(), immutableSetToPdbSet(entry.getValue()));
      }

      resultBuilder.insert(builder.done());
    }

    return resultBuilder.done();
  }

  private static IMap immutableMapToPdbMap(
      io.usethesource.capsule.api.Map.Immutable<IConstructor, Set.Immutable<IConstructor>> result) {
    // convert back to PDB for serialization
    IValueFactory vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance();

    IMapWriter builder = vf.mapWriter();

    for (Map.Entry<IConstructor, Set.Immutable<IConstructor>> entry : result.entrySet()) {
      builder.put(entry.getKey(), immutableSetToPdbSet(entry.getValue()));
    }

    return builder.done();
  }

  private static <K extends IValue> ISet immutableSetToPdbSet(Set.Immutable<K> set) {
    IValueFactory vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance();

    ISetWriter builder = vf.setWriter();

    for (K key : set) {
      builder.insert(key);
    }

    return builder.done();
  }

  private static Set.Immutable<ITuple> pdbSetToImmutableSet(ISet set) {
    Set.Transient<ITuple> builder = DefaultTrieSet.transientOf();

    for (IValue tuple : set) {
      builder.__insert((ITuple) tuple);
    }

    return builder.freeze();
  }

  public static void assertDominatorsEqual() throws FileNotFoundException, IOException {
    IValueFactory vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance();

    ISet dominatorsRascal =
        (ISet) new BinaryValueReader().read(vf, new FileInputStream("data/dominators-rascal.bin"));
    ISet dominatorsJava =
        (ISet) new BinaryValueReader().read(vf, new FileInputStream("data/dominators-java.bin"));

    if (!dominatorsRascal.equals(dominatorsJava)) {
      throw new Error("Dominator calculations do differ!");
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void performBenchmark(Blackhole bh, ArrayList<?> sampledGraphsNative) {
    for (Set.Immutable<ITuple> graph : (ArrayList<Set.Immutable<ITuple>>) sampledGraphsNative) {
      try {
        bh.consume(new DominatorsSetMultimap_Default_Instrumented2(setMultimapFactory)
            .calculateDominators(graph));
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
      Set.Transient<ITuple> convertedValue = DefaultTrieSet.transientOf();

      for (IValue tuple : graph) {
        convertedValue.__insert((ITuple) tuple);
      }

      sampledGraphsNative.add(convertedValue.freeze());
    }

    return sampledGraphsNative;
  }

}


class Util_Default_Instrumented2 {

  @SuppressWarnings("rawtypes")
  public final static Set.Immutable EMPTY = DefaultTrieSet.of();

  /*
   * Intersect many sets.
   */
  @SuppressWarnings("unchecked")
  public static <K> Set.Immutable<K> intersect(Set.Immutable<Set.Immutable<K>> sets) {
    if (sets == null || sets.isEmpty() || sets.contains(EMPTY)) {
      return EMPTY;
    }

    Set.Immutable<K> first = sets.iterator().next();
    sets = sets.__remove(first);

    Set.Immutable<K> result = first;
    for (Set.Immutable<K> elem : sets) {
      result = Util_Default_Instrumented2.intersect(result, elem);
    }

    return result;
  }

  /*
   * Intersect two sets.
   */
  public static <K> Set.Immutable<K> intersect(Set.Immutable<K> set1, Set.Immutable<K> set2) {
    if (set1 == set2) {
      return set1;
    }
    if (set1 == null) {
      return DefaultTrieSet.of();
    }
    if (set2 == null) {
      return DefaultTrieSet.of();
    }

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
      return tmp.freeze();
    } else {
      return unmodified;
    }
  }

  /*
   * Subtract one set from another.
   */
  public static <K> Set.Immutable<K> subtract(Set.Immutable<K> set1, Set.Immutable<K> set2) {
    if (set1 == null && set2 == null) {
      return DefaultTrieSet.of();
    }
    if (set1 == set2) {
      return DefaultTrieSet.of();
    }
    if (set1 == null) {
      return DefaultTrieSet.of();
    }
    if (set2 == null) {
      return set1;
    }

    final Set.Transient<K> tmp = set1.asTransient();
    boolean modified = false;

    for (K key : set2) {
      if (tmp.__remove(key)) {
        modified = true;
      }
    }

    if (modified) {
      return tmp.freeze();
    } else {
      return set1;
    }
  }

  /*
   * Union two sets.
   */
  public static <K> Set.Immutable<K> union(Set.Immutable<K> set1, Set.Immutable<K> set2) {
    if (set1 == null && set2 == null) {
      return DefaultTrieSet.of();
    }
    if (set1 == null) {
      return set2;
    }
    if (set2 == null) {
      return set1;
    }

    if (set1 == set2) {
      return set1;
    }

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
      if (tmp.__insert(key)) {
        modified = true;
      }
    }

    if (modified) {
      return tmp.freeze();
    } else {
      return unmodified;
    }
  }

  /*
   * Flattening of a set (of ITuple elements). Because of the untyped nature of ITuple, the
   * implementation is not strongly typed.
   */
  @SuppressWarnings("unchecked")
  public static <K extends Iterable<?>, T> Set.Immutable<T> carrier(Set.Immutable<K> set1) {
    Set.Transient<Object> builder = DefaultTrieSet.transientOf();

    for (K iterable : set1) {
      for (Object nested : iterable) {
        builder.__insert(nested);
      }
    }

    return (Set.Immutable<T>) builder.freeze();
  }

  /*
   * Projection from a tuple to single field.
   */
  @SuppressWarnings("unchecked")
  public static <K extends IValue> Set.Immutable<K> project(Set.Immutable<ITuple> set1, int field) {
    Set.Transient<K> builder = DefaultTrieSet.transientOf();

    for (ITuple tuple : set1) {
      builder.__insert((K) tuple.select(field));
    }

    return builder.freeze();
  }

  /*
   * Projection from a tuple to another tuple with (possible reordered) subset of fields.
   */
  public static Set.Immutable<ITuple> project(Set.Immutable<ITuple> set1, int field1, int field2) {
    Set.Transient<ITuple> builder = DefaultTrieSet.transientOf();

    for (ITuple tuple : set1) {
      builder.__insert((ITuple) tuple.select(field1, field2));
    }

    return builder.freeze();
  }

  /*
   * Convert a set of tuples to a map; value in old map is associated with a set of keys in old map.
   */
  @SuppressWarnings("unchecked")
  public static <K, V> SetMultimap.Immutable<K, V> toMultimap(SetMultimapFactory setMultimapFactory,
                                                              Set.Immutable<ITuple> st) {
    SetMultimap.Transient<K, V> mm = setMultimapFactory.transientOf();

    for (ITuple t : st) {
      K key = (K) t.get(0);
      V val = (V) t.get(1);

      mm.__insert(key, val);
    }

    return mm.freeze();
  }

  /*
   * Convert a set of tuples to a map; value in old map is associated with a set of keys in old map.
   */
  @SuppressWarnings("unchecked")
  public static <K, V> io.usethesource.capsule.api.Map.Immutable<K, Set.Immutable<V>> toMap(Set.Immutable<ITuple> st) {
    Map<K, Set.Transient<V>> hm = new HashMap<>();

    for (ITuple t : st) {
      K key = (K) t.get(0);
      V val = (V) t.get(1);
      Set.Transient<V> wValSet = hm.get(key);
      if (wValSet == null) {
        wValSet = DefaultTrieSet.transientOf();
        hm.put(key, wValSet);
      }
      wValSet.__insert(val);
    }

    io.usethesource.capsule.api.Map.Transient<K, Set.Immutable<V>> w = DefaultTrieMap.transientOf();
    for (K k : hm.keySet()) {
      w.__put(k, hm.get(k).freeze());
    }
    return w.freeze();
  }

}
