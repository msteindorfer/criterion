/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.api;

import java.util.Map;

public interface JmhMapBuilder extends JmhBuilder {

  void put(JmhValue key, JmhValue value);

  void putAll(JmhMap map);

  void putAll(Map<JmhValue, JmhValue> map);

  @Override
  JmhMap done();

}
