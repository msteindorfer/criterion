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

import io.usethesource.capsule.ImmutableSet;
import io.usethesource.criterion.api.JmhSet;
import io.usethesource.criterion.api.JmhValue;

/*
 * Operates:
 * 		* without types
 * 		* with equals() instead of isEqual()
 */
public final class ChampSet implements JmhSet {
		
	private final ImmutableSet<JmhValue> content;

	public ChampSet(ImmutableSet<JmhValue> content) {
		this.content = content;
	}

	@Override
	public boolean isEmpty() {
		return content.isEmpty();
	}

	@Override
	public JmhSet insert(JmhValue value) {
		return new ChampSet(content.__insert(value));
	}

	@Override
	public JmhSet delete(JmhValue value) {
		return new ChampSet(content.__remove(value));
	}

	@Override
	public int size() {
		return content.size();
	}

	@Override
	public boolean contains(JmhValue value) {
		return content.contains(value);
	}

	@Override
	public Iterator<JmhValue> iterator() {
		return content.iterator();
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
		
		if (other instanceof ChampSet) {
			ChampSet that = (ChampSet) other;
			
			if (this.size() != that.size())
				return false;

			return content.equals(that.content);
		}
		
		return false;
	}
			
}