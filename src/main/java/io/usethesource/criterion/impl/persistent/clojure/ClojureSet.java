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

import java.util.Iterator;

import clojure.lang.IPersistentSet;
import clojure.lang.PersistentHashSet;
import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhValue;

class ClojureSet implements JmhSet {

  protected final IPersistentSet xs;

  protected ClojureSet(IPersistentSet xs) {
    this.xs = xs;
  }

  protected ClojureSet() {
    this(PersistentHashSet.EMPTY);
  }

  protected ClojureSet(JmhValue... values) {
    this(PersistentHashSet.create((Object[]) values));
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<JmhValue> iterator() {
    return ((Iterable<JmhValue>) xs).iterator();
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public int size() {
    return xs.count();
  }

  @Override
  public boolean contains(JmhValue x) {
    return xs.contains(x);
  }

  @Override
  public JmhSet insert(JmhValue x) {
    return new ClojureSet((IPersistentSet) xs.cons(x));
  }

  @Override
  public JmhSet delete(JmhValue x) {
    return new ClojureSet(xs.disjoin(x));
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true;
    }
    if (other == null) {
      return false;
    }

    if (other instanceof ClojureSet) {
      ClojureSet that = (ClojureSet) other;

      return xs.equals(that.xs);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return xs.hashCode();
  }

}
