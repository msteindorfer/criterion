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
package nl.cwi.swat.jmh_dscg_benchmarks.impl.persistent.champ;

import java.util.Iterator;
import java.util.Map.Entry;

import io.usethesource.capsule.TransientMap;
import io.usethesource.capsule.TrieMap_5Bits;

import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhMap;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhMapWriter;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhValue;

/*
 * Operates:
 * 		* without types
 * 		* with equals() instead of isEqual()
 */
final class ChampMapWriter_Current implements JmhMapWriter {

	protected final TransientMap<JmhValue, JmhValue> mapContent;
	protected JmhMap constructedMap;

	ChampMapWriter_Current() {
		super();

		mapContent = TrieMap_5Bits.transientOf();
		constructedMap = null;
	}

	@Override
	public void put(JmhValue key, JmhValue value) {
		checkMutation();

		@SuppressWarnings("unused")
		final JmhValue replaced = mapContent.__put(key, value);
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

			@SuppressWarnings("unused")
			final JmhValue replaced = mapContent.__put(key, value);
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
			constructedMap = new ChampMap(mapContent.freeze());
		}

		return constructedMap;
	}
}
