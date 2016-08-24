/*******************************************************************************
 * Copyright (c) 2016 CWI All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package io.usethesource.criterion.impl.persistent.dexx;

import io.usethesource.criterion.api.JmhMap;
import io.usethesource.criterion.api.JmhMapBuilder;
import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhSetBuilder;
import io.usethesource.criterion.api.JmhSetMultimap;
import io.usethesource.criterion.api.JmhSetMultimapBuilder;
import io.usethesource.criterion.api.JmhValueFactory;

public class DexxValueFactory implements JmhValueFactory {

  public DexxValueFactory() {}

  public JmhSet set() {
    throw new UnsupportedOperationException();
  }

  public JmhSetBuilder setBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JmhMap map() {
    return mapBuilder().done();
  }

  @Override
  public JmhMapBuilder mapBuilder() {
    return new DexxMapWriter();
  }

  @Override
  public JmhSetMultimap setMultimap() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JmhSetMultimapBuilder setMultimapBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "VF_DEXX";
  }

}
