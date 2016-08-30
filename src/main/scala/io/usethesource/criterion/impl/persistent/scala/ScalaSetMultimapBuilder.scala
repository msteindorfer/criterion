/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.scala

import io.usethesource.criterion.api.JmhSetMultimap
import io.usethesource.criterion.api.JmhValue

sealed class ScalaSetMultimapBuilder extends JmhSetMultimap.Builder {

  var xs: ScalaSetMultimap = new ScalaSetMultimap(ScalaSetMultimap.empty)

  override def insert(k: JmhValue, v: JmhValue) = { xs = xs.insert(k, v) }

  override def done = xs

}
