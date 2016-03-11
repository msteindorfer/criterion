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
