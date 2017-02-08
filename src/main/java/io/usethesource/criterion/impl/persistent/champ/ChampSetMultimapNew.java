/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.champ;

import io.usethesource.capsule.api.SetMultimap;
import io.usethesource.criterion.api.JmhSetMultimap;
import io.usethesource.criterion.api.JmhValue;

import java.util.Iterator;
import java.util.Map.Entry;

public final class ChampSetMultimapNew implements JmhSetMultimap {

  private final SetMultimap.Immutable<JmhValue, JmhValue> content;

  protected ChampSetMultimapNew(SetMultimap.Immutable<JmhValue, JmhValue> content) {
    this.content = content;
  }

  @Override
  public boolean isEmpty() {
    return content.isEmpty();
  }

  @Override
  public int size() {
    return Math.toIntExact(content.size());
  }

  @Override
  public JmhSetMultimap insert(JmhValue key, JmhValue value) {
    return new ChampSetMultimapNew(content.insert(key, value));
  }

  @Override
  public JmhSetMultimap remove(JmhValue key, JmhValue value) {
    return new ChampSetMultimapNew(content.remove(key, value));
  }

  @Override
  public JmhSetMultimap put(JmhValue key, JmhValue value) {
    return new ChampSetMultimapNew(content.put(key, value));
  }

  @Override
  public JmhSetMultimap remove(JmhValue key) {
    return new ChampSetMultimapNew(content.remove(key));
  }

  @Override
  public boolean containsKey(JmhValue key) {
    return content.contains(key);
  }

  @Override
  public boolean contains(JmhValue key, JmhValue value) {
    return content.contains(key, value);
  }

  // @Override
  // public JmhValue get(JmhValue key) {
  // return content.get(key);
  // }

  @Override
  public java.util.Set<JmhValue> keySet() {
    // return content.keySet();
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public int hashCode() {
    return content.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other == null) {
      return false;
    }

    if (other instanceof ChampSetMultimapNew) {
      ChampSetMultimapNew that = (ChampSetMultimapNew) other;

      if (this.size() != that.size()) {
        return false;
      }

      return content.equals(that.content);
    }

    return false;
  }

  @Override
  public Iterator<JmhValue> iterator() {
    // return content.keyIterator();
    return null;
  }

  // @Override
  // public Iterator<JmhValue> valueIterator() {
  // return content.valueIterator();
  // }

  @Override
  public Iterator<Entry<JmhValue, JmhValue>> entryIterator() {
    return content.iterator();
  }

  @Override
  public Iterator<Entry<JmhValue, Object>> nativeEntryIterator() {
    // return content.nativeEntryIterator();
    return null;
  }

}
