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

import java.util.Collections
import java.util.Map.Entry 
import scala.collection.immutable
import scala.collection.JavaConversions.asJavaIterator
import scala.collection.JavaConversions.mapAsJavaMap
import scala.collection.JavaConversions._
import io.usethesource.criterion.api.JmhValue
import io.usethesource.criterion.api.JmhSetMultimap

import io.usethesource.capsule.AbstractSpecialisedImmutableMap.entryOf

case class ScalaSetMultimap(xs: ScalaSetMultimap.Coll) extends JmhSetMultimap {

  //	override def isEmpty = xs isEmpty
  //
  //	override def size = xs size

  override def put(key: JmhValue, value: JmhValue): ScalaSetMultimap = {
    xs.get(key) match {
      case None =>
        val set = makeSet
        return ScalaSetMultimap(xs.updated(key, set + value))
      case Some(set) =>
        return ScalaSetMultimap(xs.updated(key, set + value))
    }
    this
  }

  override def remove(key: JmhValue, value: JmhValue): ScalaSetMultimap = {
    xs.get(key) match {
      case None =>
      case Some(set) =>
        val newSet = set - value
        if (newSet.isEmpty)
          return ScalaSetMultimap(xs - key)
        else
          return ScalaSetMultimap(xs.updated(key, newSet))
    }
    this
  }

  override def containsKey(k: JmhValue) = xs contains k

  override def contains(k: JmhValue, v: JmhValue) = entryExists(k, _ == v)
	
//	override def get(k: JmhValue) = xs getOrElse(k, null)
//
//	override def containsValue(v: JmhValue) = xs exists {
//		case (_, cv) => v == cv
//	}
//
//	override def iterator = xs.keys iterator
//
//	override def valueIterator = xs.values iterator

	override def entryIterator: java.util.Iterator[java.util.Map.Entry[JmhValue, JmhValue]] = {
//    val xsAsFlatTuples: scala.collection.immutable.HashMap[JmhValue, JmhValue] = xs.flatMap { case (k, vs) => vs.map((k, _)) }
//    mapAsJavaMap(xsAsFlatTuples).entrySet iterator
	  new FlatteningIterator(mapAsJavaMap(xs).entrySet.iterator)  
  }


	override def equals(that: Any): Boolean = that match {
		case other: ScalaSetMultimap => this.xs equals other.xs
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

