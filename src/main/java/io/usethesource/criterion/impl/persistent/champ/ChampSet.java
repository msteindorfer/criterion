/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.champ;

import java.util.Iterator;

import io.usethesource.capsule.api.deprecated.ImmutableSet;
import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhValue;

/*
 * Operates: * without types * with equals() instead of isEqual()
 */
public final class ChampSet implements JmhSet {

  private final ImmutableSet<JmhValue> content;

  public ChampSet(ImmutableSet<JmhValue> content) {
    this.content = content;
  }

  @Override
  public boolean isEmpty() {
    return content.isEmpty();
  }

  @Override
  public JmhSet insert(JmhValue value) {
    return new ChampSet(content.__insert(value));
  }

  @Override
  public JmhSet delete(JmhValue value) {
    return new ChampSet(content.__remove(value));
  }

  @Override
  public int size() {
    return content.size();
  }

  @Override
  public boolean contains(JmhValue value) {
    return content.contains(value);
  }

  @Override
  public Iterator<JmhValue> iterator() {
    return content.iterator();
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

    if (other instanceof ChampSet) {
      ChampSet that = (ChampSet) other;

      if (this.size() != that.size()) {
        return false;
      }

      return content.equals(that.content);
    }

    return false;
  }

}
