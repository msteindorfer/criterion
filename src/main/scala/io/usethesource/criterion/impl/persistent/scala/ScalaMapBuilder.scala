/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.scala

import scala.collection.immutable.Map.empty
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.mutable.MapBuilder
import io.usethesource.criterion.api.JmhValue
import io.usethesource.criterion.api.JmhMap
import io.usethesource.criterion.api.JmhMapBuilder

sealed class ScalaMapBuilder extends JmhMapBuilder {

  val xs: MapBuilder[JmhValue, JmhValue, ScalaMap.Coll] = new MapBuilder(ScalaMap.empty)

  override def put(k: JmhValue, v: JmhValue) = xs += (k -> v)

  override def putAll(other: JmhMap) = other match {
    case ScalaMap(ys) => xs ++= ys
  }

  override def putAll(ys: java.util.Map[JmhValue, JmhValue]) = xs ++= ys

  override def done = ScalaMap(xs.result)

}
