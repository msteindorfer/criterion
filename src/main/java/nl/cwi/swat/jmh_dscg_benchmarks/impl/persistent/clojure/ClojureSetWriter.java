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
package nl.cwi.swat.jmh_dscg_benchmarks.impl.persistent.clojure;

import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhSet;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhSetWriter;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhValue;
import clojure.lang.IPersistentSet;
import clojure.lang.ITransientSet;
import clojure.lang.PersistentHashSet;

class ClojureSetWriter implements JmhSetWriter {

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