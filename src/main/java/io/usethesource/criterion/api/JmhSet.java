/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.api;

import java.util.Iterator;

public interface JmhSet extends JmhValue, Iterable<JmhValue> {

  /**
   * @return true if this set has no elements
   */
  public boolean isEmpty();

  /**
   * @return the arity of the set, the number of elements in the set
   */
  public int size();

  /**
   * @param element
   * @return true if this is an element of the set
   */
  public boolean contains(JmhValue element);

  /**
   * Add an element to the set.
   *
   * @param <SetOrRel> ISet when the result will be a set, IRelation when it will be a relation.
   * @param element
   * @return a relation if the element type is a tuple type, a set otherwise
   */
  public JmhSet insert(JmhValue element);

  /**
   * Delete one element from the set.
   *
   * @param <SetOrRel>
   * @param set
   * @return a relation if the element type is a tuple type, a set otherwise
   */
  public JmhSet delete(JmhValue elem);

  @Override
  public Iterator<JmhValue> iterator();

}
