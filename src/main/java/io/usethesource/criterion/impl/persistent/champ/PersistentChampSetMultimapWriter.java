/*******************************************************************************
 * Copyright (c) 2013-2014 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package io.usethesource.criterion.impl.persistent.champ;

import io.usethesource.capsule.ImmutableSetMultimap;
import io.usethesource.capsule.SetMultimapFactory;
import io.usethesource.criterion.api.JmhSetMultimap;
import io.usethesource.criterion.api.JmhSetMultimapBuilder;
import io.usethesource.criterion.api.JmhValue;

final class PersistentChampSetMultimapWriter implements JmhSetMultimapBuilder {

	protected ImmutableSetMultimap<JmhValue, JmhValue> mapContent;
	protected JmhSetMultimap constructedMap;

	PersistentChampSetMultimapWriter(SetMultimapFactory setMultimapFactory) {
		mapContent = setMultimapFactory.of();
		constructedMap = null;
	}

	@Override
	public void insert(JmhValue key, JmhValue value) {
		checkMutation();
		mapContent = mapContent.__insert(key, value);
	}

	// @Override
	// public void put(int key, int value) {
	// checkMutation();
	// mapContent = mapContent.__put(key, value);
	// }

	protected void checkMutation() {
		if (constructedMap != null) {
			throw new UnsupportedOperationException(
							"Mutation of a finalized map is not supported.");
		}
	}

	@Override
	public JmhSetMultimap done() {
		if (constructedMap == null) {
			constructedMap = new ChampSetMultimap(mapContent);
		}

		return constructedMap;
	}

}
