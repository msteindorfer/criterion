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

import scala.collection.JavaConversions.asJavaIterator
import io.usethesource.criterion.api.JmhSet
import io.usethesource.criterion.api.JmhValue

case class ScalaSet(val xs: ScalaSet.Coll) extends JmhSet {

	def isEmpty = xs.isEmpty

	def size = xs.size

	def contains(x: JmhValue) = xs contains x

	def insert(x: JmhValue): JmhSet = ScalaSet(xs + x)

	def delete(x: JmhValue): JmhSet = ScalaSet(xs - x)

	def iterator = xs.iterator

	override def equals(other: Any): Boolean = other match {
		case that: ScalaSet => this.xs equals that.xs
		case _ => false
	}

	override def hashCode = xs.hashCode

}

object ScalaSet {
	type Coll = scala.collection.immutable.HashSet[JmhValue]
	val empty = scala.collection.immutable.HashSet.empty[JmhValue]
}
