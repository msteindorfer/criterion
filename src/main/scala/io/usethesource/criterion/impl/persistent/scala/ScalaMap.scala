/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.scala

import scala.collection.JavaConversions.asJavaIterator
import scala.collection.JavaConversions.mapAsJavaMap

import io.usethesource.criterion.api.JmhMap
import io.usethesource.criterion.api.JmhValue

case class ScalaMap(xs: ScalaMap.Coll) extends JmhMap {

	override def isEmpty = xs isEmpty

	override def size = xs size

	override def put(k: JmhValue, v: JmhValue) = ScalaMap(xs + (k -> v))

	override def removeKey(k: JmhValue) = ScalaMap(xs - k)

	override def get(k: JmhValue) = xs getOrElse(k, null)

	override def containsKey(k: JmhValue) = xs contains k

	override def containsValue(v: JmhValue) = xs exists {
		case (_, cv) => v == cv
	}

	override def iterator = xs.keys iterator

	override def valueIterator = xs.values iterator

	@deprecated
	override def entryIterator: java.util.Iterator[java.util.Map.Entry[JmhValue, JmhValue]] = mapAsJavaMap(xs).entrySet iterator


	override def equals(that: Any): Boolean = that match {
		case other: ScalaMap => this.xs equals other.xs
		case _ => false
	}

	override def hashCode = xs.hashCode

	override def unwrap(): AnyRef = xs

}

object ScalaMap {
	type Coll = scala.collection.immutable.HashMap[JmhValue, JmhValue]
	val empty = scala.collection.immutable.HashMap.empty[JmhValue, JmhValue]
}