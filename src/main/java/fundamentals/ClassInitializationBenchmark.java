/*******************************************************************************
 * Copyright (c) 2015 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package fundamentals;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jol.info.ClassLayout;

@SuppressWarnings("restriction")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class ClassInitializationBenchmark {

	protected static final sun.misc.Unsafe unsafe;

	static {
		try {
			Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			unsafe = (sun.misc.Unsafe) field.get(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private int nodeMap = 0;
	private int dataMap = 0;

	private Object key1 = null;
	private Object val1 = null;
	private Object key2 = null;
	private Object val2 = null;
	
	long nodeMapOffsetOffset = 0;
	long dataMapOffsetOffset = 0;
	long arrayOffsetsOffset = 0;
		
	@Setup
	public void initialize() {
		final Random rand = new Random();

		this.nodeMap = rand.nextInt();
		this.dataMap = rand.nextInt();
		this.key1 = rand.nextInt();
		this.val1 = rand.nextInt();
		this.key2 = rand.nextInt();
		this.val2 = rand.nextInt();

		final Class<Map2To0Node> dstClass = Map2To0Node.class;
		
		try {
			nodeMapOffsetOffset = 
					unsafe.staticFieldOffset(dstClass.getDeclaredField("nodeMapOffset"));

			dataMapOffsetOffset = 
					unsafe.staticFieldOffset(dstClass.getDeclaredField("dataMapOffset"));

			arrayOffsetsOffset = 
					unsafe.staticFieldOffset(dstClass.getDeclaredField("arrayOffsets"));
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Benchmark
	public Object timeClassInstanziation_Constructor() {
		return new Map2To0Node(nodeMap, dataMap, key1, val1, key2, val2);
	}

	@Benchmark
	public Object timeClassInstanziation_Unsafe_ConstantOffsets() {
		try {
			final Class<Map2To0Node> dstClass = Map2To0Node.class;

			final Map2To0Node dst = (Map2To0Node) (unsafe.allocateInstance(dstClass));

			unsafe.putInt(dst, 12L, nodeMap);
			unsafe.putInt(dst, 16L, dataMap);
			unsafe.putObject(dst, 20L, key1);
			unsafe.putObject(dst, 24L, val1);
			unsafe.putObject(dst, 28L, key2);
			unsafe.putObject(dst, 32L, val2);

			return dst;
		} catch (InstantiationException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Benchmark
	public Object timeClassInstanziation_Unsafe_RunningOffsets() {
		try {
			final Class<Map2To0Node> dstClass = Map2To0Node.class;

			final Map2To0Node dst = (Map2To0Node) (unsafe.allocateInstance(dstClass));

			long offset = 12L;

			unsafe.putInt(dst, offset += 0, nodeMap);
			unsafe.putInt(dst, offset += 4, dataMap);
			unsafe.putObject(dst, offset += 4, key1);
			unsafe.putObject(dst, offset += 4, val1);
			unsafe.putObject(dst, offset += 4, key2);
			unsafe.putObject(dst, offset += 4, val2);

			return dst;
		} catch (InstantiationException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Benchmark
	public Object timeClassInstanziation_Unsafe_OffsetLookup1() {
		try {
			final Class<Map2To0Node> dstClass = Map2To0Node.class;

			final Map2To0Node dst = (Map2To0Node) (unsafe.allocateInstance(dstClass));

			final long dstNodeMapOffset = unsafe.getLong(dstClass,
					unsafe.staticFieldOffset(dstClass.getDeclaredField("nodeMapOffset")));

			final long dstDataMapOffset = unsafe.getLong(dstClass,
					unsafe.staticFieldOffset(dstClass.getDeclaredField("dataMapOffset")));

			final long[] dstArrayOffsets = (long[]) unsafe.getObject(dstClass,
					unsafe.staticFieldOffset(dstClass.getDeclaredField("arrayOffsets")));

			unsafe.putInt(dst, dstNodeMapOffset, nodeMap);
			unsafe.putInt(dst, dstDataMapOffset, dataMap);
			unsafe.putObject(dst, dstArrayOffsets[0], key1);
			unsafe.putObject(dst, dstArrayOffsets[1], val1);
			unsafe.putObject(dst, dstArrayOffsets[2], key2);
			unsafe.putObject(dst, dstArrayOffsets[3], val2);

			return dst;
		} catch (InstantiationException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Benchmark
	public Object timeClassInstanziation_Unsafe_OffsetLookup2() {
		try {
			final Class<Map2To0Node> dstClass = Map2To0Node.class;

			final Map2To0Node dst = (Map2To0Node) (unsafe.allocateInstance(dstClass));

			final long dstNodeMapOffset = unsafe.getLong(dstClass, nodeMapOffsetOffset);

			final long dstDataMapOffset = unsafe.getLong(dstClass, dataMapOffsetOffset);

			final long[] dstArrayOffsets = (long[]) unsafe.getObject(dstClass, arrayOffsetsOffset);

			unsafe.putInt(dst, dstNodeMapOffset, nodeMap);
			unsafe.putInt(dst, dstDataMapOffset, dataMap);
			unsafe.putObject(dst, dstArrayOffsets[0], key1);
			unsafe.putObject(dst, dstArrayOffsets[1], val1);
			unsafe.putObject(dst, dstArrayOffsets[2], key2);
			unsafe.putObject(dst, dstArrayOffsets[3], val2);

			return dst;
		} catch (InstantiationException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@SuppressWarnings("unused")
	static class Base {

		Base(final int nodeMap, final int dataMap) {
			this.nodeMap = nodeMap;
			this.dataMap = dataMap;
		}

		private int nodeMap = 0;
		private int dataMap = 0;

		// private int testReorderInt = 0;
	}

	@SuppressWarnings("unused")
	static class Map2To0Node extends Base {

		Map2To0Node(final int nodeMap, final int dataMap, final Object key1, final Object val1,
				final Object key2, final Object val2) {
			super(nodeMap, dataMap);

			// this.nodeMap = nodeMap;
			// this.dataMap = dataMap;

			this.key1 = key1;
			this.val1 = val1;
			this.key2 = key2;
			this.val2 = val2;
		}

		// private int nodeMap = 0;
		// private int dataMap = 0;

		private Object key1 = null;
		private Object val1 = null;
		private Object key2 = null;
		private Object val2 = null;

		// private long testReorderLong0 = 0;
		// private byte testReorderByte0 = 0;
		// private byte testReorderByte1 = 0;
		// private int testReorderInt = 0;
		// private long testReorderLong1 = 0;

		private static final long nodeMapOffset = bitmapOffset(Map2To0Node.class, "nodeMap");

		private static final long dataMapOffset = bitmapOffset(Map2To0Node.class, "dataMap");

		private static final long[] arrayOffsets = arrayOffsets(Map2To0Node.class, new String[] {
				"key1", "val1", "key2", "val2" });

		@SuppressWarnings("rawtypes")
		static final long[] arrayOffsets(final Class clazz, final String[] fieldNames) {
			try {
				long[] arrayOffsets = new long[fieldNames.length];

				for (int i = 0; i < fieldNames.length; i++) {
					arrayOffsets[i] = unsafe.objectFieldOffset(clazz
							.getDeclaredField(fieldNames[i]));
				}

				return arrayOffsets;
			} catch (NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}

		@SuppressWarnings("rawtypes")
		static final long bitmapOffset(final Class clazz, final String bitmapName) {
			try {
				List<Class> bottomUpHierarchy = new LinkedList<>();

				Class currentClass = clazz;
				while (currentClass != null) {
					bottomUpHierarchy.add(currentClass);
					currentClass = currentClass.getSuperclass();
				}

				final java.util.Optional<Field> bitmapNameField = bottomUpHierarchy.stream()
						.flatMap(hierarchyClass -> Stream.of(hierarchyClass.getDeclaredFields()))
						.filter(f -> f.getName().equals(bitmapName)).findFirst();

				if (bitmapNameField.isPresent()) {
					return unsafe.objectFieldOffset(bitmapNameField.get());
				} else {
					return sun.misc.Unsafe.INVALID_FIELD_OFFSET;
				}
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			}
		}

	}

	public static void main(String[] args) throws RunnerException {
		System.out.println(ClassInitializationBenchmark.class.getSimpleName());
		Options opt = new OptionsBuilder()
				.include(".*" + ClassInitializationBenchmark.class.getSimpleName() + ".*")
				.timeUnit(TimeUnit.NANOSECONDS).forks(1).mode(Mode.AverageTime).warmupIterations(5)
				.measurementIterations(5).build();

		new Runner(opt).run();

		System.out.println(ClassLayout.parseClass(Map2To0Node.class).toPrintable());
	}

}
