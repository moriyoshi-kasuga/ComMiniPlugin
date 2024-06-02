package github.moriyoshi.comminiplugin.dependencies.ui.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/** int 値のジェネレーターです。イテレータであることに加え、IntGeneratorは常に初期状態にリセットすることができます。 */
public interface IntGenerator extends PrimitiveIterator.OfInt {

  /**
   * intの固定配列を使ってジェネレータを構築する。
   *
   * @param ints the array
   * @return a new generator
   */
  static IntGenerator of(int... ints) {
    return new ArrayIntGenerator(ints);
  }

  /**
   * 常に同じ値を出力するジェネレータを構築する。
   *
   * @param constant 毎回発する定数です。
   * @return a new generator
   */
  static IntGenerator repeat(int constant) {
    return new ConstantIntGenerator(constant);
  }

  /**
   * 初期値から始まり、更新関数を使って更新するジェネレーターを構築する。
   *
   * @param startValue the seed value
   * @param updater the update function
   * @return a new generator
   */
  static IntGenerator iterate(int startValue, IntUnaryOperator updater) {
    return new InfiniteIntGenerator(startValue, updater);
  }

  /**
   * ある下限値と上限値の間にあるintを生成するジェネレータを構築する。
   *
   * @param startValue the lower bound (inclusive)
   * @param endExclusive the upper bound (exclusive)
   * @param step how much difference between each {@link IntGenerator#nextInt()} call.
   * @return a new generator
   */
  static IntGenerator range(int startValue, int endExclusive, int step) {
    return new RangeIntGenerator(startValue, endExclusive, step);
  }

  /** ジェネレーターを初期状態にリセットする。 */
  void reset();

  /**
   * このジェネレーターをIntStreamに変換する。
   *
   * @return a new IntStream
   */
  default IntStream toStream() {
    return StreamSupport.intStream(
        Spliterators.spliteratorUnknownSize(this, Spliterator.NONNULL | Spliterator.ORDERED),
        false);
  }

  /**
   * このジェネレータに別のジェネレータを連結させる。返されたジェネレータは、最初のジェネレータからアイテムを生成し、
   * 最初のジェネレータが終わったときにだけ、2番目のジェネレータに問い合わせをします。
   *
   * @param another the second generator
   * @return a new generator
   */
  default IntGenerator concat(IntGenerator another) {
    return new ConcatIntGenerator(this, another);
  }

  /**
   * このジェネレーターが終わったら、また自動でやり直すジェネレーターに変換してください。
   *
   * @return a new generator
   */
  default IntGenerator cycled() {
    return new CycleIntGenerator(this);
  }

  /**
   * このジェネレーターのステップ数を制限する。
   *
   * @param limit the limit to the number of steps
   * @return a new generator
   */
  default IntGenerator limit(int limit) {
    return new LimitIntGenerator(this, limit);
  }

  /**
   * 生成された値を別の値にマッピングする。
   *
   * @param mapper a pure function from int to int
   * @return a new generator
   */
  default IntGenerator map(IntUnaryOperator mapper) {
    return new MapIntGenerator(this, mapper);
  }
}

class MapIntGenerator implements IntGenerator {

  private final IntGenerator source;
  private final IntUnaryOperator mapper;

  MapIntGenerator(IntGenerator source, IntUnaryOperator mapper) {
    this.source = Objects.requireNonNull(source, "source cannot be null");
    this.mapper = Objects.requireNonNull(mapper, "mapper cannot be null");
  }

  @Override
  public void reset() {
    source.reset();
  }

  @Override
  public IntStream toStream() {
    return source.toStream().map(mapper);
  }

  @Override
  public IntGenerator map(IntUnaryOperator mapper) {
    return new MapIntGenerator(source, this.mapper.andThen(mapper));
  }

  @Override
  public int nextInt() {
    return mapper.applyAsInt(source.nextInt());
  }

  @Override
  public boolean hasNext() {
    return source.hasNext();
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, mapper);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof MapIntGenerator that)) {
      return false;
    }

    return Objects.equals(this.source, that.source) && Objects.equals(this.mapper, that.mapper);
  }

  @Override
  public String toString() {
    return "MapIntGenerator(source=" + source + ",mapper=" + mapper + ")";
  }
}

class LimitIntGenerator implements IntGenerator {

  private final int limit;
  private final IntGenerator wrapped;
  private int count = 0;

  private LimitIntGenerator(IntGenerator wrapped, int limit, int count) {
    this.wrapped = wrapped;
    this.limit = limit;
    this.count = count;
  }

  LimitIntGenerator(IntGenerator wrapped, int limit) {
    this.wrapped = Objects.requireNonNull(wrapped, "wrapped cannot be null");
    if (limit < 0) {
      throw new IllegalArgumentException("negative limit: " + limit);
    }
    this.limit = limit;
  }

  @Override
  public void reset() {
    wrapped.reset();
    count = 0;
  }

  @Override
  public IntStream toStream() {
    return wrapped.toStream().limit(limit).skip(count);
  }

  @Override
  public IntGenerator limit(int steps) {
    return new LimitIntGenerator(wrapped, Math.min(steps, limit), count);
  }

  @Override
  public IntGenerator map(IntUnaryOperator mapper) {
    return new LimitIntGenerator(wrapped.map(mapper), limit, count);
  }

  @Override
  public int nextInt() {
    count += 1;
    return wrapped.nextInt();
  }

  @Override
  public boolean hasNext() {
    return count < limit && wrapped.hasNext();
  }

  @Override
  public int hashCode() {
    return Objects.hash(limit, wrapped, count);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof LimitIntGenerator that)) {
      return false;
    }

    return this.limit == that.limit
        && Objects.equals(this.wrapped, that.wrapped)
        && this.count == that.count;
  }

  @Override
  public String toString() {
    return "LimitIntGenerator(limit=" + limit + ",wrapped=" + wrapped + ",count=" + count + ")";
  }
}

class RangeIntGenerator implements IntGenerator {

  private final int start, end, step;

  private int state;

  RangeIntGenerator(int start, int end, int step) {
    if (start >= end) {
      throw new IllegalArgumentException("start must be lower than end");
    }
    if (step <= 0) {
      throw new IllegalArgumentException("step must be positive");
    }

    this.start = start;
    this.end = end;
    this.step = step;

    state = start;
  }

  @Override
  public void reset() {
    state = start;
  }

  @Override
  public IntStream toStream() {
    return IntStream.iterate(start, i -> i < end, i -> i + step);
  }

  @Override
  public int nextInt() {
    int i = state;
    state += step;
    return i;
  }

  @Override
  public boolean hasNext() {
    return state < end;
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end, step);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof RangeIntGenerator that)) {
      return false;
    }

    return this.start == that.start && this.end == that.end && this.step == that.step;
  }

  @Override
  public String toString() {
    return "RangeIntGenerator(start=" + start + ",end=" + end + ",step=" + step + ")";
  }
}

class ConstantIntGenerator implements IntGenerator {

  private final int value;

  ConstantIntGenerator(int value) {
    this.value = value;
  }

  @Override
  public void reset() {}

  @Override
  public IntStream toStream() {
    return IntStream.generate(() -> value);
  }

  @Override
  public IntGenerator concat(IntGenerator another) {
    return this;
  }

  @Override
  public IntGenerator cycled() {
    return this;
  }

  @Override
  public IntGenerator map(IntUnaryOperator mapper) {
    return new ConstantIntGenerator(mapper.applyAsInt(value));
  }

  @Override
  public int nextInt() {
    return value;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public int hashCode() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ConstantIntGenerator that)) {
      return false;
    }

    return this.value == that.value;
  }

  @Override
  public String toString() {
    return "ConstantIntGenerator(value=" + value + ")";
  }
}

class InfiniteIntGenerator implements IntGenerator {

  private final int startValue;
  private final IntUnaryOperator updater;
  private int state;

  InfiniteIntGenerator(int startValue, IntUnaryOperator updater) {
    Objects.requireNonNull(updater, "updater cannot be null");

    this.updater = updater;
    this.state = this.startValue = startValue;
  }

  @Override
  public void reset() {
    state = startValue;
  }

  @Override
  public IntStream toStream() {
    return IntStream.iterate(startValue, updater);
  }

  @Override
  public IntGenerator concat(IntGenerator another) {
    return this;
  }

  @Override
  public IntGenerator cycled() {
    return this;
  }

  @Override
  public int nextInt() {
    int value = state;
    state = updater.applyAsInt(state);
    return value;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(startValue, updater, state);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof InfiniteIntGenerator that)) {
      return false;
    }

    return this.startValue == that.startValue
        && Objects.equals(this.updater, that.updater)
        && this.state == that.state;
  }

  @Override
  public String toString() {
    return "InfiniteIntGenerator(startValue="
        + startValue
        + ",updater="
        + updater
        + ",state="
        + state
        + ")";
  }
}

class CycleIntGenerator implements IntGenerator {

  private final IntGenerator wrapped;

  CycleIntGenerator(IntGenerator wrapped) {
    this.wrapped = Objects.requireNonNull(wrapped, "wrapped cannot be null");
  }

  @Override
  public void reset() {
    wrapped.reset();
  }

  @Override
  public IntGenerator concat(IntGenerator another) {
    return this;
  }

  @Override
  public IntGenerator cycled() {
    return this;
  }

  @Override
  public IntGenerator map(IntUnaryOperator mapper) {
    return new CycleIntGenerator(wrapped.map(mapper));
  }

  @Override
  public int nextInt() {
    int next = wrapped.nextInt();
    if (!wrapped.hasNext()) {
      wrapped.reset();
    }
    return next;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public int hashCode() {
    return wrapped.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof CycleIntGenerator that)) {
      return false;
    }

    return Objects.equals(this.wrapped, that.wrapped);
  }

  @Override
  public String toString() {
    return "CycleIntGenerator(wrapped=" + wrapped + ")";
  }
}

class ConcatIntGenerator implements IntGenerator {

  private final IntGenerator first, second;

  ConcatIntGenerator(IntGenerator first, IntGenerator second) {
    this.first = Objects.requireNonNull(first, "first cannot be null");
    this.second = Objects.requireNonNull(second, "second cannot be null");
  }

  @Override
  public void reset() {
    first.reset();
    second.reset();
  }

  @Override
  public IntStream toStream() {
    return IntStream.concat(first.toStream(), second.toStream());
  }

  @Override
  public IntGenerator map(IntUnaryOperator mapper) {
    return new ConcatIntGenerator(first.map(mapper), second.map(mapper));
  }

  @Override
  public int nextInt() {
    if (first.hasNext()) {
      return first.nextInt();
    }
    return second.nextInt();
  }

  @Override
  public boolean hasNext() {
    return first.hasNext() || second.hasNext();
  }

  @Override
  public int hashCode() {
    return Objects.hash(first, second);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ConcatIntGenerator that)) {
      return false;
    }

    return Objects.equals(this.first, that.first) && Objects.equals(this.second, that.second);
  }

  @Override
  public String toString() {
    return "ConcatIntGenerator(first=" + first + ",second=" + second + ")";
  }
}

class ArrayIntGenerator implements IntGenerator {

  private final int[] ints;
  private int index = 0;

  private ArrayIntGenerator(int index, int[] ints) {
    this.index = index;
    this.ints = ints;
  }

  ArrayIntGenerator(int... ints) {
    this.ints = Objects.requireNonNull(ints, "ints cannot be null");
  }

  @Override
  public void reset() {
    this.index = 0;
  }

  @Override
  public IntStream toStream() {
    IntStream stream = IntStream.of(ints);
    if (index != 0) {
      stream = stream.skip(index);
    }
    return stream;
  }

  @Override
  public IntGenerator concat(IntGenerator another) {
    if (another instanceof ArrayIntGenerator that) {

      int[] ints = new int[this.ints.length + that.ints.length];
      System.arraycopy(this.ints, 0, ints, 0, this.ints.length);
      System.arraycopy(that.ints, 0, ints, this.ints.length, that.ints.length);
      return new ArrayIntGenerator(index, ints);
    } else {
      return IntGenerator.super.concat(another);
    }
  }

  @Override
  public IntGenerator limit(int limit) {
    if (limit >= this.ints.length) {
      return this;
    }

    int[] ints = new int[limit];
    System.arraycopy(this.ints, 0, ints, 0, limit);
    return new ArrayIntGenerator(index, ints);
  }

  @SuppressWarnings("MismatchedReadAndWriteOfArray")
  @Override
  public IntGenerator map(IntUnaryOperator mapper) {
    int[] mapped = new int[ints.length];
    for (int i = 0; i < ints.length; i++) {
      mapped[i] = mapper.applyAsInt(ints[i]);
    }
    return new ArrayIntGenerator(index, ints);
  }

  @Override
  public int nextInt() {
    return ints[index++];
  }

  @Override
  public boolean hasNext() {
    return index < ints.length;
  }

  @Override
  public int hashCode() {
    return Objects.hash(index, Arrays.hashCode(ints));
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ArrayIntGenerator that)) {
      return false;
    }

    return this.index == that.index && Arrays.equals(this.ints, that.ints);
  }

  @Override
  public String toString() {
    return "ArrayIntGenerator(index=" + index + ",ints=" + Arrays.toString(ints) + ")";
  }
}
