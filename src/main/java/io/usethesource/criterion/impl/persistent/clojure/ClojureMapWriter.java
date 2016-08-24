/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.clojure;

import clojure.lang.ITransientMap;
import clojure.lang.PersistentHashMap;
import io.usethesource.criterion.api.JmhMap;
import io.usethesource.criterion.api.JmhMapBuilder;
import io.usethesource.criterion.api.JmhValue;

class ClojureMapWriter implements JmhMapBuilder {

  protected ITransientMap xs;

  protected ClojureMapWriter() {
    super();

    this.xs = PersistentHashMap.EMPTY.asTransient();
  }

  @Override
  public void put(JmhValue key, JmhValue value) {
    xs = xs.assoc(key, value);
  }

  @Override
  public void putAll(JmhMap map) {
    for (JmhValue k : map) {
      xs = xs.assoc(k, map.get(k));
    }
  }

  @Override
  public void putAll(java.util.Map<JmhValue, JmhValue> map) {
    for (JmhValue k : map.keySet()) {
      xs = xs.assoc(k, map.get(k));
    }
  }

  @Override
  public JmhMap done() {
    return new ClojureMap(xs.persistent());
  }

}
