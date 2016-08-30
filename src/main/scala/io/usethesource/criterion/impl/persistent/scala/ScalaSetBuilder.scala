/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.scala

import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.mutable.SetBuilder

import io.usethesource.criterion.api.JmhSet
import io.usethesource.criterion.api.JmhValue

sealed class ScalaSetBuilder extends JmhSet.Builder {

	val xs: SetBuilder[JmhValue, ScalaSet.Coll] = new SetBuilder(ScalaSet.empty)

	override def insert(ys: JmhValue*) {
		xs ++= ys
	}

	override def insertAll(ys: java.lang.Iterable[_ <: JmhValue]) {
		xs ++= ys
	}

	override def done: JmhSet = { 
		ScalaSet(xs.result)
	}

}
