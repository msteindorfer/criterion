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
import io.usethesource.criterion.api.JmhMap;
import io.usethesource.criterion.api.JmhValue;

/*
 * Operates:
 * 		* without types
 * 		* with equals() instead of isEqual()
 */
public final class ChampMap implements JmhMap {
		
	private final ImmutableMap<JmhValue,JmhValue> content; 
	
	protected ChampMap(ImmutableMap<JmhValue, JmhValue> content) {
		this.content = content;
	}
	
	@Override
	public boolean isEmpty() {
		return content.isEmpty();
	}

	@Override
	public int size() {
		return content.size();
	}
	
	@Override
	public JmhMap put(JmhValue key, JmhValue value) {
		return new ChampMap(content.__put(key, value));
	}
	
	public JmhMap removeKey(JmhValue key) {
		return new ChampMap(content.__remove(key));
	}
	
	@Override
	public boolean containsKey(JmhValue key) {
		return content.containsKey(key);
	}

	@Override
	public boolean containsValue(JmhValue value) {
		return content.containsValue(value);
	}
	
	@Override
	public JmhValue get(JmhValue key) {
		return content.get(key);
	}

	@Override
	public int hashCode() {
		return content.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (other == null)
			return false;
		
		if (other instanceof ChampMap) {
			ChampMap that = (ChampMap) other;

			if (this.size() != that.size())
				return false;
			
			return content.equals(that.content);
		}
		
		return false;
	}
	
	@Override
	public Iterator<JmhValue> iterator() {
		return content.keyIterator();
	}
	
	@Override
	public Iterator<JmhValue> valueIterator() {
		return content.valueIterator();
	}

	@Override
	public Iterator<Entry<JmhValue, JmhValue>> entryIterator() {
		return content.entryIterator();
	}

	@Override
	public boolean containsKey(int key) {
		return content.containsKey(key);
	}

	@Override
	public JmhMap put(int key, int value) {
		return new ChampMap(content.__put(key, value));
	}	
	
}
