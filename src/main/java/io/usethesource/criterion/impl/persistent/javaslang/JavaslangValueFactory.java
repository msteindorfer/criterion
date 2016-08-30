/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.javaslang;

import io.usethesource.criterion.api.JmhMap;
import io.usethesource.criterion.api.JmhValueFactory;

public class JavaslangValueFactory implements JmhValueFactory {

  @Override
  public JmhMap.Builder mapBuilder() {
    return new JavaslangMapBuilder();
  }

  @Override
  public String toString() {
    return "VF_JAVASLANG";
  }

}
