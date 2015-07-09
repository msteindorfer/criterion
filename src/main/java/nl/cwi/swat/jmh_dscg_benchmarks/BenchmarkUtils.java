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

import java.util.Arrays;
import java.util.Random;

import org.eclipse.imp.pdb.facts.IValueFactory;

public class BenchmarkUtils {
	public static enum ValueFactoryFactory {
		VF_CLOJURE {
			@Override public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.clojure.TypelessValueFactory.getInstance();
			}
		},
//		VF_CLJ_DS {
//			@Override public IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.persistent.clojure.TypelessCljDsValueFactory.getInstance();
//			}
//		},		
		VF_SCALA {
			@Override public IValueFactory getInstance() {
				return new org.eclipse.imp.pdb.facts.impl.persistent.scala.TypelessValueFactory();
			}
		},
//		VF_PDB_REFERENCE {
//			@Override public IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.reference.ValueFactory.getInstance();
//			}
//		},
		VF_PDB_FAST {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.fast.ValueFactory.getInstance();
			}
		},
//		VF_PDB_PERSISTENT {
//			@Override
//			public IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance();
//			}
//		},
//		VF_CLOJURE {
//			@Override public IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.persistent.clojure.ValueFactory.getInstance();
//			}
//		},
//		VF_SCALA {
//			@Override public IValueFactory getInstance() {
//				return new org.eclipse.imp.pdb.facts.impl.persistent.scala.ValueFactory();
//			}
//		};
//		
		VF_PDB_GPCE_0To04 {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryGPCE0To4.getInstance();
			}
		},
//		VF_PDB_GPCE_0To08 {
//			@Override
//			public IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryGPCE0To8.getInstance();
//			}
//		},
//		VF_PDB_GPCE_0To12 {
//			@Override
//			public IValueFactory getInstance() {
//				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryGPCE0To12.getInstance();
//			}
//			},		
		VF_PDB_GPCE_DYNAMIC {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryGPCEDynamic.getInstance();
			}
		},
		VF_PDB_PERSISTENT_BLEEDING_EDGE {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryBleedingEdge.getInstance();
			}
		},
		VF_PDB_PERSISTENT_SPECIALIZED {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactorySpecialization.getInstance();
			}
		},		
		VF_PDB_PERSISTENT_UNTYPED {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactorySpecializationWithUntypedVariables.getInstance();
			}
		},		
		VF_PDB_PERSISTENT_CURRENT {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryCurrent.getInstance();
			}
		},
		VF_PDB_PERSISTENT_LAZY {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryLazy.getInstance();
			}
		},
		VF_PDB_PERSISTENT_MEMOIZED {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryMemoized.getInstance();
			}
		},
		VF_PDB_PERSISTENT_MEMOIZED_LAZY {
			@Override
			public IValueFactory getInstance() {
				return org.eclipse.imp.pdb.facts.impl.persistent.TypelessValueFactoryMemoizedLazy.getInstance();
			}
		};

		public abstract IValueFactory getInstance();
	}
	
	public static enum DataType {
		MAP,
		SET
	}	
	
	public static enum SampleDataSelection {
		MATCH,
		RANDOM
	}	
	
	public static int seedFromSizeAndRun(int size, int run) {
		return mix(size) ^ mix(run);
	}
	
	private static int mix(int n) {
		int h = n;

		h *= 0x5bd1e995;
		h ^= h >>> 13;
		h *= 0x5bd1e995;
		h ^= h >>> 15;

		return h;
	}
	
	public static int[] generateSortedArrayWithRandomData(int size, int run) {

		int[] randomNumbers = new int[size];

		int seedForThisTrial = BenchmarkUtils.seedFromSizeAndRun(size, run);
		Random rand = new Random(seedForThisTrial);

		// System.out.println(String.format("Seed for this trial: %d.",
		// seedForThisTrial));

		for (int i = size - 1; i >= 0; i--) {
			randomNumbers[i] = rand.nextInt();
		}

		Arrays.sort(randomNumbers);

		return randomNumbers;
	}

}
