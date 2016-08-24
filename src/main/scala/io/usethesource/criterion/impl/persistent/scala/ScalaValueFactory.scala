/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.scala

import scala.collection.JavaConversions.mapAsScalaMap

import io.usethesource.criterion.api.JmhValue
import io.usethesource.criterion.api.JmhValueFactory
import io.usethesource.criterion.api.JmhSetMultimap

class ScalaValueFactory extends JmhValueFactory {
	
	def set() = setBuilder.done

	def set(xs: JmhValue*) = {
		val writer = setBuilder
		writer.insert(xs: _*)
		writer.done
	}

	def setBuilder = new ScalaSetBuilder
	
	def map() = mapBuilder.done

	def mapBuilder = new ScalaMapBuilder
	
	def setMultimapBuilder = new ScalaSetMultimapBuilder
	
	def setMultimap = setMultimapBuilder.done
	
	override def toString = "VF_SCALA"

}
