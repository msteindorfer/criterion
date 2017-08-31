/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.javaslang;

import io.usethesource.criterion.api.JmhValue;
import io.usethesource.criterion.impl.AbstractSetBuilder;
import javaslang.collection.HashSet;

final class JavaslangSetBuilder extends AbstractSetBuilder<JmhValue, HashSet<JmhValue>> {

  JavaslangSetBuilder() {
    super(HashSet.empty(), set -> set::add, JavaslangSet::new);
  }

}
