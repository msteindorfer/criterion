package io.usethesource.criterion;

import io.usethesource.criterion.api.JmhValue;

public enum ElementProducer {

	PDB_INTEGER {
		@Override
		public JmhValue createFromInt(int value) {
			return new PureIntegerWithCustomHashCode(value);
		}
	},
	PURE_INTEGER {
		@Override
		public JmhValue createFromInt(int value) {
			return new PureInteger(value);
		}
	},
	SLEEPING_INTEGER {
		@Override
		public JmhValue createFromInt(int value) {
			return new SleepingInteger(value);
		}
	},
	COUNTING_INTEGER {
		@Override
		public JmhValue createFromInt(int value) {
			return new CountingInteger(value);
		}
	};

	public abstract JmhValue createFromInt(int value);

}