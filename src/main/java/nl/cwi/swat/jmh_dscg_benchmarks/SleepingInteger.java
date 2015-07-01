package nl.cwi.swat.jmh_dscg_benchmarks;

class SleepingInteger {

	private static final int MAX_SLEEP_IN_MILLISECONDS = 100;

	private final int value;

	SleepingInteger(int value) {
		this.value = value;
	}

	private void sleep(int base) {
		try {
			Thread.sleep(base % MAX_SLEEP_IN_MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		sleep(value);
		return value;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof SleepingInteger) {
			int otherValue = ((SleepingInteger) other).value;

			sleep(value);
			sleep(otherValue);

			return value == otherValue;
		}
		return false;
	}

}