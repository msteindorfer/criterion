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

import clojure.lang.ITransientMap;
import clojure.lang.PersistentHashMap;
import io.usethesource.criterion.api.JmhMap;
import io.usethesource.criterion.api.JmhMapWriter;
import io.usethesource.criterion.api.JmhValue;

class ClojureMapWriter implements JmhMapWriter {

	protected ITransientMap xs;

	protected ClojureMapWriter() {
		super();

		this.xs = PersistentHashMap.EMPTY.asTransient();
	}

	@Override
	public void put(JmhValue key, JmhValue value) {
		xs = (ITransientMap) xs.assoc(key, value);
	}

	@Override
	public void putAll(JmhMap map) {
		for (JmhValue k : map) {
			xs = (ITransientMap) xs.assoc(k, map.get(k));
		}
	}

	@Override
	public void putAll(java.util.Map<JmhValue, JmhValue> map) {
		for (JmhValue k : map.keySet()) {
			xs = (ITransientMap) xs.assoc(k, map.get(k));
		}
	}

	@Override
	public JmhMap done() {
		return new ClojureMap(xs.persistent());
	}

}