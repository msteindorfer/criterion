/*******************************************************************************
 * Copyright (c) 2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package nl.cwi.swat.jmh_dscg_benchmarks;

import org.eclipse.imp.pdb.facts.IValueFactory;

public class BenchmarkUtils {
	public static enum ValueFactoryFactory {
//		VF_PDB_REFERENCE {
//			@Override IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.reference.ValueFactory.getInstance();
//			}
//		},
//		VF_PDB_FAST {
//			@Override
//			IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.fast.ValueFactory.getInstance();
//			}
//		},
//		VF_PDB_PERSISTENT {
//			@Override
//			IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance();
//			}
//		},
//		VF_CLOJURE {
//			@Override IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.persistent.clojure.ValueFactory.getInstance();
//			}
//		},
//		VF_SCALA {
//			@Override IValueFactory getInstance() {
//				return new org.eclipse.imp.pdb.facts.impl.persistent.scala.ValueFactory();
//			}
//		};
//		
		VF_PDB_GPCE_0To04 {
			@Override
			IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryGPCE0To4.getInstance();
			}
		},
//		VF_PDB_GPCE_0To08 {
//			@Override
//			IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryGPCE0To8.getInstance();
//			}
//		},
//		VF_PDB_GPCE_0To12 {
//			@Override
//			IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryGPCE0To12.getInstance();
//			}
//			},		
		VF_PDB_GPCE_DYNAMIC {
			@Override
			IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryGPCEDynamic.getInstance();
			}
		},
		VF_PDB_PERSISTENT_BLEEDING_EDGE {
			@Override
			IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryBleedingEdge.getInstance();
			}
		},
		VF_PDB_PERSISTENT_UNTYPED {
			@Override
			IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactorySpecializationWithUntypedVariables.getInstance();
			}
		},		
		VF_PDB_PERSISTENT_CURRENT {
			@Override
			IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryCurrent.getInstance();
			}
		},		
		VF_CLOJURE {
			@Override IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.clojure.TypelessValueFactory.getInstance();
			}
		},
//		VF_CLJ_DS {
//			@Override IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.persistent.clojure.TypelessCljDsValueFactory.getInstance();
//			}
//		},		
		VF_SCALA {
			@Override IValueFactory getInstance() {
				return new org.eclipse.imp.pdb.facts.impl.persistent.scala.TypelessValueFactory();
			}
		};

		abstract IValueFactory getInstance();
	}
}
