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
import io.usethesource.criterion.api.JmhSetMultimap
import io.usethesource.criterion.api.JmhSetMultimapBuilder

sealed class ScalaSetMultimapBuilder extends JmhSetMultimapBuilder {

  var xs: ScalaSetMultimap = ScalaSetMultimap(ScalaSetMultimap.empty)

  override def insert(k: JmhValue, v: JmhValue) = { xs = xs.insert(k, v) }

//  override def putAll(other: JmhSetMultimap) = other match {
//    case ScalaMap(ys) => xs ++= ys
//  }
//
//  override def putAll(ys: java.util.Map[JmhValue, JmhValue]) = xs ++= ys

  override def done = xs

}
