/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package dom;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.usethesource.capsule.Set;
import io.usethesource.capsule.Set.Transient;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.IMapWriter;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISetWriter;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.io.StandardTextWriter;
import io.usethesource.vallang.io.old.BinaryValueReader;
import io.usethesource.vallang.io.old.BinaryValueWriter;
import org.rascalmpl.interpreter.utils.Timing;

import static dom.AllDominatorsRunner.CURRENT_DATA_SET_FILE_NAME;
import static dom.AllDominatorsRunner.DATA_SET_SINGLE_FILE_NAME;
import static dom.AllDominatorsRunner.LOG_BINARY_RESULTS;
import static dom.AllDominatorsRunner.LOG_TEXTUAL_RESULTS;

public class DominatorsPDB {

  public final IValueFactory vf;
  public final ISet EMPTY;

  public DominatorsPDB(IValueFactory vf) {
    this.vf = vf;
    this.EMPTY = vf.set();
  }

  public ISet intersect(ISet sets) {
    if (sets.isEmpty() || sets.contains(EMPTY)) {
      return EMPTY;
    }

    ISet first = (ISet) sets.iterator().next();
    sets = sets.delete(first);
    ISet result = first;
    for (IValue elem : sets) {
      result = result.intersect((ISet) elem);
    }

    return result;
  }

  private ISet setofdomsets(IMap dom, ISet preds) {
    ISetWriter result = vf.setWriter();

    for (IValue p : preds) {
      ISet ps = (ISet) dom.get(p);

      result.insert(ps == null ? EMPTY : ps);
    }

    return result.done();
  }

  public ISet top(ISet graph) {
    return graph.asRelation().project(0).subtract(graph.asRelation().project(1));
  }

  public IValue getTop(ISet graph) {
    for (IValue candidate : top(graph)) {
      switch (((IConstructor) candidate).getName()) {
        case "methodEntry":
        case "functionEntry":
        case "scriptEntry":
          return candidate;
      }
    }

    throw new RuntimeException("no entry?");
  }

  public IMap jDominators(ISet graph) {
    IValue n0 = getTop(graph);
    ISet nodes = graph.asRelation().carrier();
    // if (!nodes.getElementType().isAbstractData()) {
    // throw new RuntimeException("nodes is not the right type");
    // }
    IMap preds = (IMap) toMap(graph.asRelation().project(1, 0));
    // nodes = nodes.delete(n0);

    IMapWriter w = vf.mapWriter();
    w.put(n0, vf.set(n0));
    for (IValue n : nodes.delete(n0)) {
      w.put(n, nodes);
    }
    IMap dom = w.done();
    IMap prev = vf.mapWriter().done();

    /*
     * solve (dom) for (n <- nodes) dom[n] = {n} + intersect({dom[p] | p <- preds[n]?{}});
     */
    while (!prev.equals(dom)) {
      prev = dom;
      IMapWriter newDom = vf.mapWriter();

      for (IValue n : nodes) {
        ISet ps = (ISet) preds.get(n);
        if (ps == null) {
          ps = EMPTY;
        }
        ISet sos = setofdomsets(dom, ps);
        // if (!sos.getType().isSet() || !sos.getType().getElementType().isSet() ||
        // !sos.getType().getElementType().getElementType().isAbstractData()) {
        // throw new RuntimeException("not the right type: " + sos.getType());
        // }
        ISet intersected = intersect(sos);
        // if (!intersected.getType().isSet() ||
        // !intersected.getType().getElementType().isAbstractData()) {
        // throw new RuntimeException("not the right type: " + intersected.getType());
        // }
        ISet newValue = vf.set(n).union(intersected);
        // if (!newValue.getElementType().isAbstractData()) {
        // System.err.println("problem");
        // }
        newDom.put(n, newValue);
      }

      // if (!newDom.done().getValueType().getElementType().isAbstractData()) {
      // System.err.println("not good");
      // }
      dom = newDom.done();
    }

    return dom;
  }

  public static void main(String[] args) throws FileNotFoundException, IOException {
    testAll(io.usethesource.vallang.impl.persistent.ValueFactory.getInstance());
    testAll(io.usethesource.vallang.impl.fast.ValueFactory.getInstance());
  }

  public static IMap testOne(IValueFactory vf) throws IOException, FileNotFoundException {
    ISet data =
        (ISet) new BinaryValueReader().read(vf, new FileInputStream(DATA_SET_SINGLE_FILE_NAME));

    long before = Timing.getCpuTime();
    IMap pdbResults = new DominatorsPDB(vf).jDominators(data);

    System.err.println(vf.toString() + "\nDuration: "
        + ((Timing.getCpuTime() - before) / 1000000000) + " seconds\n");

    if (LOG_BINARY_RESULTS) {
      new BinaryValueWriter().write(pdbResults,
          new FileOutputStream("data/dominators-java-" + vf.toString() + "-single.bin"));
    }

    if (LOG_TEXTUAL_RESULTS) {
      new StandardTextWriter().write(pdbResults,
          new FileWriter("data/dominators-java-" + vf.toString() + "-single.txt"));
    }

    return pdbResults;
  }

  public static ISet testAll(IValueFactory vf) throws IOException, FileNotFoundException {
    IMap data =
        (IMap) new BinaryValueReader().read(vf, new FileInputStream(CURRENT_DATA_SET_FILE_NAME));
    ISetWriter result = vf.setWriter();

    long before = Timing.getCpuTime();
    for (IValue key : data) {
      try {
        result.insert(new DominatorsPDB(vf).jDominators((ISet) data.get(key)));
      } catch (RuntimeException e) {
        System.err.println(e.getMessage());
      }
    }
    System.err.println(vf.toString() + "\nDuration: "
        + ((Timing.getCpuTime() - before) / 1000000000) + " seconds\n");

    ISet pdbResults = result.done();

    if (LOG_BINARY_RESULTS) {
      new BinaryValueWriter().write(pdbResults, new FileOutputStream("data/dominators-java.bin"));
    }

    if (LOG_TEXTUAL_RESULTS) {
      new StandardTextWriter().write(pdbResults,
          new FileWriter("data/dominators-java-" + vf.toString() + ".txt"));
    }

    return pdbResults;
  }

  /*
   * Convert a set of tuples to a map; value in old map is associated with a set of keys in old map.
   */
  public static <K, V> io.usethesource.capsule.Map.Immutable<K, Set.Immutable<V>> toMap(
      ISet st) {
    Map<K, Set.Transient<V>> hm = new HashMap<>();

    for (IValue v : st) {
      ITuple t = (ITuple) v;
      K key = (K) t.get(0);
      V val = (V) t.get(1);
      Set.Transient<V> wValSet = hm.get(key);
      if (wValSet == null) {
        wValSet = Transient.of();
        hm.put(key, wValSet);
      }
      wValSet.__insert(val);
    }

    io.usethesource.capsule.Map.Transient<K, Set.Immutable<V>> w = io.usethesource.capsule.Map
        .Transient.of();
    for (K k : hm.keySet()) {
      w.__put(k, hm.get(k).freeze());
    }
    return w.freeze();
  }

}
