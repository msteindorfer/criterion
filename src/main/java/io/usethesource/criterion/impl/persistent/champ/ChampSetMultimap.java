/*******************************************************************************
 * Copyright (c) 2013-2014 CWI All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package io.usethesource.criterion.impl.persistent.champ;

import java.util.Iterator;
import java.util.Map.Entry;

import io.usethesource.capsule.ImmutableSetMultimap;
import io.usethesource.criterion.api.JmhSetMultimap;
import io.usethesource.criterion.api.JmhValue;

public final class ChampSetMultimap implements JmhSetMultimap {

  private final ImmutableSetMultimap<JmhValue, JmhValue> content;

  protected ChampSetMultimap(ImmutableSetMultimap<JmhValue, JmhValue> content) {
    this.content = content;
  }

  @Override
  public boolean isEmpty() {
    return content.isEmpty();
  }

  @Override
  public int size() {
    return content.size();
  }

  @Override
  public JmhSetMultimap insert(JmhValue key, JmhValue value) {
    return new ChampSetMultimap(content.__insert(key, value));
  }

  @Override
  public JmhSetMultimap remove(JmhValue key, JmhValue value) {
    return new ChampSetMultimap(content.__removeEntry(key, value));
  }

  @Override
  public JmhSetMultimap put(JmhValue key, JmhValue value) {
    return new ChampSetMultimap(content.__put(key, value));
  }

  @Override
  public JmhSetMultimap remove(JmhValue key) {
    return new ChampSetMultimap(content.__remove(key));
  }

  @Override
  public boolean containsKey(JmhValue key) {
    return content.containsKey(key);
  }

  @Override
  public boolean contains(JmhValue key, JmhValue value) {
    return content.containsEntry(key, value);
  }

  // @Override
  // public JmhValue get(JmhValue key) {
  // return content.get(key);
  // }

  @Override
  public int hashCode() {
    return content.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other == this)
      return true;
    if (other == null)
      return false;

    if (other instanceof ChampSetMultimap) {
      ChampSetMultimap that = (ChampSetMultimap) other;

      if (this.size() != that.size())
        return false;

      return content.equals(that.content);
    }

    return false;
  }

  @Override
  public Iterator<JmhValue> iterator() {
    return content.keyIterator();
  }

  // @Override
  // public Iterator<JmhValue> valueIterator() {
  // return content.valueIterator();
  // }

  @Override
  public Iterator<Entry<JmhValue, JmhValue>> entryIterator() {
    return content.entryIterator();
  }

  @Override
  public Iterator<Entry<JmhValue, Object>> nativeEntryIterator() {
    return content.nativeEntryIterator();
  }

}
