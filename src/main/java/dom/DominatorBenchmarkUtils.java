/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package dom;

import dom.multimap.DominatorsSetMultimap_Default;
import dom.multimap.DominatorsSetMultimap_Default_Instrumented;
import io.usethesource.capsule.SetMultimapFactory;
import io.usethesource.capsule.core.PersistentTrieSetMultimap;
import io.usethesource.capsule.experimental.multimap.TrieSetMultimap_HHAMT;
import io.usethesource.capsule.experimental.multimap.TrieSetMultimap_HHAMT_Specialized;

public class DominatorBenchmarkUtils {

  public static enum DominatorBenchmarkEnum {
    VF_CHAMP_MULTIMAP_INSTRUMENTED {
      @Override
      public DominatorBenchmark getBenchmark() {
        return new DominatorsSetMultimap_Default_Instrumented();
      }
    },
    VF_CHAMP_MULTIMAP_HCHAMP {
      @Override
      public DominatorBenchmark getBenchmark() {
        return new DominatorsSetMultimap_Default(
            new SetMultimapFactory(PersistentTrieSetMultimap.class));
      }
    },
    VF_CHAMP_MULTIMAP_HHAMT {
      @Override
      public DominatorBenchmark getBenchmark() {
        return new DominatorsSetMultimap_Default(
            new SetMultimapFactory(TrieSetMultimap_HHAMT.class));
      }
    },
    VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED {
      @Override
      public DominatorBenchmark getBenchmark() {
        return new DominatorsSetMultimap_Default(
            new SetMultimapFactory(TrieSetMultimap_HHAMT_Specialized.class));
      }
    },
    CHART {
      @Override
      public DominatorBenchmark getBenchmark() {
        return new DominatorsWithoutPDB_Default();
      }
    },
    CHART_LAZY {
      @Override
      public DominatorBenchmark getBenchmark() {
        return new DominatorsWithoutPDB_LazyHashCode();
      }
    },
    CLOJURE_LAZY {
      @Override
      public DominatorBenchmark getBenchmark() {
        return new DominatorsClojure();
      }
    },
    SCALA {
      @Override
      public DominatorBenchmark getBenchmark() {
        return new DominatorsScala_Default();
      }
    },
    SCALA_LAZY {
      @Override
      public DominatorBenchmark getBenchmark() {
        return new DominatorsScala_LazyHashCode();
      }
    };

    public abstract DominatorBenchmark getBenchmark();
  }

}
