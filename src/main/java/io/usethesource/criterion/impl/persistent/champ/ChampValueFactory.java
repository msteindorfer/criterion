/*******************************************************************************
 * Copyright (c) 2013-2014 CWI All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 * * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package io.usethesource.criterion.impl.persistent.champ;

import io.usethesource.capsule.MapFactory;
import io.usethesource.capsule.SetFactory;
import io.usethesource.capsule.SetMultimapFactory;
import io.usethesource.criterion.api.JmhMap;
import io.usethesource.criterion.api.JmhMapBuilder;
import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhSetBuilder;
import io.usethesource.criterion.api.JmhSetMultimap;
import io.usethesource.criterion.api.JmhSetMultimapBuilder;
import io.usethesource.criterion.api.JmhValueFactory;

public class ChampValueFactory implements JmhValueFactory {

  private final SetFactory setFactory;
  private final MapFactory mapFactory;
  private final SetMultimapFactory setMultimapFactory;

  public ChampValueFactory(final Class<?> targetSetClass, final Class<?> targetMapClass,
      final Class<?> targetSetMultimapClass) {
    setFactory = targetSetClass == null ? null : new SetFactory(targetSetClass);
    mapFactory = targetMapClass == null ? null : new MapFactory(targetMapClass);

    if (targetSetMultimapClass == null) {
      setMultimapFactory = null;
    } else {
      setMultimapFactory = new SetMultimapFactory(targetSetMultimapClass);
    }
  }

  public JmhSet set() {
    return setBuilder().done();
  }

  public JmhSetBuilder setBuilder() {
    return new ChampSetWriter(setFactory);
  }

  @Override
  public JmhMap map() {
    return mapBuilder().done();
  }

  @Override
  public JmhMapBuilder mapBuilder() {
    return new PersistentChampMapWriter(mapFactory);
  }

  @Override
  public JmhSetMultimap setMultimap() {
    return setMultimapBuilder().done();
  }

  @Override
  public JmhSetMultimapBuilder setMultimapBuilder() {
    if (setMultimapFactory == null) {
      return new PersistentChampSetMultimapWriterNew();
    } else {
      return new PersistentChampSetMultimapWriter(setMultimapFactory);
    }
  }

  @Override
  public String toString() {
    return "VF_PDB_PERSISTENT_CURRENT";
  }

}
