/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.scala_strawman

import io.usethesource.criterion.api.{JmhSet, JmhValue}
import io.usethesource.criterion.impl.persistent.scala.ScalaSet.Coll

import strawman.collection.immutable.{CapsuleHashSet, HashSet}
import scala.collection.JavaConverters.{asJavaIteratorConverter, setAsJavaSetConverter}

import strawman.collection.mutable.Builder
import strawman.collection.{IterableFactory, IterableOps}

case class ScalaSet(val xs: ScalaSet.Coll) extends JmhSet {

  def isEmpty = xs.isEmpty

  def size = xs.size

  def contains(x: JmhValue) = xs contains x

  def insert(x: JmhValue): JmhSet = ScalaSet(xs + x)

  def delete(x: JmhValue): JmhSet = ScalaSet(xs - x)

  override def subsetOf(other: JmhSet): Boolean = other match {
    case that: ScalaSet => this.xs subsetOf that.xs
    case _ => throw new IllegalArgumentException
  }

  override def union(other: JmhSet): JmhSet = other match {
    case that: ScalaSet => ScalaSet(this.xs union that.xs)
    case _ => throw new IllegalArgumentException
  }

  override def subtract(other: JmhSet): JmhSet = other match {
    case that: ScalaSet => ScalaSet(this.xs diff that.xs)
    case _ => throw new IllegalArgumentException
  }

  override def intersect(other: JmhSet): JmhSet = other match {
    case that: ScalaSet => ScalaSet(this.xs intersect that.xs)
    case _ => throw new IllegalArgumentException
  }

  // def iterator = ??? // xs.iterator.asJava

  override def iterator = xs.iterator

  implicit class ScalaToJavaIteratorAdapter(val it: strawman.collection.Iterator[JmhValue])
    extends java.util.Iterator[JmhValue] {

    override def hasNext: Boolean = it.hasNext

    override def next: JmhValue = it.next
  }

  implicit class JavaToScalaIteratorAdapter(val it: java.util.Iterator[JmhValue])
    extends strawman.collection.Iterator[JmhValue] {

    override def hasNext: Boolean = it.hasNext

    override def next: JmhValue = it.next
  }

  implicit class ScalaToJavaIterableAdapter(val iterable: strawman.collection.Iterable[JmhValue])
    extends java.lang.Iterable[JmhValue] {

    override def iterator = iterable.iterator
  }

  implicit class JavaToScalaIterableAdapter(val iterable: java.lang.Iterable[JmhValue])
    extends strawman.collection.Iterable[JmhValue] {

    override def iterator = iterable.iterator

    override protected[this] def coll: strawman.collection.Iterable[JmhValue] = ???

    override protected[this] def fromSpecificIterable(coll: strawman.collection.Iterable[JmhValue]): strawman.collection.Iterable[JmhValue] = ???

    override def iterableFactory: IterableFactory[strawman.collection.Iterable] = ???

    override protected[this] def newSpecificBuilder(): Nothing = ???
  }

  override def fromIterable(iterable: java.lang.Iterable[JmhValue]): JmhSet = {
    ScalaSet(strawman.collection.immutable.Set.fromIterable(iterable))
  }

  override def asJavaSet: java.util.Set[JmhValue] = ??? // xs.asJava

  override def equals(other: Any): Boolean = other match {
    case that: ScalaSet => this.xs equals that.xs
    case _ => false
  }

  override def hashCode = xs.hashCode

}

object ScalaSet {
  type Coll = strawman.collection.immutable.Set[JmhValue]
  val empty = strawman.collection.immutable.Set.empty[JmhValue]
}
