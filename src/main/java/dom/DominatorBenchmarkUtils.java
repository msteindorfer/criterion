package dom;


public class DominatorBenchmarkUtils {

	public static enum DominatorBenchmarkEnum {
		CHART {
			@Override
			public DominatorBenchmark getBenchmark() {
				return new DominatorsChart();
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
		};

		public abstract DominatorBenchmark getBenchmark();		
	}

}
