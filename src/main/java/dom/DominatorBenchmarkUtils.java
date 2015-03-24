package dom;


public class DominatorBenchmarkUtils {

	public static enum DominatorBenchmarkEnum {
		CHART {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsWithoutPDB();
			}
		},
		CLOJURE {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsClojure();
			}
		},		
		SCALA {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsScalaV1();
			}
		};

		public abstract DominatorBenchmark getBenchmark();		
	}

}
