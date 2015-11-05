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

import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhMap;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhMapWriter;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhSet;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhSetWriter;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhValueFactory;

public class ClojureValueFactory implements JmhValueFactory {

	@Override
	public JmhMap map() {
		return mapWriter().done();
	}

	@Override
	public JmhMapWriter mapWriter() {
		return new ClojureMapWriter();
	}

	@Override
	public JmhSet set() {
		return setWriter().done();
	}

	@Override
	public JmhSetWriter setWriter() {
		return new ClojureSetWriter();
	}
	
	@Override
	public String toString() {
		return "VF_CLOJURE";
	}
	
}
