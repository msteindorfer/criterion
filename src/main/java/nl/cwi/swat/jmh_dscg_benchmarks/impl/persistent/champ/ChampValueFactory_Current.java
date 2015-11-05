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
package nl.cwi.swat.jmh_dscg_benchmarks.impl.persistent.champ;

import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhMap;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhMapWriter;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhSet;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhSetWriter;
import nl.cwi.swat.jmh_dscg_benchmarks.api.JmhValueFactory;

public class ChampValueFactory_Current implements JmhValueFactory {

	public JmhSet set() {
		return setWriter().done();
	}
	
	public JmhSetWriter setWriter() {
		return new ChampSetWriter_Current();
	}

	@Override
	public JmhMap map() {
		return mapWriter().done();
	}

	@Override
	public JmhMapWriter mapWriter() {
		return new ChampMapWriter_Current();
	}

	@Override
	public String toString() {
		return "VF_PDB_PERSISTENT_CURRENT";
	}

}
