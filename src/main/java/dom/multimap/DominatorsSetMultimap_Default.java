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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

import dom.DominatorBenchmark;
import io.usethesource.capsule.Set;
import io.usethesource.capsule.Set.Immutable;
import io.usethesource.capsule.Set.Transient;
import io.usethesource.capsule.SetMultimap;
import io.usethesource.capsule.SetMultimapFactory;
import io.usethesource.capsule.experimental.multimap.TrieSetMultimap_HHAMT;
import io.usethesource.vallang.IAnnotatable;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.IMapWriter;
import io.usethesource.vallang.INode;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISetWriter;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.IWithKeywordParameters;
import io.usethesource.vallang.exceptions.FactTypeUseException;
import io.usethesource.vallang.io.old.BinaryValueReader;
import io.usethesource.vallang.type.Type;
import io.usethesource.vallang.type.TypeFactory;
import io.usethesource.vallang.type.TypeStore;
import io.usethesource.vallang.visitors.IValueVisitor;
import org.openjdk.jmh.infra.Blackhole;
import org.rascalmpl.interpreter.utils.Timing;

import static dom.AllDominatorsRunner.DATA_SET_SINGLE_FILE_NAME;
import static dom.multimap.Util_Default.EMPTY;
import static dom.multimap.Util_Default.carrier;
import static dom.multimap.Util_Default.intersect;
import static dom.multimap.Util_Default.printStatistics;
import static dom.multimap.Util_Default.project;
import static dom.multimap.Util_Default.resetStatistics;
import static dom.multimap.Util_Default.toMultimap;

public class DominatorsSetMultimap_Default implements DominatorBenchmark {

  private final SetMultimapFactory setMultimapFactory;

  public DominatorsSetMultimap_Default(SetMultimapFactory setMultimapFactory) {
    this.setMultimapFactory = setMultimapFactory;
  }

  private <T extends IConstructor> Set.Immutable<Set.Immutable<T>> setOfDominatorSets(
      final Immutable<T> preds, final SetMultimap.Immutable<T, T> dom) {

    final Set.Transient<Set.Immutable<T>> builder = Transient.of();

    for (Object p : preds) {
      Set.Immutable<T> ps = dom.get(p);

      builder.__insert(ps == null ? EMPTY : ps);
    }

    return builder.freeze();
  }

  public <T extends IConstructor> Set.Immutable<T> topCandidates(
      final SetMultimap.Immutable<T, T> graph) {
    return project(graph, 0).subtract(project(graph, 1));

//    java.util.Set<T> allL = (graph.keySet());
//    java.util.Set<T> allR = (graph.inverseMap().keySet());
//
//    java.util.Set<T> uniqueL = new HashSet<>(allL);
//    uniqueL.removeAll(allR);
//
//    java.util.Set<T> uniqueR = new HashSet<>(allR);
//    uniqueR.removeAll(allL);
//
//    T entryNode = uniqueL.stream().findAny().get();
//    T exitNode = uniqueR.stream().findAny().get();
  }

  public <T extends IConstructor> T getTop(SetMultimap.Immutable<T, T> graph) {
//    final IValueFactory vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance();
//    final TypeFactory tf = TypeFactory.getInstance();
//
//    T i1 = (T) vf.constructor(tf.integerType(), vf.integer(1));
//
//    return i1;

    final Set.Immutable<T> candidates = topCandidates(graph);

    if (candidates.size() > 1) {
    }

    for (T candidate : candidates) {
      switch (candidate.getName()) {
        case "methodEntry":
        case "functionEntry":
        case "scriptEntry":
          return candidate;
      }
    }

    throw new NoSuchElementException("No candidate found.");
  }

  public static final <K, V> SetMultimap.Transient<K, Set.Immutable<V>> join(
      final SetMultimap.Immutable<K, V> left,
      final SetMultimap.Immutable<K, V> right) {
    return null;
  }

  public static final <K, V> SetMultimap.Transient<K, V> reduceValues(
      final SetMultimap.Transient<K, Set.Immutable<V>> joinedMap,
      final BiFunction<K, Set.Immutable<Set.Immutable<V>>, Set.Immutable<V>> reducer) {
    return null;
  }

  public <T extends IConstructor> SetMultimap.Immutable<T, T> calculateDominators(
      final SetMultimap.Immutable<T, T> graph) {

    final T entryNode = getTop(graph);
    final Set.Immutable<T> graphNodes = carrier(graph);
    final SetMultimap.Immutable<T, T> predecessorGraph = graph.inverseMap();

    SetMultimap.Immutable<T, T> domPrevious = setMultimapFactory.of();
    SetMultimap.Immutable<T, T> domCandidate = initializeDominatorRelationship(graphNodes,
        entryNode, topCandidates(graph));

    boolean areEqual = domPrevious.equals(domCandidate);

    while (!areEqual) {
      domPrevious = domCandidate;

      final SetMultimap.Transient<T, T> domBuilder = setMultimapFactory.transientOf();

      for (T node : graphNodes) {
        final Set.Immutable<T> nodePredecessors = predecessorGraph.get(node);

        final Set.Immutable<Set.Immutable<T>> setOfDominatorSets =
            setOfDominatorSets(nodePredecessors, domCandidate);

        final Set.Immutable<T> dominators = intersect(setOfDominatorSets);
        // printStatistics();

        domBuilder.__insert(node, dominators);
        domBuilder.__insert(node, node);

//        domBuilder.__insert(node, union(dominators, Set.Immutable.of(node)));
//        domBuilder.__insert(node, dominators.union(Set.Immutable.of(node)));

//        domBuilder.__insert(node, dominators.__insert(node));
      }

      domCandidate = domBuilder.freeze();

//      Timing timing = new Timing();
//      timing.start();
      areEqual = domPrevious.equals(domCandidate);
//      totalTimeEqualsAfterIntersect1 += timing.duration() / 1_000;
    }

    // printStatistics();
    final SetMultimap.Immutable<T, T> isDominatedBy = domCandidate;

//    final int dominatedNodesByEntryNodeCount = isDominatedBy.inverseMap().get(entryNode).size();
//    final int dominatedNodesCount = isDominatedBy.keySet().size();
//    assert dominatedNodesByEntryNodeCount == dominatedNodesCount;

    return isDominatedBy;
  }

  private <T extends IConstructor> SetMultimap.Immutable<T, T> initializeDominatorRelationship(
      final Set.Immutable<T> graphNodes, T entryNode, Set.Immutable<T> topCandidates) {

    final SetMultimap.Transient<T, T> builder = setMultimapFactory.transientOf();

//    final Set.Immutable<T> remainingNodes = graphNodes.__remove(entryNode);
//
//    builder.__insert(entryNode, entryNode);
//    remainingNodes.forEach(node -> builder.__insert(node, graphNodes));

//    final Set.Immutable<T> remainingNodes = graphNodes.__removeAll(topCandidates);
//
//    topCandidates.forEach(top -> builder.__insert(top, top));
//    remainingNodes.forEach(node -> builder.__insert(node, graphNodes));

    final Set.Immutable<T> remainingNodes = graphNodes.__removeAll(topCandidates);

    builder.__insert(entryNode, entryNode);
    remainingNodes.forEach(node -> builder.__insert(node, graphNodes));

    return builder.freeze();
  }

  public <T extends IConstructor> SetMultimap.Immutable<T, T> calculateDominatorsWithJoin(
      final SetMultimap.Immutable<T, T> graph) {

    final T entryNode = getTop(graph);
    final Set.Immutable<T> topCandidates = topCandidates(graph);

    final Set.Immutable<T> graphNodes = carrier(graph);
    final SetMultimap.Immutable<T, T> predecessorGraph = graph.inverseMap();

//    final SetMultimap.Transient<T, T> tmpGraph = graph.asTransient();
//    topCandidates.__remove(entryNode).forEach(tmpGraph::__remove);
//    final SetMultimap.Immutable<T, T> predecessorGraph = tmpGraph.freeze().inverseMap();

//    ////////////////
//    final SetMultimap.Transient<T, T> tmp = predecessorGraph.asTransient();
//    topCandidates(graph).stream().forEach(key -> tmp.__put(key, key));
//    final SetMultimap.Immutable<T, T> predecessorWithIdentityOfTopCandidates = tmp.freeze();
//    ////////////////

    SetMultimap.Immutable<T, T> domPrevious = setMultimapFactory.of();
    SetMultimap.Immutable<T, T> domCandidate = initializeDominatorRelationship(graphNodes,
        entryNode, topCandidates);

    while (!domPrevious.equals(domCandidate)) {
      domPrevious = domCandidate;

      domCandidate = TrieSetMultimap_HHAMT
          .joinReduce(
              (TrieSetMultimap_HHAMT) predecessorGraph.__insert(entryNode, entryNode),
              // predecessorGraph.__insert(entryNode, entryNode)
              (TrieSetMultimap_HHAMT) domPrevious);
    }

    // add identity to result
//    final SetMultimap.Transient<T, T> builder = domCandidate.asTransient();
//    graphNodes.stream().forEach(node -> builder.__insert(node, node));
//    final SetMultimap.Immutable<T, T> isDominatedBy = builder.freeze();

    final SetMultimap.Immutable<T, T> isDominatedBy = domCandidate;

//    final int dominatedNodesByEntryNodeCount = isDominatedBy.inverseMap().get(entryNode).size();
//    final int dominatedNodesCount = isDominatedBy.keySet().size();
//    assert dominatedNodesByEntryNodeCount == dominatedNodesCount;

    return isDominatedBy;
  }

//  public SetMultimap.Immutable<IConstructor, IConstructor> calculateDominatorsWithJoin(
//      SetMultimap.Immutable<IConstructor, IConstructor> graph) {
//
//    IConstructor n0 = getTop(graph);
//    Set.Immutable<IConstructor> nodes = carrier(graph);
//
//    SetMultimap.Immutable<IConstructor, IConstructor> preds = inverse(setMultimapFactory, graph);
//
//    SetMultimap.Transient<IConstructor, IConstructor> w = setMultimapFactory.transientOf();
//
//    w.__insert(n0, n0);
//    for (IConstructor n : nodes.__remove(n0)) {
//      w.__insert(n, nodes);
//    }
//
//    SetMultimap.Immutable<IConstructor, IConstructor> dom = w.freeze();
//
//    final SetMultimap.Transient<IConstructor, IConstructor> tmpPredsPlusIdentity = preds
//        .asTransient();
//    nodes.forEach(n -> tmpPredsPlusIdentity.__insert(n, n));
//    SetMultimap.Immutable<IConstructor, IConstructor> predPlusIdentity = tmpPredsPlusIdentity
//        .freeze();
//
//    SetMultimap.Immutable<IConstructor, IConstructor> prev = setMultimapFactory.of();
//
//    while (!prev.equals(dom)) {
//      prev = dom;
//
////      final SetMultimap.Transient<IConstructor, IConstructor> newDom =
////          reduceValues(join(preds, dom),
////              (node, setOfSets) -> union(intersect(setOfSets), Immutable.of(node)));
////
////      dom = newDom.freeze();
//
//      final SetMultimap.Immutable<IConstructor, IConstructor> domReference = dom;
//
//      dom = predPlusIdentity.<Set.Immutable<Set.Immutable<IConstructor>>>joinReduce(
//          (key, value) -> Optional.ofNullable(domReference.get(value)).orElse(EMPTY),
//          (node, setOfSets) -> union(intersect(setOfSets), Immutable.of(node)));
//    }
//
//    return dom;
//  }

  private static SetMultimapFactory DEFAULT_SET_MULTIMAP_FACTORY =
      new SetMultimapFactory(TrieSetMultimap_HHAMT.class);

  public static void main(String[] args) throws FileNotFoundException, IOException {
    final IValueFactory vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance();
    final TypeFactory tf = TypeFactory.getInstance();

    IValue i1 = new ConstructorDummy(1);
    IValue i2 = new ConstructorDummy(2);
    IValue i3 = new ConstructorDummy(3);
    IValue i4 = new ConstructorDummy(4);
    IValue i5 = new ConstructorDummy(5);
    IValue i6 = new ConstructorDummy(6);

    IValue i1_1 = new ConstructorDummy(1, 1);

    final SetMultimap.Transient graphBuilder = TrieSetMultimap_HHAMT.transientOf();
    graphBuilder.__insert(i1, i2);
    graphBuilder.__insert(i2, i3);
    graphBuilder.__insert(i2, i4);
    graphBuilder.__insert(i2, i6);
    graphBuilder.__insert(i3, i5);
    graphBuilder.__insert(i4, i5);
    graphBuilder.__insert(i5, i2);

    graphBuilder.__insert(i1_1, i2);

    final SetMultimap.Immutable testInput = graphBuilder.freeze();

    new DominatorsSetMultimap_Default(DEFAULT_SET_MULTIMAP_FACTORY).calculateDominators(testInput);
  }

  public static IMap testOne() throws IOException, FileNotFoundException {
    IValueFactory vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance();

    ISet data =
        (ISet) new BinaryValueReader().read(vf, new FileInputStream(DATA_SET_SINGLE_FILE_NAME));

    // convert data to remove PDB dependency
    Set.Immutable<ITuple> graph = pdbSetToImmutableSet(data);
    SetMultimap.Immutable<IConstructor, IConstructor> mm = toMultimap(DEFAULT_SET_MULTIMAP_FACTORY,
        graph);

    long before = Timing.getCpuTime();
    SetMultimap.Immutable<IConstructor, IConstructor> results =
        new DominatorsSetMultimap_Default(DEFAULT_SET_MULTIMAP_FACTORY).calculateDominators(mm);
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
        Transient.of();
    long before = Timing.getCpuTime();
    for (Set.Immutable<ITuple> graph : graphs) {
      try {
        result.__insert(new DominatorsSetMultimap_Default(DEFAULT_SET_MULTIMAP_FACTORY)
            .calculateDominators(toMultimap(DEFAULT_SET_MULTIMAP_FACTORY, graph)));
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

      Set.Transient<ITuple> convertedValue = Transient.of();
      for (IValue tuple : value) {
        convertedValue.__insert((ITuple) tuple);
      }

      graphs.add(convertedValue.freeze());
    }

    return graphs;
  }

  private static ISet immutableSetOfMapsToSetOfMapValues(
      Set.Immutable<io.usethesource.capsule.Map.Immutable<IConstructor, Set.Immutable<IConstructor>>> result) {
    // convert back to PDB for serialization
    IValueFactory vf = io.usethesource.vallang.impl.persistent.ValueFactory.getInstance();

    ISetWriter resultBuilder = vf.setWriter();

    for (io.usethesource.capsule.Map.Immutable<IConstructor, Set.Immutable<IConstructor>> dominatorResult : result) {
      IMapWriter builder = vf.mapWriter();

      for (Map.Entry<IConstructor, Set.Immutable<IConstructor>> entry : dominatorResult
          .entrySet()) {
        builder.put(entry.getKey(), immutableSetToPdbSet(entry.getValue()));
      }

      resultBuilder.insert(builder.done());
    }

    return resultBuilder.done();
  }

  private static IMap immutableMapToPdbMap(
      io.usethesource.capsule.Map.Immutable<IConstructor, Set.Immutable<IConstructor>> result) {
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
    Set.Transient<ITuple> builder = Transient.of();

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

  @Override
  public void performBenchmark(Blackhole bh, java.util.List<?> sampledGraphsNative) {
    resetStatistics();

    for (SetMultimap.Immutable<IConstructor, IConstructor> graph : (List<SetMultimap.Immutable<IConstructor, IConstructor>>) sampledGraphsNative) {
      try {
//        final SetMultimap.Immutable<IConstructor, IConstructor> dom0 =
//            new DominatorsSetMultimap_Default(setMultimapFactory)
//                .calculateDominators(graph);
//        bh.consume(dom0);

        final SetMultimap.Immutable<IConstructor, IConstructor> dom1 =
            new DominatorsSetMultimap_Default(setMultimapFactory)
                .calculateDominatorsWithJoin(graph);
        bh.consume(dom1);

//        if (!dom0.equals(dom1)) {
//          final SetMultimap.Immutable<IConstructor, IConstructor> onlyInDom0 = dom1.inverseMap()
//              .complement(dom0.inverseMap());
//          final SetMultimap.Immutable<IConstructor, IConstructor> onlyInDom1 = dom0.inverseMap()
//              .complement(dom1.inverseMap());
//
//          // throw new IllegalStateException("Dominator results differ.");
//
//          final Set.Immutable<IConstructor> dom0KeySet = dom0.keySet().stream()
//              .collect(CapsuleCollectors.toSet());
//
//          final Set.Immutable<IConstructor> dom1KeySet = dom1.keySet().stream()
//              .collect(CapsuleCollectors.toSet());
//
//          final Set.Immutable<IConstructor> onlyInDom0KeySet = onlyInDom0.keySet().stream()
//              .collect(CapsuleCollectors.toSet());
//
//          final Set.Immutable<IConstructor> onlyInDom1KeySet = onlyInDom1.keySet().stream()
//              .collect(CapsuleCollectors.toSet());
//
//          if (!dom0KeySet.intersect(onlyInDom1KeySet).isEmpty()) {
//            throw new IllegalStateException("Key set intersects with diff key set.");
//          }
//
//          if (!dom1KeySet.intersect(onlyInDom0KeySet).isEmpty()) {
//            throw new IllegalStateException("Key set intersects with diff key set.");
//          }
//
//          final SetMultimap.Transient<IConstructor, IConstructor> tmpDom0 = dom0.inverseMap()
//              .asTransient();
//          onlyInDom0KeySet.forEach(tmpDom0::__remove);
//
//          final SetMultimap.Transient<IConstructor, IConstructor> tmpDom1 = dom1.inverseMap()
//              .asTransient();
//          onlyInDom1KeySet.forEach(tmpDom1::__remove);
//
//          if (!tmpDom0.equals(tmpDom1)) {
//            throw new IllegalStateException("Processed dominator results differ.");
//          }
//        }

      } catch (NoSuchElementException e) {
        System.err.println(e.getMessage());
      }
    }

    printStatistics();
  }

  @Override
  public ArrayList<?> convertDataToNativeFormat(java.util.List<ISet> sampledGraphs) {
    // convert data to remove PDB dependency
    ArrayList<SetMultimap.Immutable<IConstructor, IConstructor>> sampledGraphsNative = new ArrayList<>(
        sampledGraphs.size());

    for (ISet graph : sampledGraphs) {
      SetMultimap.Transient<IConstructor, IConstructor> convertedValue = setMultimapFactory
          .transientOf();

      for (IValue value : graph) {
        ITuple tuple = (ITuple) value;
        convertedValue.__insert((IConstructor) tuple.get(0), (IConstructor) tuple.get(1));
      }

      sampledGraphsNative.add(convertedValue.freeze());
    }

    return sampledGraphsNative;
  }

}


class Util_Default {

  public final static Set.Immutable EMPTY = Immutable.of();

  private static long totalTimeIntersect0 = 0;
  private static long totalTimeIntersect1 = 0;

  public static long totalTimeEqualsAfterIntersect0 = 0;
  public static long totalTimeEqualsAfterIntersect1 = 0;

  public static void resetStatistics() {
    totalTimeIntersect0 = 0;
    totalTimeIntersect1 = 0;
    totalTimeEqualsAfterIntersect0 = 0;
    totalTimeEqualsAfterIntersect1 = 0;
  }

  public static void printStatistics() {
//    if ((totalTimeIntersect0 > 10 || totalTimeIntersect1 > 10)
//        && totalTimeIntersect0 < totalTimeIntersect1) {
//    System.out.println(
//        String.format("Function >> %d (ms) ? %d (ms) << Method", totalTimeIntersect0,
//            totalTimeIntersect1));

    System.out.println(
        String.format("Equal >> %d (ms)", totalTimeEqualsAfterIntersect1));
//    }
  }

  /*
   * Intersect many sets.
   */
  public static <K> Set.Immutable<K> intersect(Set.Immutable<Set.Immutable<K>> sets) {
    if (sets == null || sets.isEmpty() || sets.contains(EMPTY)) {
      return EMPTY;
    }

    Set.Immutable<K> first = sets.findFirst().get();
    sets = sets.__remove(first);

//    Set.Immutable<K> result = first;
//    for (Set.Immutable<K> elem : sets) {
////      result = intersect(result, elem);
//      result = result.intersect(elem);
//    }

    Set.Immutable<K> result0 = first;
//    Set.Immutable<K> result1 = first;
    final Iterator<Set.Immutable<K>> it = sets.iterator();

    while (it.hasNext()) { // !result0.isEmpty() &&
      Set.Immutable<K> elem = it.next();

//      Timing timing0 = new Timing();
//      timing0.start();
      result0 = Set.Immutable.intersect(result0, elem);
//      totalTimeIntersect0 += timing0.duration() / 1_000;

////      Timing timing1 = new Timing();
////      timing1.start();
//      result1 = result1.intersect(elem);
////      totalTimeIntersect1 += timing1.duration() / 1_000;

    }

    return result0;
//    return result1;
  }

  /*
   * Intersect two sets.
   */
  public static <K> Set.Immutable<K> intersect(Set.Immutable<K> set1, Set.Immutable<K> set2) {
    if (set1 == set2) {
      return set1;
    }
    if (set1 == null) {
      return Immutable.of();
    }
    if (set2 == null) {
      return Immutable.of();
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

    for (Iterator<K> it = tmp.iterator(); it.hasNext(); ) {
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
  public static <T extends IConstructor> Set.Immutable<T> subtract(final Set.Immutable<T> set1,
      final Set.Immutable<T> set2) {

    if (set1 == null && set2 == null) {
      return Set.Immutable.of();
    }
    if (set1 == set2) {
      return Set.Immutable.of();
    }
    if (set1 == null) {
      return Set.Immutable.of();
    }
    if (set2 == null) {
      return set1;
    }

    final Set.Transient<T> tmp = set1.asTransient();
    boolean modified = false;

    for (T key : set2) {
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

//  /*
//   * Subtract one set from another.
//   */
//  public static <K, V> SetMultimap.Immutable<K, V> subtract(SetMultimap.Immutable<K, V> set1,
//      SetMultimap.Immutable<K, V> set2) {
//    if (set1 == null && set2 == null) {
//      return SetMultimap.Immutable.of();
//    }
//    if (set1 == set2) {
//      return SetMultimap.Immutable.of();
//    }
//    if (set1 == null) {
//      return SetMultimap.Immutable.of();
//    }
//    if (set2 == null) {
//      return set1;
//    }
//
//    final SetMultimap.Transient<K, V> tmp = set1.asTransient();
//    boolean modified = false;
//
//    for (Map.Entry<K, V> tuple : set2.entrySet()) {
//      if (tmp.__remove(tuple.getKey(), tuple.getValue()))){
//        modified = false;
//      }
//    }
//
//    if (modified) {
//      return tmp.freeze();
//    } else {
//      return set1;
//    }
//  }

  /*
   * Union two sets.
   */
  public static <T> Set.Immutable<T> union(final Set.Immutable<T> set1,
      final Set.Immutable<T> set2) {

    if (set1 == null && set2 == null) {
      return Immutable.of();
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

    final Set.Immutable<T> smaller;
    final Set.Immutable<T> bigger;

    final Set.Immutable<T> unmodified;

    if (set2.size() >= set1.size()) {
      unmodified = set2;
      smaller = set1;
      bigger = set2;
    } else {
      unmodified = set1;
      smaller = set2;
      bigger = set1;
    }

    final Set.Transient<T> tmp = bigger.asTransient();
    boolean modified = false;

    for (T key : smaller) {
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
   * Merging key and value column.
   */
  public static <T extends IConstructor> Set.Immutable<T> carrier(
      final SetMultimap.Immutable<T, T> graph) {

    final Set.Transient<T> tmp = Transient.of();

    graph.keySet().forEach(tmp::__insert);
    graph.values().forEach(tmp::__insert);

    return tmp.freeze();
  }

  /*
   * Projection from a tuple to single field.
   */
  public static <T extends IConstructor> Set.Immutable<T> project(
      final SetMultimap.Immutable<T, T> graph, final int field) {

    final Set.Transient<T> tmp = Transient.of();

    switch (field) {
      case 0:
        graph.keySet().forEach(tmp::__insert);
        break;
      case 1:
        graph.values().forEach(tmp::__insert);
        break;
    }

    return tmp.freeze();
  }

  /*
   * Projection from a tuple to another tuple with (possible reordered) subset of fields.
   */
  public static <T extends IConstructor> SetMultimap.Immutable<T, T> inverse(
      final SetMultimapFactory setMultimapFactory,
      final SetMultimap.Immutable<T, T> graph) {

    final SetMultimap.Transient<T, T> builder = setMultimapFactory
        .transientOf();

    graph.entrySet().forEach(tuple -> builder.__insert(tuple.getValue(), tuple.getKey()));

    return builder.freeze();
  }

  /*
   * Convert a set of tuples to a map; value in old map is associated with a set of keys in old map.
   */
  public static SetMultimap.Immutable<IConstructor, IConstructor> toMultimap(
      SetMultimapFactory setMultimapFactory,
      Set.Immutable<ITuple> st) {
    SetMultimap.Transient<IConstructor, IConstructor> mm = setMultimapFactory.transientOf();

    for (ITuple t : st) {
      IConstructor key = (IConstructor) t.get(0);
      IConstructor val = (IConstructor) t.get(1);

      mm.__insert(key, val);
    }

    return mm.freeze();
  }

  public static <T extends IConstructor, U extends IConstructor, V extends IConstructor> SetMultimap.Immutable<T, V> compose(
      SetMultimap.Immutable<T, U> relation0, SetMultimap.Immutable<U, V> relation1) {
    final SetMultimap.Transient<T, V> resultBuilder = SetMultimap.Transient.of();

    relation0.entrySet().stream()
        .filter(entry -> relation1.containsKey(entry.getValue()))
        .forEach(entry -> resultBuilder.__insert(entry.getKey(), relation1.get(entry.getValue())));

    return resultBuilder.freeze();
  }

  public static <T extends IConstructor> SetMultimap.Immutable<T, T> transitiveClosure(
      SetMultimap.Immutable<T, T> relation) {

    SetMultimap.Immutable<T, T> result = relation;

    while (true) {
      final SetMultimap.Immutable<T, T> previous = result;
      result = compose(result, relation);

      if (previous.size() == result.size()) {
        return result;
      }
    }
  }

  public static <T extends IConstructor> SetMultimap.Immutable<T, T> reflexiveTransitiveClosure(
      SetMultimap.Immutable<T, T> relation) {

    final SetMultimap.Transient<T, T> resultBuilder = transitiveClosure(relation).asTransient();
    carrier(relation).forEach(element -> resultBuilder.__insert(element, element));
    return resultBuilder.freeze();
  }

//  private static <K extends IConstructor, V extends IConstructor> SetMultimap.Immutable<K, V> computeClosureDelta(
//      IValueFactory vf, ISet rel1) {
//    RotatingQueue<IValue> iLeftKeys = new RotatingQueue<>();
//    RotatingQueue<RotatingQueue<IValue>> iLefts = new RotatingQueue<>();
//
//    ValueIndexedHashMap<RotatingQueue<IValue>> interestingLeftSides = new ValueIndexedHashMap<>();
//    ValueIndexedHashMap<ShareableValuesHashSet> potentialRightSides = new ValueIndexedHashMap<>();
//
//    // Index
//    Iterator<IValue> allDataIterator = rel1.iterator();
//    while (allDataIterator.hasNext()) {
//      ITuple tuple = (ITuple) allDataIterator.next();
//
//      IValue key = tuple.get(0);
//      IValue value = tuple.get(1);
//      RotatingQueue<IValue> leftValues = interestingLeftSides.get(key);
//      ShareableValuesHashSet rightValues;
//      if (leftValues != null) {
//        rightValues = potentialRightSides.get(key);
//      } else {
//        leftValues = new RotatingQueue<>();
//        iLeftKeys.put(key);
//        iLefts.put(leftValues);
//        interestingLeftSides.put(key, leftValues);
//
//        rightValues = new ShareableValuesHashSet();
//        potentialRightSides.put(key, rightValues);
//      }
//      leftValues.put(value);
//      rightValues.add(value);
//    }
//
//    int size = potentialRightSides.size();
//    int nextSize = 0;
//
//    // Compute
//    final ShareableValuesHashSet newTuples = new ShareableValuesHashSet();
//    do {
//      ValueIndexedHashMap<ShareableValuesHashSet> rightSides = potentialRightSides;
//      potentialRightSides = new ValueIndexedHashMap<>();
//
//      for (; size > 0; size--) {
//        IValue leftKey = iLeftKeys.get();
//        RotatingQueue<IValue> leftValues = iLefts.get();
//
//        RotatingQueue<IValue> interestingLeftValues = null;
//
//        IValue rightKey;
//        while ((rightKey = leftValues.get()) != null) {
//          ShareableValuesHashSet rightValues = rightSides.get(rightKey);
//          if (rightValues != null) {
//            Iterator<IValue> rightValuesIterator = rightValues.iterator();
//            while (rightValuesIterator.hasNext()) {
//              IValue rightValue = rightValuesIterator.next();
//              if (newTuples.add(vf.tuple(leftKey, rightValue))) {
//                if (interestingLeftValues == null) {
//                  nextSize++;
//
//                  iLeftKeys.put(leftKey);
//                  interestingLeftValues = new RotatingQueue<>();
//                  iLefts.put(interestingLeftValues);
//                }
//                interestingLeftValues.put(rightValue);
//
//                ShareableValuesHashSet potentialRightValues = potentialRightSides.get(rightKey);
//                if (potentialRightValues == null) {
//                  potentialRightValues = new ShareableValuesHashSet();
//                  potentialRightSides.put(rightKey, potentialRightValues);
//                }
//                potentialRightValues.add(rightValue);
//              }
//            }
//          }
//        }
//      }
//      size = nextSize;
//      nextSize = 0;
//    } while (size > 0);
//
//    return newTuples;
//  }

//  public static ISet closure(IValueFactory vf, ISet rel1) {
//    if (rel1.getElementType() == TF.voidType())
//      return rel1;
//    if (!isBinary(rel1))
//      throw new IllegalOperationException("closure", rel1.getType());
//
//    Type tupleElementType = rel1.getElementType().getFieldType(0).lub(rel1.getElementType().getFieldType(1));
//    Type tupleType = TF.tupleType(tupleElementType, tupleElementType);
//
//    java.util.Set<IValue> closureDelta = computeClosureDelta(vf, rel1, tupleType);
//
//    // NOTE: type is already known, thus, using a SetWriter degrades performance
//    ISetWriter resultWriter = vf.setWriter();
//    resultWriter.insertAll(rel1);
//    resultWriter.insertAll(closureDelta);
//
//    return resultWriter.done();
//  }
//
////	public static ISet closureStar(IValueFactory vf, ISet set1)
////			throws FactTypeUseException {
////		set1.getType().closure();
////		// an exception will have been thrown if the type is not acceptable
////
////		ISetWriter reflex = vf.setWriter();
////
////		for (IValue e : carrier(vf, set1)) {
////			reflex.insert(vf.tuple(new IValue[] { e, e }));
////		}
////
////		return closure(vf, set1).union(reflex.done());
////	}
//
//  // TODO: Currently untested in PDB.
//  public static ISet closureStar(IValueFactory vf, ISet rel1) {
//    if (rel1.getElementType() == TF.voidType())
//      return rel1;
//    if (!isBinary(rel1))
//      throw new IllegalOperationException("closureStar", rel1.getType());
//
//    Type tupleElementType = rel1.getElementType().getFieldType(0).lub(rel1.getElementType().getFieldType(1));
//    Type tupleType = TF.tupleType(tupleElementType, tupleElementType);
//
//    // calculate
//    ShareableValuesHashSet closureDelta = computeClosureDelta(vf, rel1, tupleType);
//    ISet carrier = carrier(vf, rel1);
//
//    // aggregate result
//    // NOTE: type is already known, thus, using a SetWriter degrades performance
//    ISetWriter resultWriter = vf.setWriter();
//    resultWriter.insertAll(rel1);
//    resultWriter.insertAll(closureDelta);
//
//    Iterator<IValue> carrierIterator = carrier.iterator();
//    while (carrierIterator.hasNext()) {
//      IValue element = carrierIterator.next();
//      resultWriter.insert(vf.tuple(element, element));
//    }
//
//    return resultWriter.done();
//  }

}

class ConstructorDummy implements IConstructor {

  private static final int DISCRIMINATOR_DEFAULT = -1;

  private final int value;
  private final int discriminator;

  ConstructorDummy(int value) {
    this.value = value;
    this.discriminator = DISCRIMINATOR_DEFAULT;
  }

  ConstructorDummy(int value, int discriminator) {
    if (discriminator < 0) {
      throw new IllegalArgumentException("Discriminators must be >= 0.");
    }

    this.value = value;
    this.discriminator = discriminator;
  }

  public boolean equals(Object other) {
    final ConstructorDummy that = ((ConstructorDummy) other);
    return value == that.value && discriminator == that.discriminator;
  }

  public int hashCode() {
    return value ^ discriminator;
  }

  public String toString() {
    if (discriminator == DISCRIMINATOR_DEFAULT) {
      return Integer.toString(value);
    } else {
      return String.format("%d(%d)", value, discriminator);
    }
  }

  @Override
  public Type getType() {
    return null;
  }

  @Override
  public <T, E extends Throwable> T accept(IValueVisitor<T, E> v) throws E {
    return null;
  }

  @Override
  public boolean isEqual(IValue other) {
    return false;
  }

  @Override
  public boolean isAnnotatable() {
    return false;
  }

  @Override
  public Type getConstructorType() {
    return null;
  }

  @Override
  public Type getUninstantiatedConstructorType() {
    return null;
  }

  @Override
  public IValue get(String label) {
    return null;
  }

  @Override
  public IConstructor set(String label, IValue newChild) throws FactTypeUseException {
    return null;
  }

  @Override
  public boolean has(String label) {
    return false;
  }

  @Override
  public IValue get(int i) throws IndexOutOfBoundsException {
    return null;
  }

  @Override
  public IConstructor set(int index, IValue newChild) throws FactTypeUseException {
    return null;
  }

  @Override
  public int arity() {
    return 0;
  }

  @Override
  public String getName() {
    if (value == 1) {
      return "methodEntry";
    } else {
      return "???";
    }
  }

  @Override
  public Iterable<IValue> getChildren() {
    return null;
  }

  @Override
  public Iterator<IValue> iterator() {
    return null;
  }

  @Override
  public INode replace(int first, int second, int end, IList repl)
      throws FactTypeUseException, IndexOutOfBoundsException {
    return null;
  }

  @Override
  public Type getChildrenTypes() {
    return null;
  }

  @Override
  public boolean declaresAnnotation(TypeStore store, String label) {
    return false;
  }

  @Override
  public IAnnotatable<? extends IConstructor> asAnnotatable() {
    return null;
  }

  @Override
  public boolean mayHaveKeywordParameters() {
    return false;
  }

  @Override
  public IWithKeywordParameters<? extends IConstructor> asWithKeywordParameters() {
    return null;
  }
}