/*******************************************************************************
 * Copyright (c) 2008, 2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package io.usethesource.criterion.api;

import java.util.Iterator;
import java.util.Map.Entry;

public interface JmhMap extends Iterable<JmhValue>, JmhValue {
	/**
	 * @return true iff the map is empty
	 */
	public boolean isEmpty();

	/**
	 * @return the number of keys that have a mapped value in this map
	 */
	public int size();

	/**
	 * Adds a new entry to the map, mapping the key to value. If the key existed
	 * before, the old value will be lost.
	 * 
	 * @param key
	 * @param value
	 * @return a copy of the map with the new key/value mapping
	 */
	public JmhMap put(JmhValue key, JmhValue value);

	public JmhMap removeKey(JmhValue key);

	/**
	 * @param key
	 * @return the value that is mapped to this key, or null if no such value
	 *         exists
	 */
	public JmhValue get(JmhValue key);

	/**
	 * Determine whether a certain key exists in this map.
	 * 
	 * @param key
	 * @return true iff there is a value mapped to this key
	 */
	public boolean containsKey(JmhValue key);

	/**
	 * Determine whether a certain value exists in this map.
	 * 
	 * @param value
	 * @return true iff there is at least one key that maps to the given value.
	 */
	public boolean containsValue(JmhValue value);

	/**
	 * @return an iterator over the keys of the map
	 */
	public Iterator<JmhValue> iterator();

	/**
	 * @return an iterator over the values of the map
	 */
	public Iterator<JmhValue> valueIterator();

	/**
	 * @return an iterator over the keys-value pairs of the map
	 */
	public Iterator<Entry<JmhValue, JmhValue>> entryIterator();
		
	default public boolean containsKey(int key) { 
		throw new UnsupportedOperationException("Not implemented.");
	}
	
	default public JmhMap put(int key, int value) { 
		throw new UnsupportedOperationException("Not implemented.");
	}

}
