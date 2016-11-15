/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package fundamentals;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
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
import org.openjdk.jol.util.VMSupport;

import io.usethesource.capsule.util.RangecopyUtils;

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

  private Object[] src = null;
  private Map2To0Node srcClassInstance;
  private Map2To0NodeAlt srcClassAltInstance;

  long nodeMapOffsetOffset = 0;
  long dataMapOffsetOffset = 0;
  long arrayOffsetsOffset = 0;

  long firstFieldOffset = 0;
  long addressSize = 0;

  @Setup
  public void initialize() {
    final Random rand = new Random();

    this.nodeMap = rand.nextInt();
    this.dataMap = rand.nextInt();
    this.key1 = rand.nextInt();
    this.val1 = rand.nextInt();
    this.key2 = rand.nextInt();
    this.val2 = rand.nextInt();

    this.src = new Object[] {key1, val1, key2, val2};

    final Class<Map2To0Node> dstClass = Map2To0Node.class;
    this.srcClassInstance = new Map2To0Node(nodeMap, dataMap, key1, val1, key2, val2);

    final Class<Map2To0NodeAlt> dstClassAlt = Map2To0NodeAlt.class;
    this.srcClassAltInstance = new Map2To0NodeAlt(nodeMap, dataMap, key1, val1, key2, val2);

    try {

      nodeMapOffsetOffset = unsafe.staticFieldOffset(dstClass.getDeclaredField("nodeMapOffset"));

      dataMapOffsetOffset = unsafe.staticFieldOffset(dstClass.getDeclaredField("dataMapOffset"));

      arrayOffsetsOffset = unsafe.staticFieldOffset(dstClass.getDeclaredField("arrayOffsets"));

      unsafe.staticFieldOffset(dstClassAlt.getDeclaredField("nodeMapOffset"));

      unsafe.staticFieldOffset(dstClassAlt.getDeclaredField("dataMapOffset"));

      unsafe.staticFieldOffset(dstClassAlt.getDeclaredField("arrayOffsets"));

      /**************************************************************************/

      firstFieldOffset = unsafe.getLong(dstClass, nodeMapOffsetOffset);

      final long[] dstArrayOffsets = (long[]) unsafe.getObject(dstClass, arrayOffsetsOffset);

      // assuems that both are of type Object and next to each other in memory
      addressSize = dstArrayOffsets[1] - dstArrayOffsets[0];

      /**************************************************************************/

      // System.out.println(ClassLayout.parseClass(Map2To0Node.class).toPrintable());

    } catch (NoSuchFieldException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  boolean ensure(Object o) {
    Map2To0Node that = (Map2To0Node) o;

    if (that.nodeMap != this.nodeMap) {
      return false;
    }
    if (that.dataMap != this.dataMap) {
      return false;
    }

    if (!that.key1.equals(this.key1)) {
      return false;
    }
    if (!that.val1.equals(this.val1)) {
      return false;
    }
    if (!that.key2.equals(this.key2)) {
      return false;
    }
    if (!that.val2.equals(this.val2)) {
      return false;
    }

    return true;
  }

  static MethodHandle MH_constructor;

  static {
    try {
      // NOTE: lookup code only considers public constructors
      MH_constructor =
          MethodHandles.lookup().unreflectConstructor(Map2To0Node.class.getConstructors()[0]);
    } catch (IllegalAccessException | SecurityException e) {
    }
  }

  @Benchmark
  public Object timeClassInstanziation_MethodHandle() {
    try {
      Map2To0Node newInstance = (Map2To0Node) MethodHandles
          .insertArguments(MH_constructor, 0, nodeMap, dataMap, key1, val1, key2, val2)
          .invokeExact();
      assert ensure(newInstance);
      return newInstance;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Benchmark
  public Object timeClassInstanziation_MethodHandle_invokeWithArguments() {
    try {
      Object newInstance =
          MH_constructor.invokeWithArguments(nodeMap, dataMap, key1, val1, key2, val2);
      assert ensure(newInstance);
      return newInstance;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Benchmark
  public Object timeClassInstanziation_MethodHandle_invoke() {
    try {
      Object newInstance = MH_constructor.invoke(nodeMap, dataMap, key1, val1, key2, val2);
      assert ensure(newInstance);
      return newInstance;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Benchmark
  public Object timeClassInstanziation_MethodHandle_invokeExact() {
    try {
      Map2To0Node newInstance =
          (Map2To0Node) MH_constructor.invokeExact(nodeMap, dataMap, key1, val1, key2, val2);
      assert ensure(newInstance);
      return newInstance;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Benchmark
  public Object timeClassInstanziation_Constructor() {
    Object result = new Map2To0Node(nodeMap, dataMap, key1, val1, key2, val2);
    assert ensure(result);
    return result;
  }

  // @formatter:off
  /*
   * Crashes with -XX:-UseCompressedOops.
   */
  @Benchmark
  public Object timeClassInstanziation_Unsafe_ConstantOffsets_32bit() {
    try {
      final Class<Map2To0Node> dstClass = Map2To0Node.class;

      final Map2To0Node dst = (Map2To0Node) (unsafe.allocateInstance(dstClass));

      unsafe.putInt(dst, 12L, nodeMap);
      unsafe.putInt(dst, 16L, dataMap);
      // assumes no padding
      unsafe.putObject(dst, 20L, key1);
      unsafe.putObject(dst, 24L, val1);
      unsafe.putObject(dst, 28L, key2);
      unsafe.putObject(dst, 32L, val2);

      assert ensure(dst);
      return dst;
    } catch (InstantiationException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }
  // @formatter:on

  // @formatter:off
  /*
   * Crashes with -XX:+UseCompressedOops.
   */
  @Benchmark
  public Object timeClassInstanziation_Unsafe_ConstantOffsets_64bit() {
    try {
      final Class<Map2To0Node> dstClass = Map2To0Node.class;

      final Map2To0Node dst = (Map2To0Node) (unsafe.allocateInstance(dstClass));

      unsafe.putInt(dst, 16L, nodeMap);
      unsafe.putInt(dst, 20L, dataMap);
      // assumes no padding
      unsafe.putObject(dst, 24L, key1);
      unsafe.putObject(dst, 32L, val1);
      unsafe.putObject(dst, 40L, key2);
      unsafe.putObject(dst, 48L, val2);

      assert ensure(dst);
      return dst;
    } catch (InstantiationException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }
  // @formatter:on

  @Benchmark
  public Object timeClassInstanziation_Unsafe_RunningOffsets() {
    try {
      final Class<Map2To0Node> dstClass = Map2To0Node.class;

      final Map2To0Node dst = (Map2To0Node) (unsafe.allocateInstance(dstClass));

      long offset = this.firstFieldOffset;
      long addressSize = this.addressSize;

      unsafe.putInt(dst, offset, nodeMap);
      offset += 4;
      unsafe.putInt(dst, offset, dataMap);
      offset += 4;
      // assumes no padding
      unsafe.putObject(dst, offset, key1);
      offset += addressSize;
      unsafe.putObject(dst, offset, val1);
      offset += addressSize;
      unsafe.putObject(dst, offset, key2);
      offset += addressSize;
      unsafe.putObject(dst, offset, val2);
      offset += addressSize;

      assert ensure(dst);
      return dst;
    } catch (InstantiationException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  @Benchmark
  public Object timeClassInstanziation_Unsafe_RunningOffsetsVarargs() {
    try {
      final Class<Map2To0Node> dstClass = Map2To0Node.class;

      final Map2To0Node dst = (Map2To0Node) (unsafe.allocateInstance(dstClass));

      long offset = this.firstFieldOffset;
      long addressSize = this.addressSize;

      Object[] args = new Object[] {key1, val1, key2, val2};

      unsafe.putInt(dst, offset, nodeMap);
      offset += 4;
      unsafe.putInt(dst, offset, dataMap);
      offset += 4;
      // assumes no padding
      for (Object item : args) {
        unsafe.putObject(dst, offset, item);
        offset += addressSize;
      }

      assert ensure(dst);
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
      // works in presence of padding
      int slot = 0;
      unsafe.putObject(dst, dstArrayOffsets[slot], key1);
      slot++;
      unsafe.putObject(dst, dstArrayOffsets[slot], val1);
      slot++;
      unsafe.putObject(dst, dstArrayOffsets[slot], key2);
      slot++;
      unsafe.putObject(dst, dstArrayOffsets[slot], val2);
      slot++;

      assert ensure(dst);
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
      // works in presence of padding
      int slot = 0;
      unsafe.putObject(dst, dstArrayOffsets[slot], key1);
      slot++;
      unsafe.putObject(dst, dstArrayOffsets[slot], val1);
      slot++;
      unsafe.putObject(dst, dstArrayOffsets[slot], key2);
      slot++;
      unsafe.putObject(dst, dstArrayOffsets[slot], val2);
      slot++;

      assert ensure(dst);
      return dst;
    } catch (InstantiationException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  public int RANGECOPY_SIZE = 4; // 0 <= RANGECOPY_SIZE <= 4
  public int RANGECOPY_COUNT = 512;

  @Benchmark
  public Object timeRangecopy_Array_SystemArraycopy() {
    Object dst = new Object[RANGECOPY_SIZE];

    for (int i = 0; i < RANGECOPY_COUNT; i++) {
      System.arraycopy(src, 0, dst, 0, RANGECOPY_SIZE);
    }

    return dst;
  }

  // @Benchmark
  // public Object timeRangecopy_Array_UnsafeCopyMemory() {
  // Object dst = new Object[RANGECOPY_SIZE];
  //
  // for (int i = 0; i < RANGECOPY_COUNT; i++)
  // unsafe.copyMemory(src, 16L, dst, 16L, RANGECOPY_SIZE * 4); // assume addressSize == 4
  //
  // return dst;
  // }

  @Benchmark
  public Object timeRangecopy_ObjectRegionThrows_ConstantOffsets_32bit()
      throws InstantiationException {
    Object src = srcClassInstance;
    Object dst = unsafe.allocateInstance(src.getClass());

    for (int i = 0; i < RANGECOPY_COUNT; i++) {
      RangecopyUtils.rangecopyObjectRegion(src, 20L, dst, 20L, RANGECOPY_SIZE);
    }

    return dst;
  }

  // @Benchmark
  // public Object timeRangecopy_ObjectRegionThrows_UnsafeCopyMemory_ConstantOffsets_32bit() throws
  // InstantiationException {
  // Object src = srcClassInstance;
  // Object dst = unsafe.allocateInstance(src.getClass());
  //
  // for (int i = 0; i < RANGECOPY_COUNT; i++)
  // unsafe.copyMemory(src, 20L, dst, 20L, RANGECOPY_SIZE * 4); // assume addressSize == 4
  //
  // return dst;
  // }

  // @Benchmark
  public Object timeRangecopy_ObjectRegionCatches_ConstantOffsets_32bit() {
    try {
      return timeRangecopy_ObjectRegionThrows_ConstantOffsets_32bit();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    }
  }

  static class Base {

    Base(final int nodeMap, final int dataMap) {
      this.nodeMap = nodeMap;
      this.dataMap = dataMap;
    }

    protected int nodeMap = 0;
    protected int dataMap = 0;

    // private int testReorderInt = 0;

    @SuppressWarnings("rawtypes")
    static final long[] arrayOffsets(final Class clazz, final String[] fieldNames) {
      try {
        long[] arrayOffsets = new long[fieldNames.length];

        for (int i = 0; i < fieldNames.length; i++) {
          arrayOffsets[i] = unsafe.objectFieldOffset(clazz.getDeclaredField(fieldNames[i]));
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

  static class Map2To0Node extends Base {

    public Map2To0Node(final int nodeMap, final int dataMap, final Object key1, final Object val1,
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

    @SuppressWarnings("unused")
    private static final long nodeMapOffset = bitmapOffset(Map2To0Node.class, "nodeMap");

    @SuppressWarnings("unused")
    private static final long dataMapOffset = bitmapOffset(Map2To0Node.class, "dataMap");

    @SuppressWarnings("unused")
    private static final long[] arrayOffsets =
        arrayOffsets(Map2To0Node.class, new String[] {"key1", "val1", "key2", "val2"});

  }

  @SuppressWarnings("unused")
  static class Map2To0NodeAlt extends Base {

    public Map2To0NodeAlt(final int nodeMap, final int dataMap, final Object key1,
        final Object val1, final Object key2, final Object val2) {
      super(nodeMap, dataMap);

      // this.nodeMap = nodeMap;
      // this.dataMap = dataMap;

    }

    // private int nodeMap = 0;
    // private int dataMap = 0;

    

    // private long testReorderLong0 = 0;
    // private byte testReorderByte0 = 0;
    // private byte testReorderByte1 = 0;
    // private int testReorderInt = 0;
    // private long testReorderLong1 = 0;

    private static final long nodeMapOffset = bitmapOffset(Map2To0Node.class, "nodeMap");

    private static final long dataMapOffset = bitmapOffset(Map2To0Node.class, "dataMap");

    private static final long[] arrayOffsets =
        arrayOffsets(Map2To0Node.class, new String[] {"key1", "val1", "key2", "val2"});

  }

  public static void main(String[] args) throws RunnerException {
    System.out.println(ClassInitializationBenchmark.class.getSimpleName());
    Options opt = new OptionsBuilder()
        .include(".*" + ClassInitializationBenchmark.class.getSimpleName() + ".*timeRangecopy_.*")
        .timeUnit(TimeUnit.NANOSECONDS).forks(1).mode(Mode.AverageTime).warmupIterations(5)
        .measurementIterations(5).build();

    System.out.println(VMSupport.vmDetails());
    System.out.println(ClassLayout.parseClass(Map2To0Node.class).toPrintable());

    new Runner(opt).run();
  }

}
