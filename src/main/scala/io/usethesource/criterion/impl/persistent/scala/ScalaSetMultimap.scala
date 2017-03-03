/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.scala

import java.util.Map.Entry

import io.usethesource.capsule.util.collection.AbstractSpecialisedImmutableMap.entryOf
import io.usethesource.criterion.api.{JmhSet, JmhSetMultimap, JmhValue}

import scala.collection.JavaConversions.{asJavaIterator, mapAsJavaMap, setAsJavaSet}
import scala.collection.immutable

case class ScalaSetMultimap(xs: ScalaSetMultimap.Coll) extends JmhSetMultimap {

	override def isEmpty = xs isEmpty

	override def size = xs size  // TODO: is unique keySet size instead of entrySet size

  override def insert(key: JmhValue, value: JmhValue): ScalaSetMultimap = {
    xs.get(key) match {
      case None =>
        val set = makeSet
        return ScalaSetMultimap(xs.updated(key, set + value))
      case Some(set) =>
        return ScalaSetMultimap(xs.updated(key, set + value))
    }
  }

	override def put(key: JmhValue, value: JmhValue): ScalaSetMultimap = {
		val set = makeSet
		return ScalaSetMultimap(xs.updated(key, set + value))
	}

	override def remove(key: JmhValue): ScalaSetMultimap = {
		return ScalaSetMultimap(xs - key)
	}
  
  override def remove(key: JmhValue, value: JmhValue): ScalaSetMultimap = {
    xs.get(key) match {
      case None => return this
      case Some(set) =>
        val newSet = set - value
        if (newSet.isEmpty)
          return ScalaSetMultimap(xs - key)
        else
          return ScalaSetMultimap(xs.updated(key, newSet))
    }
  }

  override def containsKey(k: JmhValue) = xs contains k

	override def containsValue(v: JmhValue) = xs containsValue v

  override def contains(k: JmhValue, v: JmhValue) = entryExists(k, _ == v)
	
//	override def get(k: JmhValue) = xs getOrElse(k, null)
//
//	override def containsValue(v: JmhValue) = xs exists {
//		case (_, cv) => v == cv
//	}

	override def iterator = xs.keys iterator

//	override def valueIterator = xs.values iterator

	override def entryIterator: java.util.Iterator[java.util.Map.Entry[JmhValue, JmhValue]] = {
//    val xsAsFlatTuples: scala.collection.immutable.HashMap[JmhValue, JmhValue] = xs.flatMap { case (k, vs) => vs.map((k, _)) }
//    mapAsJavaMap(xsAsFlatTuples).entrySet iterator
	  new FlatteningIterator(mapAsJavaMap(xs).entrySet.iterator)  
  }

  	override def nativeEntryIterator: java.util.Iterator[java.util.Map.Entry[JmhValue, Object]] = {
		mapAsJavaMap(xs).entrySet.iterator.asInstanceOf[java.util.Iterator[java.util.Map.Entry[JmhValue, Object]]]
  	}

	override def keySet: java.util.Set[JmhValue] = xs.keySet

	override def equals(that: Any): Boolean = that match {
		case other: ScalaSetMultimap => (this.xs.size == other.xs.size) && (this.xs equals other.xs)
		case _ => false
	}

	override def hashCode = xs.hashCode

	/////
	// Mulitmap Utilities
	//////
	
  private def makeSet: immutable.HashSet[JmhValue] = new immutable.HashSet[JmhValue]
		
	private def entryExists(key: JmhValue, p: JmhValue => Boolean): Boolean = xs.get(key) match {
    case None => false
    case Some(set) => set exists p
  }
	
}

class FlatteningIterator(val entryIterator : java.util.Iterator[java.util.Map.Entry[JmhValue, immutable.HashSet[JmhValue]]]) extends java.util.Iterator[java.util.Map.Entry[JmhValue, JmhValue]] {

	var lastKey : JmhValue = null
	var lastIterator : java.util.Iterator[JmhValue] = java.util.Collections.emptyIterator[JmhValue]

	override def hasNext : Boolean = if (lastIterator.hasNext) true else entryIterator.hasNext   

	override def next : Entry[JmhValue, JmhValue] = {
		if (lastIterator.hasNext) {
			return entryOf(lastKey, lastIterator.next());
		} else {
			lastKey = null;
			
			val nextEntry = entryIterator.next
							  					
			lastKey = nextEntry.getKey
			lastIterator = setAsJavaSet(nextEntry.getValue).iterator
			
			return entryOf(lastKey, lastIterator.next)
		}
	}

}

object ScalaSetMultimap {
  type Coll = scala.collection.immutable.HashMap[JmhValue, immutable.HashSet[JmhValue]]
  val empty = scala.collection.immutable.HashMap.empty[JmhValue, immutable.HashSet[JmhValue]]
}

