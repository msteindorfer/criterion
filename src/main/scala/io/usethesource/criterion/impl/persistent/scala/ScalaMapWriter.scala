/*******************************************************************************
 * Copyright (c) 2012-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *    * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 ******************************************************************************/
package io.usethesource.criterion.impl.persistent.scala

import scala.collection.immutable.Map.empty
import scala.collection.JavaConversions.mapAsScalaMap
import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.mutable.MapBuilder
import io.usethesource.criterion.api.JmhValue
import io.usethesource.criterion.api.JmhMap
import io.usethesource.criterion.api.JmhMapWriter

sealed class ScalaMapWriter extends JmhMapWriter {

  val xs: MapBuilder[JmhValue, JmhValue, ScalaMap.Coll] = new MapBuilder(ScalaMap.empty)

  override def put(k: JmhValue, v: JmhValue) = xs += (k -> v)

  override def putAll(other: JmhMap) = other match {
    case ScalaMap(ys) => xs ++= ys
  }

  override def putAll(ys: java.util.Map[JmhValue, JmhValue]) = xs ++= ys

  override def done = ScalaMap(xs.result)

}
