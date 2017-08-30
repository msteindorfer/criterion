/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.scala

import io.usethesource.criterion.api.{JmhSet, JmhValue}

import scala.collection.JavaConverters.{asJavaIteratorConverter, setAsJavaSetConverter, iterableAsScalaIterableConverter}
import scala.collection.immutable.HashSet

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

//  override def fromIterable(iterable: java.lang.Iterable[JmhValue]): JmhSet = {
//    ScalaSet(ScalaSet.empty ++ iterable.asScala)
//
////    val builder = HashSet.newBuilder[JmhValue]
////    iterable.forEach(item => builder += item)
////    ScalaSet(builder.result)
//
////    val builder = HashSet.newBuilder[JmhValue]
////    builder ++= iterable.asScala
////    ScalaSet(builder.result)
//  }

  def iterator = xs.iterator.asJava

  override def asJavaSet: java.util.Set[JmhValue] = xs.asJava

  override def equals(other: Any): Boolean = other match {
    case that: ScalaSet => this.xs equals that.xs
    case _ => false
  }

  override def hashCode = xs.hashCode

  override def unwrap(): AnyRef = xs

}

object ScalaSet {
  type Coll = scala.collection.immutable.HashSet[JmhValue]
  val empty = scala.collection.immutable.HashSet.empty[JmhValue]
}
