/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion.impl.persistent.scala

import io.usethesource.criterion.api.{JmhValue, JmhValueFactory}

class ScalaValueFactory extends JmhValueFactory {

  def set() = setBuilder.done

  def set(xs: JmhValue*) = {
    val writer = setBuilder
    writer.insert(xs: _*)
    writer.done
  }

  override def setBuilder = new ScalaSetBuilder

  //  def map = mapBuilder.done

  override def mapBuilder = new ScalaMapBuilder

  override def setMultimapBuilder = new ScalaSetMultimapBuilder

  //  def setMultimap = setMultimapBuilder.done

  override def toString = "VF_SCALA"

}
