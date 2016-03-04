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

import scala.collection.JavaConversions.mapAsScalaMap

import io.usethesource.criterion.api.JmhValue
import io.usethesource.criterion.api.JmhValueFactory

class ScalaValueFactory extends JmhValueFactory {
	
	def set() = setWriter.done

	def set(xs: JmhValue*) = {
		val writer = setWriter
		writer.insert(xs: _*)
		writer.done
	}

	def setWriter = new ScalaSetWriter
	
	def map() = mapWriter.done

	def mapWriter = new ScalaMapWriter

	override def toString = "VF_SCALA"

}
