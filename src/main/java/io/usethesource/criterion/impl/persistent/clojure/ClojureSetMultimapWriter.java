/*******************************************************************************
 * Copyright (c) 2012-2013 CWI All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package io.usethesource.criterion.impl.persistent.clojure;

import java.util.HashMap;
import java.util.Map;

import clojure.lang.ITransientMap;
import clojure.lang.ITransientSet;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import io.usethesource.criterion.api.JmhSetMultimap;
import io.usethesource.criterion.api.JmhSetMultimapBuilder;
import io.usethesource.criterion.api.JmhValue;

class ClojureSetMultimapWriter implements JmhSetMultimapBuilder {

  Map<JmhValue, Object> builderMap = new HashMap<>();

  protected ClojureSetMultimapWriter() {
    super();
  }

  @Override
  public void insert(JmhValue key, JmhValue value) {
    Object singletonOrSet = builderMap.get(key);

    if (singletonOrSet == null) {
      builderMap.put(key, value);
    } else if (singletonOrSet instanceof ITransientSet) {
      ITransientSet set = (ITransientSet) singletonOrSet;
      set.conj(value);
    } else if (singletonOrSet.equals(value)) {
      // NOTHING
    } else {
      ITransientSet set =
          (ITransientSet) PersistentHashSet.create(singletonOrSet, value).asTransient();
      builderMap.put(key, set);
    }
  }

  // @Override
  // public void putAll(JmhMap map) {
  // for (JmhValue k : map) {
  // xs = (ITransientMap) xs.assoc(k, map.get(k));
  // }
  // }
  //
  // @Override
  // public void putAll(java.util.Map<JmhValue, JmhValue> map) {
  // for (JmhValue k : map.keySet()) {
  // xs = (ITransientMap) xs.assoc(k, map.get(k));
  // }
  // }

  @Override
  public JmhSetMultimap done() {
    ITransientMap xs = PersistentHashMap.EMPTY.asTransient();

    for (Map.Entry<JmhValue, Object> entry : builderMap.entrySet()) {
      Object key = entry.getKey();
      Object valueOrSet = entry.getValue();

      if (valueOrSet instanceof ITransientSet) {
        ITransientSet set = (ITransientSet) valueOrSet;
        xs.assoc(key, set.persistent());
      } else {
        xs.assoc(key, valueOrSet);
      }
    }

    return new ClojureSetMultimap(xs.persistent());
  }

}
