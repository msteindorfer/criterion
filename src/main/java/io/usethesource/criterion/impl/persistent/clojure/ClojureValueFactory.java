/*******************************************************************************
 * Copyright (c) 2012-2013 CWI All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package io.usethesource.criterion.impl.persistent.clojure;

import io.usethesource.criterion.api.JmhMap;
import io.usethesource.criterion.api.JmhMapBuilder;
import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhSetBuilder;
import io.usethesource.criterion.api.JmhSetMultimap;
import io.usethesource.criterion.api.JmhSetMultimapBuilder;
import io.usethesource.criterion.api.JmhValueFactory;

public class ClojureValueFactory implements JmhValueFactory {

  @Override
  public JmhMap map() {
    return mapBuilder().done();
  }

  @Override
  public JmhMapBuilder mapBuilder() {
    return new ClojureMapWriter();
  }

  @Override
  public JmhSet set() {
    return setBuilder().done();
  }

  @Override
  public JmhSetBuilder setBuilder() {
    return new ClojureSetWriter();
  }

  @Override
  public JmhSetMultimap setMultimap() {
    return setMultimapBuilder().done();
  }

  @Override
  public JmhSetMultimapBuilder setMultimapBuilder() {
    return new ClojureSetMultimapWriter();
  }

  @Override
  public String toString() {
    return "VF_CLOJURE";
  }

}
