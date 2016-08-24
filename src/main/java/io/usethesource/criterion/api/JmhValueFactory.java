/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.api;

/**
 * An IValueFactory is an AbstractFactory for values. Implementations of this class should guarantee
 * that the values returned are immutable. For batch construction of container classes there should
 * be implementations of the I{List,Set,Relation,Map}Writer interfaces.
 *
 * @author jurgen@vinju.org
 * @author rfuhrer@watson.ibm.com
 *
 */
public interface JmhValueFactory {

  /**
   * Creates an empty unmodifiable set.
   *
   * @return an empty set
   */
  public JmhSet set();

  /**
   * Create a set builder.
   *
   * @return a set builder
   */
  public JmhSetBuilder setBuilder();

  /**
   * Creates an empty unmodifiable map.
   *
   * @return an empty map
   */
  public JmhMap map();

  /**
   * Create a map builder.
   *
   * @return a map builder
   */
  public JmhMapBuilder mapBuilder();

  /**
   * Creates an empty unmodifiable set-multimap.
   *
   * @return an empty set-multimap
   */
  default JmhSetMultimap setMultimap() {
    throw new RuntimeException("Not supported.");
  }

  /**
   * Create a map builder.
   *
   * @return a map builder
   */
  default JmhSetMultimapBuilder setMultimapBuilder() {
    throw new RuntimeException("Not supported.");
  }

}
