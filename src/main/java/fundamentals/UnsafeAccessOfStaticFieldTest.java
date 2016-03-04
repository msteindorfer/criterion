package fundamentals;

import java.lang.reflect.Field;

public class UnsafeAccessOfStaticFieldTest {

	public static final boolean COUNT_TRIGGERING_JIT = true;
	public static final boolean USE_EXTENDING_TEMPLATE = true;
	
	private static final sun.misc.Unsafe initializeUnsafe() {
		try {
			Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			return (sun.misc.Unsafe) field.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static final sun.misc.Unsafe unsafe = initializeUnsafe();

	private static final long initializeStaticVariableOffset() {
		try {
			final Field staticVariableField;
			
			if (USE_EXTENDING_TEMPLATE) {
				staticVariableField = StaticVariableHolder.ExtendingTemplate.class
								.getDeclaredField("staticVariable");
			} else {
				staticVariableField = StaticVariableHolder.StandaloneTemplate.class
								.getDeclaredField("staticVariable");
			}
			return unsafe.staticFieldOffset(staticVariableField);
		} catch (Exception e) {
			return -1L;
		}
	}

	private static long staticVariableOffsetNonFinal = initializeStaticVariableOffset();

	private final static long staticVariableOffset = initializeStaticVariableOffset();

	static class StaticVariableHolder {

		private static int staticVariable;

		public StaticVariableHolder() {
			staticVariable = 0;
		}

		public StaticVariableHolder(int value) {
			staticVariable = value;
		}

		public int getStaticVariable() {
			return staticVariable;
		}

		public int getStaticVariableUnsafeWithStaticOffsetNonFinal() {
			return unsafe.getInt(this.getClass(), staticVariableOffsetNonFinal);
		}

		public int getStaticVariableUnsafeWithStaticOffset() {
			return unsafe.getInt(this.getClass(), staticVariableOffset);
		}

		public int getStaticVariableUnsafeWithConstantOffset() {
			return unsafe.getInt(this.getClass(), 104L);
		}

		static class ExtendingTemplate extends StaticVariableHolder {

			public static int staticVariable;

		}

		static class StandaloneTemplate {

			public static int staticVariable;

		}

	}

	public static void main(String[] args) {
		System.out.println("staticVariableOffset: " + staticVariableOffset + "\n");

		int count = COUNT_TRIGGERING_JIT ? 100_000 : 1_000;

		System.out.println("Trying getStaticVariable ... ");
		for (int i = 0; i < count; i++) {
			StaticVariableHolder dltw = new StaticVariableHolder(i);
			System.out.println(dltw.getStaticVariable());
		}
		System.out.println("[SUCCESS]");

		System.out.println("Trying getStaticVariableUnsafeWithStaticOffsetNonFinal ... ");
		for (int i = 0; i < count; i++) {
			StaticVariableHolder dltw = new StaticVariableHolder(i);
			System.out.println(dltw.getStaticVariableUnsafeWithStaticOffsetNonFinal());
		}
		System.out.println("[SUCCESS]");

		System.out.println("Trying getStaticVariableUnsafeWithStaticOffset ... ");
		for (int i = 0; i < count; i++) {
			StaticVariableHolder dltw = new StaticVariableHolder(i);
			System.out.println(dltw.getStaticVariableUnsafeWithStaticOffset());
		}
		System.out.println("[SUCCESS]");

		System.out.println("Trying getStaticVariableUnsafeWithConstantOffset ... ");
		for (int i = 0; i < count; i++) {
			StaticVariableHolder dltw = new StaticVariableHolder(i);
			System.out.println(dltw.getStaticVariableUnsafeWithConstantOffset());
		}
		System.out.println("[SUCCESS]");

		System.out.println("\n>>> ALL FINISHED <<<\n");
	}
}
