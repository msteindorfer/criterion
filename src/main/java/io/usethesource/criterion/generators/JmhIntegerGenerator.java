package io.usethesource.criterion.generators;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import io.usethesource.criterion.PureInteger;
import io.usethesource.criterion.api.JmhValue;

public class JmhIntegerGenerator extends Generator<JmhValue> {

  public JmhIntegerGenerator() {
    super(JmhValue.class);
  }

  @Override
  public JmhValue generate(SourceOfRandomness random, GenerationStatus status) {
    return new PureInteger(random.nextInt());
  }

}
