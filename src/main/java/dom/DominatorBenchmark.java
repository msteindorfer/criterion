package dom;

import java.util.ArrayList;

import org.openjdk.jmh.infra.Blackhole;
import org.rascalmpl.value.ISet;

public interface DominatorBenchmark {

  void performBenchmark(Blackhole bh, ArrayList<?> sampledGraphsNative);

  ArrayList<?> convertDataToNativeFormat(ArrayList<ISet> sampledGraphs);

}
