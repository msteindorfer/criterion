/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 * 
 *******************************************************************************/

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

  public Iterator<JmhValue> iterator();

}
