/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.pcollections;

import io.usethesource.criterion.api.JmhMapBuilder;
import io.usethesource.criterion.api.JmhValueFactory;

public class PcollectionsValueFactory implements JmhValueFactory {

  @Override
  public JmhMapBuilder mapBuilder() {
    return new PcollectionsMapBuilder();
  }

  @Override
  public String toString() {
    return "VF_PCOLLECTIONS";
  }

}
