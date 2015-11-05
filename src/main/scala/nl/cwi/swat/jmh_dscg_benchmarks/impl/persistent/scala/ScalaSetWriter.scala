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

import scala.collection.JavaConversions.iterableAsScalaIterable
import scala.collection.mutable.SetBuilder

import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhSet
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhSetWriter
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhValue

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
