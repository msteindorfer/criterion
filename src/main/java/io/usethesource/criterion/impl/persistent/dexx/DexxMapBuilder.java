/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.dexx;

import java.util.Iterator;
import java.util.Map.Entry;

import com.github.andrewoma.dexx.collection.HashMap;

import io.usethesource.criterion.api.JmhMap;
import io.usethesource.criterion.api.JmhMapBuilder;
import io.usethesource.criterion.api.JmhValue;

final class DexxMapBuilder implements JmhMapBuilder {

  protected HashMap<JmhValue, JmhValue> mapContent;
  protected JmhMap constructedMap;

  DexxMapBuilder() {
    mapContent = HashMap.empty();
    constructedMap = null;
  }

  @Override
  public void put(JmhValue key, JmhValue value) {
    checkMutation();
    mapContent = mapContent.put(key, value);
  }

  @Override
  public void putAll(JmhMap map) {
    putAll(map.entryIterator());
  }

  @Override
  public void putAll(java.util.Map<JmhValue, JmhValue> map) {
    putAll(map.entrySet().iterator());
  }

  private void putAll(Iterator<Entry<JmhValue, JmhValue>> entryIterator) {
    checkMutation();

    while (entryIterator.hasNext()) {
      final Entry<JmhValue, JmhValue> entry = entryIterator.next();
      final JmhValue key = entry.getKey();
      final JmhValue value = entry.getValue();

      mapContent = mapContent.put(key, value);
    }
  }

  protected void checkMutation() {
    if (constructedMap != null) {
      throw new UnsupportedOperationException("Mutation of a finalized map is not supported.");
    }
  }

  @Override
  public JmhMap done() {
    if (constructedMap == null) {
      constructedMap = new DexxMap(mapContent);
    }

    return constructedMap;
  }

}
