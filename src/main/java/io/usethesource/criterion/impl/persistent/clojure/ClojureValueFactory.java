/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.clojure;

import io.usethesource.criterion.api.JmhMap;
import io.usethesource.criterion.api.JmhMapBuilder;
import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhSetBuilder;
import io.usethesource.criterion.api.JmhSetMultimap;
import io.usethesource.criterion.api.JmhSetMultimapBuilder;
import io.usethesource.criterion.api.JmhValueFactory;

public class ClojureValueFactory implements JmhValueFactory {

  @Override
  public JmhMap map() {
    return mapBuilder().done();
  }

  @Override
  public JmhMapBuilder mapBuilder() {
    return new ClojureMapWriter();
  }

  @Override
  public JmhSet set() {
    return setBuilder().done();
  }

  @Override
  public JmhSetBuilder setBuilder() {
    return new ClojureSetWriter();
  }

  @Override
  public JmhSetMultimap setMultimap() {
    return setMultimapBuilder().done();
  }

  @Override
  public JmhSetMultimapBuilder setMultimapBuilder() {
    return new ClojureSetMultimapWriter();
  }

  @Override
  public String toString() {
    return "VF_CLOJURE";
  }

}
