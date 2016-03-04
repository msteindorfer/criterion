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

import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.mutable.SetBuilder

import io.usethesource.criterion.api.JmhSet
import io.usethesource.criterion.api.JmhSetWriter
import io.usethesource.criterion.api.JmhValue

sealed class ScalaSetWriter extends JmhSetWriter {

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
