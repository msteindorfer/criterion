/**
 * Copyright (c) Michael Steindorfer <Centrum Wiskunde & Informatica> and Contributors.
 * All rights reserved.
 *
 * This file is licensed under the BSD 2-Clause License, which accompanies this project
 * and is available under https://opensource.org/licenses/BSD-2-Clause.
 */
package io.usethesource.criterion;

import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.CompilerControl.Mode;

import io.usethesource.criterion.api.JmhValue;

@CompilerControl(Mode.DONT_INLINE)
public class PureInteger implements JmhValue {

  private int value;

  PureInteger(int value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    return value;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (other == this) {
      return true;
    }

    if (other instanceof PureInteger) {
      int otherValue = ((PureInteger) other).value;

      return value == otherValue;
    }
    return false;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

}
