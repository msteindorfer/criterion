/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.scala_strawman

import io.usethesource.criterion.api.{JmhMap, JmhValue}
import strawman.collection.immutable.HashMap
import strawman.collection.mutable

sealed class ScalaMapBuilder extends JmhMap.Builder {

  val xs: mutable.Builder[(JmhValue, JmhValue), ScalaMap.Coll] = HashMap.newBuilder()

  override def put(k: JmhValue, v: JmhValue) = xs += (k -> v)

  override def putAll(other: JmhMap) = other match {
    case ScalaMap(ys) => xs ++= ys
  }

  override def putAll(ys: java.util.Map[JmhValue, JmhValue]) = ??? // xs ++= ys

  override def done = ScalaMap(xs.result)

}
