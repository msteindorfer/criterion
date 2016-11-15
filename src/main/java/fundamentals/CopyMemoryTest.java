/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package fundamentals;

import java.lang.reflect.Field;
import java.util.Arrays;

@SuppressWarnings("restriction")
public class CopyMemoryTest {

  static final int size = 10;
  static final int[] a = new int[size];
  static final Integer[] A = new Integer[size];

  static final sun.misc.Unsafe unsafe;

  static {
    for (int i = 0; i < size; i++) {
      a[i] = i;
      A[i] = i;
    }

    try {
      Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      unsafe = (sun.misc.Unsafe) field.get(null);
    } catch (Exception e) {
      throw new RuntimeException();
    }
  }

  public void performTestCopyMemory(int[] a, int[] b) throws Exception {
    unsafe.copyMemory(a, 16L, b, 16L, 4L * size);
  }

  public void performTestArraycopy(int[] a, int[] c) throws Exception {
    System.arraycopy(a, 0, c, 0, size);
  }

  public void performTestRangecopyInt(int[] a, int[] d) throws Exception {
    long bytes =
        io.usethesource.capsule.util.RangecopyUtils.rangecopyIntRegion(a, 16L, d, 16L, size);
    DUMP += bytes;
  }

  public void performTestRangecopyObject(Object[] a, Object[] d) throws Exception {
    long bytes =
        io.usethesource.capsule.util.RangecopyUtils.rangecopyObjectRegion(a, 16L, d, 16L, size);
    DUMP += bytes;
  }

  public static long DUMP = 0;

  public static void main(String[] args) throws Exception {
    CopyMemoryTest instance = new CopyMemoryTest();
    boolean areEqual = true;

    do {
      int[] b = new int[size];
      instance.performTestCopyMemory(a, b);
      boolean areEqual1 = Arrays.equals(a, b);
      areEqual = areEqual && areEqual1;

      int[] c = new int[size];
      instance.performTestArraycopy(a, c);
      boolean areEqual2 = Arrays.equals(a, c);
      areEqual = areEqual && areEqual2;

      int[] d = new int[size];
      instance.performTestRangecopyInt(a, d);
      boolean areEqual3 = Arrays.equals(a, d);
      areEqual = areEqual && areEqual3;

      Integer[] D = new Integer[size];
      instance.performTestRangecopyObject(A, D);
      boolean areEqual4 = Arrays.equals(A, D);
      areEqual = areEqual && areEqual4;

      if (!areEqual) {
        System.out.println("Failed!");
        // System.out.println("areEqual1: " + areEqual1);
        // System.out.println("areEqual2: " + areEqual2);
        // System.out.println("areEqual3: " + areEqual3);
        // System.out.println("areEqual4: " + areEqual4);
      }
    } while (areEqual);
  }

  public static class StaticArray10 {
    public int length;
    public int slot0;
    public int slot1;
    public int slot2;
    public int slot3;
    public int slot4;
    public int slot5;
    public int slot6;
    public int slot7;
    public int slot8;
    public int slot9;
  }

}
