/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.scala_strawman

import io.usethesource.criterion.api.{JmhSet, JmhValue}
import strawman.collection.immutable
import strawman.collection.mutable

import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.JavaConverters._

sealed class ScalaSetBuilder extends JmhSet.Builder {

	val xs: mutable.Builder[JmhValue, ScalaSet.Coll] = strawman.collection.immutable.Set.newBuilder()

	override def insert(y: JmhValue) = xs += y

	override def insert(ys: JmhValue*) = ??? // xs ++= ys

	override def insertAll(ys: java.lang.Iterable[_ <: JmhValue]) = ??? // xs ++= ys

	override def done: JmhSet = { 
		ScalaSet(xs.result)
	}

}
