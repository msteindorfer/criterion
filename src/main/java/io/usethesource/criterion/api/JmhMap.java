/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.api;

import java.util.Iterator;
import java.util.Map.Entry;

public interface JmhMap extends Iterable<JmhValue>, JmhValue {
  /**
   * @return true iff the map is empty
   */
  public boolean isEmpty();

  /**
   * @return the number of keys that have a mapped value in this map
   */
  public int size();

  /**
   * Adds a new entry to the map, mapping the key to value. If the key existed before, the old value
   * will be lost.
   *
   * @param key
   * @param value
   * @return a copy of the map with the new key/value mapping
   */
  public JmhMap put(JmhValue key, JmhValue value);

  public JmhMap removeKey(JmhValue key);

  /**
   * @param key
   * @return the value that is mapped to this key, or null if no such value exists
   */
  public JmhValue get(JmhValue key);

  /**
   * Determine whether a certain key exists in this map.
   *
   * @param key
   * @return true iff there is a value mapped to this key
   */
  public boolean containsKey(JmhValue key);

  /**
   * Determine whether a certain value exists in this map.
   *
   * @param value
   * @return true iff there is at least one key that maps to the given value.
   */
  public boolean containsValue(JmhValue value);

  /**
   * @return an iterator over the keys of the map
   */
  @Override
  public Iterator<JmhValue> iterator();

  /**
   * @return an iterator over the values of the map
   */
  public Iterator<JmhValue> valueIterator();

  /**
   * @return an iterator over the keys-value pairs of the map
   */
  public Iterator<Entry<JmhValue, JmhValue>> entryIterator();

  @Deprecated
  default public boolean containsKey(int key) {
    // throw new UnsupportedOperationException("Not implemented.");
    return false;
  }

  @Deprecated
  default public JmhMap put(int key, int value) {
    // throw new UnsupportedOperationException("Not implemented.");
    return null;
  }

}
