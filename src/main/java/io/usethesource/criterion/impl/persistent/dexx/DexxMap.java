/*******************************************************************************
 * Copyright (c) 2016 CWI All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package io.usethesource.criterion.impl.persistent.dexx;

import java.util.Iterator;
import java.util.Map.Entry;

import com.github.andrewoma.dexx.collection.HashMap;

import io.usethesource.criterion.api.JmhMap;
import io.usethesource.criterion.api.JmhValue;

public final class DexxMap implements JmhMap {

  private final HashMap<JmhValue, JmhValue> content;

  protected DexxMap(HashMap<JmhValue, JmhValue> content) {
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
  public JmhMap put(JmhValue key, JmhValue value) {
    return new DexxMap(content.put(key, value));
  }

  @Override
  public JmhMap removeKey(JmhValue key) {
    return new DexxMap(content.remove(key));
  }

  @Override
  public boolean containsKey(JmhValue key) {
    return content.containsKey(key);
  }

  @Override
  public boolean containsValue(JmhValue value) {
    // TODO: implement search based on values()
    // return content.containsValue(value);
    throw new UnsupportedOperationException("Does not natively support containsValue().");
  }

  @Override
  public JmhValue get(JmhValue key) {
    return content.get(key);
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

    if (other instanceof DexxMap) {
      DexxMap that = (DexxMap) other;

      if (this.size() != that.size()) {
        return false;
      }

      return content.equals(that.content);
    }

    return false;
  }

  @Override
  public Iterator<JmhValue> iterator() {
    return content.keys().iterator();
  }

  @SuppressWarnings("deprecation")
  @Override
  public Iterator<JmhValue> valueIterator() {
    // safe to call iterator in values view
    return content.values().iterator();
  }

  @Override
  public Iterator<Entry<JmhValue, JmhValue>> entryIterator() {
    // return content.iterator();
    throw new UnsupportedOperationException(
        "Has own Pair<K, V> class that does not implement Map.Entry.");
  }

}
