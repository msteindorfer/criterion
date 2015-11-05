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

import java.util.Iterator;
import java.util.Map.Entry;

import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhMap;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhValue;
import clojure.lang.APersistentMap;
import clojure.lang.IPersistentMap;

/*
 * Operates:
 * 		* without types
 * 		* with equals() instead of isEqual()
 */
public class ClojureMap implements JmhMap {

	protected final IPersistentMap xs;

	protected ClojureMap(IPersistentMap xs) {
		this.xs = xs;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public int size() {
		return xs.count();
	}

	@Override
	public JmhMap put(JmhValue key, JmhValue value) {
		return new ClojureMap((IPersistentMap) xs.assoc(key, value));
	}

	@Override
	public JmhMap removeKey(JmhValue key) {
		return new ClojureMap((IPersistentMap) xs.without(key));
	}

	@Override
	public JmhValue get(JmhValue key) {
		return (JmhValue) xs.valAt(key);
	}

	@Override
	public boolean containsKey(JmhValue key) {
		return xs.containsKey(key);
	}

	@Override
	public boolean containsValue(JmhValue value) {
		return ((APersistentMap) xs).containsValue(value);
	}

	@Override
	public int hashCode() {
		return xs.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (other == null)
			return false;

		if (other instanceof ClojureMap) {
			ClojureMap that = (ClojureMap) other;

			return xs.equals(that.xs);
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<JmhValue> iterator() {
		return ((APersistentMap) xs).keySet().iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<JmhValue> valueIterator() {
		return ((APersistentMap) xs).values().iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<Entry<JmhValue, JmhValue>> entryIterator() {
		return ((APersistentMap) xs).entrySet().iterator();
	}

}
