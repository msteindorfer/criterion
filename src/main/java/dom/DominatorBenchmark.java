package dom;

import java.util.ArrayList;

import org.rascalmpl.value.ISet;
import org.openjdk.jmh.infra.Blackhole;

public interface DominatorBenchmark {

  void performBenchmark(Blackhole bh, ArrayList<?> sampledGraphsNative);

  ArrayList<?> convertDataToNativeFormat(ArrayList<ISet> sampledGraphs);

}
