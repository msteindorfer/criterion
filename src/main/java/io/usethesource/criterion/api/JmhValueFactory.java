/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* Copyright (C) 2007-2013 CWI
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
*    Anya Helene Bagge - rationals and labeled maps

*******************************************************************************/
package io.usethesource.criterion.api;


/**
 * An IValueFactory is an AbstractFactory for values. Implementations of this
 * class should guarantee that the values returned are immutable. For batch
 * construction of container classes there should be implementations of the
 * I{List,Set,Relation,Map}Writer interfaces.
 * 
 * @author jurgen@vinju.org
 * @author rfuhrer@watson.ibm.com
 * 
 */
public interface JmhValueFactory {

	/**
	 * Creates an empty unmodifiable set.
	 * 
	 * @return an empty set
	 */
	public JmhSet set();

	/**
	 * Create a set builder.
	 * 
	 * @return a set builder
	 */
	public JmhSetBuilder setBuilder();

	/**
	 * Creates an empty unmodifiable map.
	 * 
	 * @return an empty map
	 */
	public JmhMap map();

	/**
	 * Create a map builder.
	 * 
	 * @return a map builder
	 */
	public JmhMapBuilder mapBuilder();
		
	/**
	 * Creates an empty unmodifiable set-multimap.
	 * 
	 * @return an empty set-multimap
	 */
	default JmhSetMultimap setMultimap() {
		throw new RuntimeException("Not supported.");
	}

	/**
	 * Create a map builder.
	 * 
	 * @return a map builder
	 */
	default JmhSetMultimapBuilder setMultimapBuilder() {
		throw new RuntimeException("Not supported.");
	}
	
}
