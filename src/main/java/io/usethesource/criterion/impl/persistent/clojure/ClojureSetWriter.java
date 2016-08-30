/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.clojure;

import clojure.lang.IPersistentSet;
import clojure.lang.ITransientSet;
import clojure.lang.PersistentHashSet;
import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhValue;

class ClojureSetWriter implements JmhSet.Builder {

  protected ITransientSet xs;

  protected ClojureSetWriter() {
    super();
    this.xs = (ITransientSet) PersistentHashSet.EMPTY.asTransient();
  }

  @Override
  public void insert(JmhValue... values) {
    for (JmhValue x : values) {
      xs = (ITransientSet) xs.conj(x);
    }
  }

  @Override
  public void insertAll(Iterable<? extends JmhValue> values) {
    for (JmhValue x : values) {
      xs = (ITransientSet) xs.conj(x);
    }
  }

  @Override
  public JmhSet done() {
    IPersistentSet result = (IPersistentSet) xs.persistent();
    return new ClojureSet(result);
  }

}
