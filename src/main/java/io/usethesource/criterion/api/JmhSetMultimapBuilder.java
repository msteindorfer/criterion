/*******************************************************************************
 * Copyright (c) 2008 CWI. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 * 
 *******************************************************************************/

package io.usethesource.criterion.api;

public interface JmhSetMultimapBuilder extends JmhBuilder {

  default void insert(int key, int value) {
    throw new UnsupportedOperationException("Not implemented.");
  }

  void insert(JmhValue key, JmhValue value);

  // void putAll(JmhMap map);
  //
  // void putAll(Map<JmhValue, JmhValue> map);

  JmhSetMultimap done();

}
