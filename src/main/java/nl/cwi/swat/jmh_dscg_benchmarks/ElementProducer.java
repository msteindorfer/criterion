package nl.cwi.swat.jmh_dscg_benchmarks;

import org.eclipse.imp.pdb.facts.IValue;

public enum ElementProducer {

	PDB_INTEGER {
		@Override
		public IValue createFromInt(int value) {
			return org.eclipse.imp.pdb.facts.impl.primitive.IntegerValue.newInteger(value);
		}
	},
	PURE_INTEGER {
		@Override
		public IValue createFromInt(int value) {
			return new PureInteger(value);
		}
	},
	SLEEPING_INTEGER {
		@Override
		public IValue createFromInt(int value) {
			return new SleepingInteger(value);
		}
	},
	COUNTING_INTEGER {
		@Override
		public IValue createFromInt(int value) {
			return new CountingInteger(value);
		}
	};

	public abstract IValue createFromInt(int value);

}