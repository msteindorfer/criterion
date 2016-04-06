package dom;

import dom.multimap.DominatorsSetMultimap_Default;

public class DominatorBenchmarkUtils {

	public static enum DominatorBenchmarkEnum {
		HHAMT {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsSetMultimap_Default();
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
