/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package fundamentals;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.util.VMSupport;

@SuppressWarnings("restriction")
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class BitmapIndexingBenchmark {

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

  private Base mapNode = null;
  private Base2 mapNodeBase2 = null;

  private int keyHash;

  long nodeMapOffsetOffset = 0;
  long dataMapOffsetOffset = 0;
  long arrayOffsetsOffset = 0;

  long firstFieldOffset = 0;
  long addressSize = 0;

  @Setup
  public void initialize() {
    final Random rand = new Random(153);

    this.nodeMap = rand.nextInt();
    this.dataMap = rand.nextInt();
    this.key1 = rand.nextInt();
    this.val1 = rand.nextInt();
    this.key2 = rand.nextInt();
    this.val2 = rand.nextInt();

    // long bitmap = rand.nextLong();
    long bitmap = 0;
    for (int i = 31; i >= 0; i--) {
      long bit1 = (nodeMap >>> i) & 0b1;
      long bit2 = (dataMap >>> i) & 0b1;

      // if (bit1 == 1L && bit2 == 1L) {
      // bit1 = 0;
      // bit2 = 0;
      // }

      bitmap = bitmap << 1;
      bitmap = bitmap | bit1;

      bitmap = bitmap << 1;
      bitmap = bitmap | bit2;
    }

    mapNode = new Map2To0Node(nodeMap, dataMap, key1, val1, key2, val2);
    mapNodeBase2 = new Map2To0NodeBase2(bitmap, key1, val1, key2, val2);

    final Class<Map2To0Node> dstClass = Map2To0Node.class;
    new Map2To0Node(nodeMap, dataMap, key1, val1, key2, val2);

    final Class<Map2To0NodeAlt> dstClassAlt = Map2To0NodeAlt.class;
    new Map2To0NodeAlt(nodeMap, dataMap, key1, val1, key2, val2);

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

  private Random random;

  @Setup(Level.Trial)
  public void setupRandom() {
    random = new Random(157);
  }

  @Setup(Level.Iteration)
  public void setupKeyHash() {
    keyHash = random.nextInt();
    System.out.println(keyHash);
  }

  public static int shiftBound = 10;
  public static int shiftIncrease = 5;

  @Benchmark
  public void timeCaseDistinctionBase_CHAMP(Blackhole bh) {
    for (int shift = 0; shift < shiftBound; shift += shiftIncrease) {
      bh.consume(mapNode.exerciseCaseDistinctionChamp(keyHash, shift));
    }
  }

  @Benchmark
  public void timeCaseDistinctionBase_HETEROGENEOUS_CHAMP_1(Blackhole bh) {
    for (int shift = 0; shift < shiftBound; shift += shiftIncrease) {
      bh.consume(mapNode.exerciseCaseDistinction(keyHash, shift));
    }
  }

  @Benchmark
  public void timeCaseDistinctionBase_HETEROGENEOUS_CHAMP_2(Blackhole bh) {
    for (int shift = 0; shift < shiftBound; shift += shiftIncrease) {
      bh.consume(mapNode.exerciseCaseDistinction2(keyHash, shift));
    }
  }

  @Benchmark
  public void timeCaseDistinctionBase_HETEROGENEOUS_CHAMP_3(Blackhole bh) {
    for (int shift = 0; shift < shiftBound; shift += shiftIncrease) {
      bh.consume(mapNode.exerciseCaseDistinction3(keyHash, shift));
    }
  }

  @Benchmark
  public void timeCaseDistinctionBase_HETEROGENEOUS_IDEA(Blackhole bh) {
    for (int shift = 0; shift < shiftBound; shift += shiftIncrease) {
      bh.consume(mapNodeBase2.exerciseCaseDistinction(keyHash, shift));
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

    public int nodeMap() {
      return rawMap1() ^ rareMap();
    }

    public int dataMap() {
      return rawMap2() ^ rareMap();
    }

    public int rareMap() {
      return rawMap1() & rawMap2();
    }

    public int rawMap1() {
      return nodeMap;
    }

    public int rawMap2() {
      return dataMap;
    }

    static final int mask(final int keyHash, final int shift) {
      return (keyHash >>> shift) & 0b11111;
    }

    static final int bitpos(final int mask) {
      return 1 << mask;
    }

    static final int index(final int bitmap, final int bitpos) {
      return java.lang.Integer.bitCount((bitmap) & (bitpos - 1));
    }

    static final int index(final int bitmap, final int mask, final int bitpos) {
      return ((bitmap) == -1) ? mask : index(bitmap, bitpos);
    }

    public boolean exerciseCaseDistinctionChamp(int keyHash, int shift) {
      final int mask = mask(keyHash, shift);
      final int bitpos = bitpos(mask);

      final int dataMap = rawMap2();
      if (dataMap != 0 && (dataMap == -1 || (dataMap & bitpos) != 0)) {
        final int index = index(dataMap, mask, bitpos);
        return index != 0;
      }

      final int nodeMap = rawMap1();
      if (nodeMap != 0 && (nodeMap == -1 || (nodeMap & bitpos) != 0)) {
        final int index = index(nodeMap, mask, bitpos);
        return index != 0;
      }

      return false;
    }

    public boolean exerciseCaseDistinction(int keyHash, int shift) {
      final int mask = mask(keyHash, shift);
      final int bitpos = bitpos(mask);

      final int dataMap = dataMap();
      if (dataMap != 0 && (dataMap == -1 || (dataMap & bitpos) != 0)) {
        final int index = index(dataMap, mask, bitpos);
        return index != 0;
      }

      final int rareMap = rareMap();
      if (rareMap != 0 && (rareMap == -1 || (rareMap & bitpos) != 0)) {
        final int index = index(rareMap, mask, bitpos);
        return index != 0;
      }

      final int nodeMap = nodeMap();
      if (nodeMap != 0 && (nodeMap == -1 || (nodeMap & bitpos) != 0)) {
        final int index = index(nodeMap, mask, bitpos);
        return index != 0;
      }

      return false;
    }

    public boolean exerciseCaseDistinction2(int keyHash, int shift) {
      final int mask = mask(keyHash, shift);
      final int bitpos = bitpos(mask);

      final int rawMap1 = rawMap1();
      final int rawMap2 = rawMap2();

      boolean inMap1 = (rawMap1 != 0 && (rawMap1 == -1 || (rawMap1 & bitpos) != 0));
      boolean inMap2 = (rawMap2 != 0 && (rawMap2 == -1 || (rawMap2 & bitpos) != 0));

      if (inMap1 && !inMap2) {
        final int rareMap = rawMap1 & rawMap2;
        final int dataMap = rawMap2 ^ rareMap;

        final int index = index(dataMap, mask, bitpos);
        return index != 0;
      }

      if (inMap1 && inMap2) {
        final int rareMap = rawMap1 & rawMap2;

        final int index = index(rareMap, mask, bitpos);
        return index != 0;
      }

      if (!inMap1 && inMap2) {
        final int rareMap = rawMap1 & rawMap2;
        final int nodeMap = rawMap1 ^ rareMap;

        final int index = index(nodeMap, mask, bitpos);
        return index != 0;
      }

      return false;
    }

    public boolean exerciseCaseDistinction3(int keyHash, int shift) {
      final int mask = mask(keyHash, shift);
      final int bitpos = bitpos(mask);

      final int rawMap1 = rawMap1();
      final int rawMap2 = rawMap2();

      int pattern = toContentType(rawMap1, rawMap2, bitpos);

      switch (pattern) {
        case 0b01: {
          // final int rareMap = (int) (rawMap1 & rawMap2);
          // final int dataMap = (int) (rawMap2 ^ rareMap);

          final int index = index(dataMap(), mask, bitpos);
          return index != 0;
        }
        case 0b10: {
          // final int rareMap = (int) (rawMap1 & rawMap2);
          // final int nodeMap = (int) (rawMap1 ^ rareMap);

          final int index = index(nodeMap(), mask, bitpos);
          return index != 0;
        }
        case 0b11: {
          // final int rareMap = (int) (rawMap1 & rawMap2);

          final int index = index(rareMap(), mask, bitpos);
          return index != 0;
        }
        default:
          return false;
      }
    }

  }

  static final int toContentType(int rawMap1, int rawMap2, int bitpos) {
    final boolean inMap1 = (rawMap1 != 0 && (rawMap1 == -1 || (rawMap1 & bitpos) != 0));

    if (inMap1) {
      final boolean inMap2 = (rawMap2 != 0 && (rawMap2 == -1 || (rawMap2 & bitpos) != 0));

      if (inMap2) {
        return 0b11;
      } else {
        return 0b10;
      }
    } else {
      final boolean inMap2 = (rawMap2 != 0 && (rawMap2 == -1 || (rawMap2 & bitpos) != 0));

      if (inMap2) {
        return 0b01;
      } else {
        return 0b00;
      }
    }
  }

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

    Map2To0NodeAlt(final int nodeMap, final int dataMap, final Object key1, final Object val1,
        final Object key2, final Object val2) {
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

  static class Base2 {

    Base2(final long bitmap) {
      this.bitmap = bitmap;
    }

    protected long bitmap = 0L;

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

    public static int emptyConst() {
      return 0b00;
    }

    public static int nodeConst() {
      return 0b10;
    }

    public static int dataConst() {
      return 0b01;
    }

    public static int rareConst() {
      return 0b11;
    }

    public long bitmap() {
      return bitmap;
    }

    static final int mask(final int keyHash, final int shift) {
      return (keyHash >>> shift) & 0b11111;
    }

    // static final int bitpos(final int mask) {
    // return (1 << mask);
    // }

    static final int bitPattern(final long bitmap, final int mask) {
      return (int) ((bitmap >>> (mask * 2)) & 0b11);
    }

    // static final int index(final long bitmap, final int mask, final int pattern) {
    // final long bitpos = 1L << (mask * 2);
    //
    // final long filteredBitmap;
    //
    // if (pattern == 0b01) {
    // filteredBitmap = (bitmap & 0x5555555555555555L) & (((bitmap >> 1) & 0x5555555555555555L) ^
    // 0x5555555555555555L);
    // } else if (pattern == 0b10) {
    // filteredBitmap = ((bitmap & 0x5555555555555555L) ^ 0x5555555555555555L) & ((bitmap >> 1) &
    // 0x5555555555555555L);
    // } else if (pattern == 0b11) {
    // filteredBitmap = (bitmap & 0x5555555555555555L) & ((bitmap >> 1) & 0x5555555555555555L);
    // } else {
    // filteredBitmap = bitmap;
    // }
    //
    // return java.lang.Long.bitCount(filteredBitmap & (bitpos - 1));
    // }

    static final int index01(final long bitmap, final long bitpos) {
      // if (USE_SELF_WRITTEN_POPULATION_COUNT) {
      // return (int) populationCountPattern01(bitmap & (bitpos - 1));
      // } else {
      final long filteredBitmap = (bitmap & 0x5555555555555555L)
          & (((bitmap >> 1) & 0x5555555555555555L) ^ 0x5555555555555555L);
      return java.lang.Long.bitCount(filteredBitmap & (bitpos - 1));
      // }
    }

    static final int index10(final long bitmap, final long bitpos) {
      // if (USE_SELF_WRITTEN_POPULATION_COUNT) {
      // return (int) populationCountPattern10(bitmap & (bitpos - 1));
      // } else {
      final long filteredBitmap = ((bitmap & 0x5555555555555555L) ^ 0x5555555555555555L)
          & ((bitmap >> 1) & 0x5555555555555555L);
      return java.lang.Long.bitCount(filteredBitmap & (bitpos - 1));
      // }
    }

    static final int index11(final long bitmap, final long bitpos) {
      // if (USE_SELF_WRITTEN_POPULATION_COUNT) {
      // return (int) populationCountPattern11(bitmap & (bitpos - 1));
      // } else {
      final long filteredBitmap =
          (bitmap & 0x5555555555555555L) & ((bitmap >> 1) & 0x5555555555555555L);
      return java.lang.Long.bitCount(filteredBitmap & (bitpos - 1));
      // }
    }

    static final long populationCountPattern01(long v) {
      long c = (v & 0x5555555555555555L) & (((v >> 1) & 0x5555555555555555L) ^ 0x5555555555555555L);
      c = (c & 0x3333333333333333L) + ((c >> 2) & 0x3333333333333333L);
      c = (c & 0x0F0F0F0F0F0F0F0FL) + ((c >> 4) & 0x0F0F0F0F0F0F0F0FL);
      c = (c & 0x00FF00FF00FF00FFL) + ((c >> 8) & 0x00FF00FF00FF00FFL);
      c = (c & 0x0000FFFF0000FFFFL) + ((c >> 16) & 0x0000FFFF0000FFFFL);
      return c;
    }

    static final long populationCountPattern10(long v) {
      long c = ((v & 0x5555555555555555L) ^ 0x5555555555555555L) & ((v >> 1) & 0x5555555555555555L);
      c = (c & 0x3333333333333333L) + ((c >> 2) & 0x3333333333333333L);
      c = (c & 0x0F0F0F0F0F0F0F0FL) + ((c >> 4) & 0x0F0F0F0F0F0F0F0FL);
      c = (c & 0x00FF00FF00FF00FFL) + ((c >> 8) & 0x00FF00FF00FF00FFL);
      c = (c & 0x0000FFFF0000FFFFL) + ((c >> 16) & 0x0000FFFF0000FFFFL);
      return c;
    }

    static final long populationCountPattern11(long v) {
      long c = (v & 0x5555555555555555L) & ((v >> 1) & 0x5555555555555555L);
      c = (c & 0x3333333333333333L) + ((c >> 2) & 0x3333333333333333L);
      c = (c & 0x0F0F0F0F0F0F0F0FL) + ((c >> 4) & 0x0F0F0F0F0F0F0F0FL);
      c = (c & 0x00FF00FF00FF00FFL) + ((c >> 8) & 0x00FF00FF00FF00FFL);
      c = (c & 0x0000FFFF0000FFFFL) + ((c >> 16) & 0x0000FFFF0000FFFFL);
      return c;
    }

    State toState(long bitmap, int doubledMask) {
      final long pattern = ((bitmap >>> doubledMask) & 0b11);

      if (pattern == 0b00L) {
        return State.EMPTY;
      } else if (pattern == 0b01L) {
        return State.PAYLOAD;
      } else if (pattern == 0b10L) {
        return State.NODE;
      } else {
        return State.PAYLOAD_RARE;
      }

      // final int pattern = (int) ((bitmap >>> doubledMask) & 0b11);
      //
      // switch (pattern) {
      // case 0b01:
      // return State.PAYLOAD;
      // case 0b10:
      // return State.NODE;
      // case 0b11:
      // return State.PAYLOAD_RARE;
      // default:
      // return State.EMPTY;
      // }
    }

    public boolean exerciseCaseDistinction(int keyHash, int shift) {
      long bitmap = bitmap();

      final int mask = mask(keyHash, shift);
      final int doubledMask = mask << 1;

      final long bitpos = 1L << doubledMask;
      final int pattern = (int) ((bitmap >>> doubledMask) & 0b11);

      switch (pattern) {
        case 0b01:
          return index01(bitmap, bitpos) != 0;
        case 0b10:
          return index10(bitmap, bitpos) != 0;
        case 0b11:
          return index11(bitmap, bitpos) != 0;
        default:
          return false;
      }

      // switch (toState(bitmap, doubledMask)) {
      // case PAYLOAD:
      // return index01(bitmap, bitpos) != 0;
      // case NODE:
      // return index10(bitmap, bitpos) != 0;
      // case PAYLOAD_RARE:
      // return index11(bitmap, bitpos) != 0;
      // default:
      // return false;
      // }
    }

    enum State {
      EMPTY, PAYLOAD, PAYLOAD_RARE, NODE
    }

  }

  static class Map2To0NodeBase2 extends Base2 {

    Map2To0NodeBase2(final long bitmap, final Object key1, final Object val1, final Object key2,
        final Object val2) {
      super(bitmap);

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

    @SuppressWarnings("unused")
    private static final long bitmapOffset = bitmapOffset(Map2To0NodeBase2.class, "bitmap");

    @SuppressWarnings("unused")
    private static final long[] arrayOffsets =
        arrayOffsets(Map2To0NodeBase2.class, new String[] {"key1", "val1", "key2", "val2"});

  }

  public static void main(String[] args) throws RunnerException {
    System.out.println(BitmapIndexingBenchmark.class.getSimpleName());
    Options opt =
        new OptionsBuilder().include(".*" + BitmapIndexingBenchmark.class.getSimpleName() + ".*")
            .timeUnit(TimeUnit.NANOSECONDS).forks(0).mode(Mode.AverageTime).warmupIterations(5)
            .measurementIterations(10).build();

    System.out.println(VMSupport.vmDetails());
    System.out.println(ClassLayout.parseClass(Map2To0Node.class).toPrintable());

    new Runner(opt).run();
  }

}
