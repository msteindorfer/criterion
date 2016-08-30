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

  public boolean isEmpty();

  public int size();

  public boolean contains(JmhValue element);

  public JmhSet insert(JmhValue element);

  public JmhSet delete(JmhValue elem);

  @Override
  public Iterator<JmhValue> iterator();

  public static interface Builder extends JmhBuilder {

    void insert(JmhValue... v);

    void insertAll(Iterable<? extends JmhValue> collection);

    @Override
    JmhSet done();

  }

}
