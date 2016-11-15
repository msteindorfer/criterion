/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.champ;

import io.usethesource.capsule.SetFactory;
import io.usethesource.capsule.api.deprecated.TransientSet;
import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhValue;

class ChampSetBuilder implements JmhSet.Builder {

  protected final TransientSet<JmhValue> setContent;
  protected JmhSet constructedSet;

  ChampSetBuilder(SetFactory setFactory) {
    setContent = setFactory.transientOf();
    constructedSet = null;
  }

  private void put(JmhValue element) {
    setContent.__insert(element);
  }

  @Override
  public void insert(JmhValue... values) {
    checkMutation();

    for (JmhValue item : values) {
      put(item);
    }
  }

  @Override
  public void insertAll(Iterable<? extends JmhValue> collection) {
    checkMutation();

    for (JmhValue item : collection) {
      put(item);
    }
  }

  @Override
  public JmhSet done() {
    if (constructedSet == null) {
      constructedSet = new ChampSet(setContent.freeze());
    }

    return constructedSet;
  }

  private void checkMutation() {
    if (constructedSet != null) {
      throw new UnsupportedOperationException("Mutation of a finalized set is not supported.");
    }
  }

  @Override
  public String toString() {
    return setContent.toString();
  }

}
