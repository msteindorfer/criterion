/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.dexx;

import io.usethesource.criterion.api.JmhMap;
import io.usethesource.criterion.api.JmhMapBuilder;
import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhSetBuilder;
import io.usethesource.criterion.api.JmhSetMultimap;
import io.usethesource.criterion.api.JmhSetMultimapBuilder;
import io.usethesource.criterion.api.JmhValueFactory;

public class DexxValueFactory implements JmhValueFactory {

  public DexxValueFactory() {}

  @Override
  public JmhSet set() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JmhSetBuilder setBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JmhMap map() {
    return mapBuilder().done();
  }

  @Override
  public JmhMapBuilder mapBuilder() {
    return new DexxMapWriter();
  }

  @Override
  public JmhSetMultimap setMultimap() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JmhSetMultimapBuilder setMultimapBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "VF_DEXX";
  }

}
