/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package dom;

import java.util.List;

import io.usethesource.vallang.ISet;
import org.openjdk.jmh.infra.Blackhole;

public interface DominatorBenchmark {

  void performBenchmark(Blackhole bh, List<?> sampledGraphsNative);

  List<?> convertDataToNativeFormat(List<ISet> sampledGraphs);

}
