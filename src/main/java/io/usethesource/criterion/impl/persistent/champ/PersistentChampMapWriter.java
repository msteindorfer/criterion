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

import java.util.Iterator;
import java.util.Map.Entry;

import io.usethesource.capsule.ImmutableMap;
import io.usethesource.capsule.MapFactory;
import io.usethesource.criterion.api.JmhMap;
import io.usethesource.criterion.api.JmhMapBuilder;
import io.usethesource.criterion.api.JmhValue;

/*
 * Operates:
 * 		* without types
 * 		* with equals() instead of isEqual()
 */
final class PersistentChampMapWriter implements JmhMapBuilder {

	protected ImmutableMap<JmhValue, JmhValue> mapContent;
	protected JmhMap constructedMap;

	PersistentChampMapWriter(MapFactory mapFactory) {
		mapContent = mapFactory.of();
		constructedMap = null;
	}

	@Override
	public void put(JmhValue key, JmhValue value) {
		checkMutation();
		mapContent = mapContent.__put(key, value);
	}

	@Override
	public void putAll(JmhMap map) {
		putAll(map.entryIterator());
	}

	@Override
	public void putAll(java.util.Map<JmhValue, JmhValue> map) {
		putAll(map.entrySet().iterator());
	}

	private void putAll(Iterator<Entry<JmhValue, JmhValue>> entryIterator) {
		checkMutation();

		while (entryIterator.hasNext()) {
			final Entry<JmhValue, JmhValue> entry = entryIterator.next();
			final JmhValue key = entry.getKey();
			final JmhValue value = entry.getValue();

			mapContent = mapContent.__put(key, value);
		}
	}

	protected void checkMutation() {
		if (constructedMap != null) {
			throw new UnsupportedOperationException("Mutation of a finalized map is not supported.");
		}
	}

	@Override
	public JmhMap done() {
		if (constructedMap == null) {
			constructedMap = new ChampMap(mapContent);
		}

		return constructedMap;
	}

	@Override
	public void put(int key, int value) {
		checkMutation();
		mapContent = mapContent.__put(key, value);
	}

}
