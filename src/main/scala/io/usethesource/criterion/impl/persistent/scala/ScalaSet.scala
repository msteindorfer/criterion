/**
  * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
  * All rights reserved.
  *
  * This file is licensed under the BSD 2-Clause License, which accompanies this project
  * and is available under https://opensource.org/licenses/BSD-2-Clause.
  */
package io.usethesource.criterion.impl.persistent.scala

import io.usethesource.criterion.api.{JmhSet, JmhValue}

import scala.collection.JavaConverters.{asJavaIteratorConverter, setAsJavaSetConverter}

case class ScalaSet(val xs: ScalaSet.Coll) extends JmhSet {

  def isEmpty = xs.isEmpty

  def size = xs.size

  def contains(x: JmhValue) = xs contains x

  def insert(x: JmhValue): JmhSet = ScalaSet(xs + x)

  def delete(x: JmhValue): JmhSet = ScalaSet(xs - x)

  def iterator = xs.iterator.asJava

  override def asJavaSet: java.util.Set[JmhValue] = xs.asJava

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
