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

import io.usethesource.capsule.SetFactory;
import io.usethesource.capsule.TransientSet;
import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhSetBuilder;
import io.usethesource.criterion.api.JmhValue;

class ChampSetWriter implements JmhSetBuilder {

	protected final TransientSet<JmhValue> setContent;
	protected JmhSet constructedSet;

	ChampSetWriter(SetFactory setFactory) {
		setContent = setFactory.transientOf();
		constructedSet = null;
	}

	private void put(JmhValue element) {
		@SuppressWarnings("unused")
		boolean result = setContent.__insert(element);
	}

	@Override
	public void insert(JmhValue... values) {
		checkMutation();

		for (JmhValue item : values) {
			put(item);
		}
	}

	@Override
	public void insertAll(Iterable<? extends JmhValue> collection) {
		checkMutation();

		for (JmhValue item : collection) {
			put(item);
		}
	}

	@Override
	public JmhSet done() {
		if (constructedSet == null) {
			constructedSet = new ChampSet(setContent.freeze());
		}

		return constructedSet;
	}

	private void checkMutation() {
		if (constructedSet != null) {
			throw new UnsupportedOperationException("Mutation of a finalized set is not supported.");
		}
	}

	@Override
	public String toString() {
		return setContent.toString();
	}

}
