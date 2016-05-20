package dom;

import dom.multimap.DominatorsSetMultimap_Default;
import dom.multimap.DominatorsSetMultimap_New;
import io.usethesource.capsule.SetMultimapFactory;
import io.usethesource.capsule.TrieSetMultimap_HHAMT;
import io.usethesource.capsule.TrieSetMultimap_HHAMT_Interlinked;
import io.usethesource.capsule.TrieSetMultimap_HHAMT_Specialized;
import io.usethesource.capsule.TrieSetMultimap_HHAMT_Specialized_Interlinked;

public class DominatorBenchmarkUtils {

	public static enum DominatorBenchmarkEnum {
		VF_CHAMP_MULTIMAP_HHAMT {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsSetMultimap_Default(
								new SetMultimapFactory(TrieSetMultimap_HHAMT.class));
			}
		},
		VF_CHAMP_MULTIMAP_HHAMT_INTERLINKED {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsSetMultimap_Default(
								new SetMultimapFactory(TrieSetMultimap_HHAMT_Interlinked.class));
			}
		},
		VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED_INTERLINKED {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsSetMultimap_Default(
								new SetMultimapFactory(TrieSetMultimap_HHAMT_Specialized_Interlinked.class));
			}
		},				
		VF_CHAMP_MULTIMAP_HHAMT_SPECIALIZED {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsSetMultimap_Default(
								new SetMultimapFactory(TrieSetMultimap_HHAMT_Specialized.class));
			}
		},				
		VF_CHAMP_MULTIMAP_HHAMT_NEW {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsSetMultimap_New();
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
