/*******************************************************************************
 * Copyright (c) 2012-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package io.usethesource.criterion.impl.persistent.clojure;

import clojure.lang.IPersistentSet;
import clojure.lang.ITransientSet;
import clojure.lang.PersistentHashSet;
import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhSetBuilder;
import io.usethesource.criterion.api.JmhValue;

class ClojureSetWriter implements JmhSetBuilder {

	protected ITransientSet xs;

	protected ClojureSetWriter() {
		super();
		this.xs = (ITransientSet) PersistentHashSet.EMPTY.asTransient();
	}

	@Override
	public void insert(JmhValue... values) {
		for (JmhValue x : values) {
			xs = (ITransientSet) xs.conj(x);
		}
	}

	@Override
	public void insertAll(Iterable<? extends JmhValue> values) {
		for (JmhValue x : values) {
			xs = (ITransientSet) xs.conj(x);
		}
	}

	@Override
	public JmhSet done() {
		IPersistentSet result = (IPersistentSet) xs.persistent();
		return new ClojureSet(result);
	}

}