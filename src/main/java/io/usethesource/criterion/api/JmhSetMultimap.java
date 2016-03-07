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

public interface JmhSetMultimap extends JmhValue { // Iterable<JmhValue> 

//	boolean isEmpty();
//
//	int size();

	JmhSetMultimap put(JmhValue key, JmhValue value);	
	
	JmhSetMultimap remove(JmhValue key, JmhValue value);

	boolean containsKey(JmhValue key);
	
	boolean contains(JmhValue key, JmhValue value);
	
//	JmhValue get(JmhValue key);
//
//	boolean containsValue(JmhValue value);
//
//	Iterator<JmhValue> iterator();
//
//	Iterator<JmhValue> valueIterator();
//
//	Iterator<Entry<JmhValue, JmhValue>> entryIterator();
		
	default boolean containsKey(int key) { 
		// throw new UnsupportedOperationException("Not implemented.");
		return false;
	}
	
	default JmhSetMultimap put(int key, int value) { 
		throw new UnsupportedOperationException("Not implemented.");
	}

}
