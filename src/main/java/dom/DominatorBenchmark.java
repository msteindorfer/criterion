/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package dom;

import java.util.ArrayList;

import org.openjdk.jmh.infra.Blackhole;
import io.usethesource.vallang.ISet;

public interface DominatorBenchmark {

  void performBenchmark(Blackhole bh, ArrayList<?> sampledGraphsNative);

  ArrayList<?> convertDataToNativeFormat(ArrayList<ISet> sampledGraphs);

}
