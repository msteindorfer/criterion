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
package nl.cwi.swat.jmh_dscg_benchmarks.impl.persistent.scala

import scala.collection.JavaConversions.asJavaIterator
import scala.collection.JavaConversions.mapAsJavaMap
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhMap
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhValue

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

}

object ScalaMap {
	type Coll = scala.collection.immutable.HashMap[JmhValue, JmhValue]
	val empty = scala.collection.immutable.HashMap.empty[JmhValue, JmhValue]
}