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

import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentSet;
import clojure.lang.PersistentHashSet;
import io.usethesource.criterion.api.JmhSetMultimap;
import io.usethesource.criterion.api.JmhValue;

public class ClojureSetMultimap implements JmhSetMultimap {

	protected final IPersistentMap xs;

	protected ClojureSetMultimap(IPersistentMap xs) {
		this.xs = xs;
	}

	// @Override
	// public boolean isEmpty() {
	// return size() == 0;
	// }
	//
	// @Override
	// public int size() {
	// return xs.count();
	// }

	@Override
	public JmhSetMultimap put(JmhValue key, JmhValue value) {
		Object singletonOrSet = xs.valAt(key);

		if (singletonOrSet == null) {
			return new ClojureSetMultimap((IPersistentMap) xs.assoc(key, value));
		} else if (singletonOrSet instanceof IPersistentSet) {
			IPersistentSet set = (IPersistentSet) singletonOrSet;
			return new ClojureSetMultimap((IPersistentMap) xs.assoc(key, set.cons(value)));
		} else if (singletonOrSet.equals(value)) {
			return this;
		} else {
			IPersistentSet set = PersistentHashSet.create(singletonOrSet, value);
			return new ClojureSetMultimap((IPersistentMap) xs.assoc(key, set));
		}
	}

	@Override
	public JmhSetMultimap remove(JmhValue key, JmhValue value) {
		Object singletonOrSet = xs.valAt(key);

		if (singletonOrSet == null) {
			return this;
		} else if (singletonOrSet instanceof IPersistentSet) {
			IPersistentSet oldSet = (IPersistentSet) singletonOrSet;
			IPersistentSet newSet = oldSet.disjoin(value);

			switch (newSet.count()) {
			case 0:
				return new ClojureSetMultimap((IPersistentMap) xs.without(key));
			case 1:
				return new ClojureSetMultimap((IPersistentMap) xs.assoc(key, newSet.seq().first()));
			default:
				return new ClojureSetMultimap((IPersistentMap) xs.assoc(key, newSet));
			}
		} else {
			if (singletonOrSet.equals(value)) {
				return new ClojureSetMultimap((IPersistentMap) xs.without(key));
			} else {
				return this;
			}
		}
	}


	// @Override
	// public JmhMap removeKey(JmhValue key) {
	// return new ClojureSetMultimap((IPersistentMap) xs.without(key));
	// }

	// @Override
	// public JmhValue get(JmhValue key) {
	// return (JmhValue) xs.valAt(key);
	// }

	@Override
	public boolean containsKey(JmhValue key) {
		return xs.containsKey(key);
	}

	@Override
	public boolean contains(JmhValue key, JmhValue value) {
		Object singletonOrSet = xs.valAt(key);
		
		if (singletonOrSet == null) {
			return false;
		} else if (singletonOrSet instanceof IPersistentSet) {
			IPersistentSet set = (IPersistentSet) singletonOrSet;
			return set.contains(value);
		} else {
			return singletonOrSet.equals(value);
		}
	}

//	@Override
//	public boolean containsValue(JmhValue value) {
//		return ((APersistentMap) xs).containsValue(value);
//	}

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

		if (other instanceof ClojureSetMultimap) {
			ClojureSetMultimap that = (ClojureSetMultimap) other;

			return xs.equals(that.xs);
		}

		return false;
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public Iterator<JmhValue> iterator() {
//		return ((APersistentMap) xs).keySet().iterator();
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public Iterator<JmhValue> valueIterator() {
//		return ((APersistentMap) xs).values().iterator();
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public Iterator<Entry<JmhValue, JmhValue>> entryIterator() {
//		return ((APersistentMap) xs).entrySet().iterator();
//	}

}
