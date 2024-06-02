package github.moriyoshi.comminiplugin.dependencies.ui.animate;

import java.util.Arrays;
import java.util.Objects;
import java.util.OptionalLong;

/** アニメーションを再生するためのスケジュールです。 */
public interface Schedule {

  /**
   * ディレイで区切られたフレームを提供するスケジュールです。
   *
   * @param delays フレーム間のディレイ
   * @return 新スケジュール
   */
  static Schedule of(long... delays) {
    if (delays.length == 1) {
      return once(delays[0]);
    }

    for (long delay : delays) {
      if (delay < 0) {
        throw new IllegalArgumentException("Negative delay: " + delay);
      }
    }

    return new ArraySchedule(Arrays.copyOf(delays, delays.length));
  }

  /**
   * 最初の遅延を経て、1フレームのみ提供するスケジュール
   *
   * @param delay 刻みの初期遅延
   * @return 新スケジュール
   */
  static Schedule once(long delay) {
    if (delay < 0L) {
      throw new IllegalArgumentException("Negative delay: " + delay);
    }

    return new OneTimeSchedule(delay);
  }

  /**
   * 1枠しかないスケジュール
   *
   * @return 新スケジュール
   */
  static Schedule now() {
    return once(0L);
  }

  /**
   * フレームを固定料金で提供するスケジュール
   *
   * @param period フレーム間の刻み数
   * @return しんこうけいかく
   */
  static Schedule fixedRate(long period) {
    if (period < 0L) {
      throw new IllegalArgumentException("Negative period: " + period);
    }

    return new FixedRateSchedule(period);
  }

  /** スケジュールを初期状態に戻す。 */
  void reset();

  /**
   * 次のフレームが表示されるまでのティック数を取得する。
   *
   * @return このスケジュールが終了した場合は空のOptionalLong、そうでない場合は次のフレームまでのティック数を含むOptionalLong。
   */
  OptionalLong next();

  /**
   * このスケジュールのコピーを入手する。
   *
   * @return 新スケジュール
   */
  Schedule clone();

  /**
   * 通過したティック数によって制限されたフレーム数を提供するスケジュール。
   *
   * @param totalTicks 通過したティックの上限値
   * @return 新スケジュール
   */
  default Schedule limitTime(long totalTicks) {
    if (totalTicks < 0) {
      throw new IllegalArgumentException("Negative time limit: " + totalTicks);
    }

    return new TimeLimitedSchedule(this, totalTicks);
  }

  /**
   * 通過したコマ数だけ限定してサーバーにかけるスケジュール。
   *
   * @param totalSteps 渡されるフレームの上限値
   * @return 新スケジュール
   */
  default Schedule limitSteps(long totalSteps) {
    if (totalSteps < 0) {
      throw new IllegalArgumentException("Negative step limit: " + totalSteps);
    }

    return new StepLimitedSchedule(this, totalSteps);
  }

  /**
   * まず現在のスケジュールに従ってフレームを提供し、次に第2のスケジュールに従ってフレームを提供するスケジュール。
   *
   * @param andThen にばんめ
   * @return 新しいスケジュール、または現在のスケジュールが無限の場合は、現在のスケジュール
   */
  default Schedule append(Schedule andThen) {
    return new ConcatSchedule(this, andThen);
  }

  /**
   * A schedule that loops the current schedule.
   *
   * @return a new Schedule, or the current schedule if it is infinite
   */
  default Schedule repeat() {
    return new RepeatingSchedule(this);
  }
}

class RepeatingSchedule implements Schedule {

  private final Schedule source;

  RepeatingSchedule(Schedule source) {
    this.source = source;
  }

  @Override
  public void reset() {
    source.reset();
  }

  @Override
  public OptionalLong next() {
    OptionalLong next = source.next();
    if (next.isPresent()) {
      return next;
    }
    source.reset();
    return source.next();
  }

  @Override
  public Schedule append(Schedule andThen) {
    return this;
  }

  @Override
  public Schedule repeat() {
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(source);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof RepeatingSchedule that)) {
      return false;
    }

    return Objects.equals(this.source, that.source);
  }

  @Override
  public Schedule clone() {
    return new RepeatingSchedule(source.clone());
  }

  @Override
  public String toString() {
    return "RepeatingSchedule(source=" + source + ")";
  }
}

class ArraySchedule implements Schedule {

  private final long[] delays;
  private int currentIndex;

  private ArraySchedule(long[] delays, int currentIndex) {
    this.delays = delays;
    this.currentIndex = currentIndex;
  }

  ArraySchedule(long[] delays) {
    this.delays = Objects.requireNonNull(delays, "delays cannot be null");
  }

  @Override
  public void reset() {
    currentIndex = 0;
  }

  @Override
  public OptionalLong next() {
    if (currentIndex >= delays.length) {
      return OptionalLong.empty();
    }

    return OptionalLong.of(delays[currentIndex++]);
  }

  @Override
  public Schedule limitSteps(long totalSteps) {
    if (totalSteps < this.delays.length) {
      int length = this.delays.length;
      long[] delays = new long[length];
      System.arraycopy(this.delays, 0, delays, 0, length);
      return new ArraySchedule(delays, currentIndex);
    } else {
      return this;
    }
  }

  @Override
  public Schedule append(Schedule andThen) {
    if (andThen instanceof ArraySchedule that) {

      long[] delays = new long[this.delays.length + that.delays.length];
      System.arraycopy(this.delays, 0, delays, 0, this.delays.length);
      System.arraycopy(that.delays, 0, delays, this.delays.length, that.delays.length);

      return new ArraySchedule(delays, currentIndex);
    } else {
      return Schedule.super.append(andThen);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(currentIndex, Arrays.hashCode(delays));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ArraySchedule that)) {
      return false;
    }

    return this.currentIndex == that.currentIndex && Arrays.equals(this.delays, that.delays);
  }

  @Override
  public Schedule clone() {
    return new ArraySchedule(Arrays.copyOf(delays, delays.length), currentIndex);
  }

  @Override
  public String toString() {
    return "ArraySchedule(delays="
        + Arrays.toString(delays)
        + ",currentIndex="
        + currentIndex
        + ")";
  }
}

record ConcatSchedule(Schedule one, Schedule two) implements Schedule {

  @Override
  public void reset() {
    one.reset();
    two.reset();
  }

  @Override
  public OptionalLong next() {
    OptionalLong next = one.next();
    if (next.isPresent()) {
      return next;
    }

    next = two.next();
    return next;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ConcatSchedule that)) {
      return false;
    }

    return Objects.equals(this.one, that.one) && Objects.equals(this.two, that.two);
  }

  @Override
  public Schedule clone() {
    return new ConcatSchedule(one.clone(), two.clone());
  }

  @Override
  public String toString() {
    return "ConcatSchedule(one=" + one + ",two=" + two + ")";
  }
}

class OneTimeSchedule implements Schedule {

  final long when;
  private boolean done = false;

  private OneTimeSchedule(boolean done, long when) {
    this.done = done;
    this.when = when;
  }

  OneTimeSchedule(long delay) {
    this.when = delay;
  }

  @Override
  public void reset() {
    done = false;
  }

  @Override
  public OptionalLong next() {
    if (done) {
      return OptionalLong.empty();
    } else {
      done = true;
      return OptionalLong.of(when);
    }
  }

  @Override
  public Schedule repeat() {
    return Schedule.fixedRate(when);
  }

  @Override
  public int hashCode() {
    return Objects.hash(done, when);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof OneTimeSchedule that)) {
      return false;
    }

    return this.done == that.done && this.when == that.when;
  }

  @Override
  public Schedule clone() {
    return new OneTimeSchedule(done, when);
  }

  @Override
  public String toString() {
    return "OneTimeSchedule(done=" + done + ",when=" + when + ")";
  }
}

class StepLimitedSchedule implements Schedule {

  final long stepLimit;
  final Schedule source;
  long stepsPassed = 0L;

  private StepLimitedSchedule(Schedule source, long stepLimit, long stepsPassed) {
    this.stepLimit = stepLimit;
    this.source = source;
    this.stepsPassed = stepsPassed;
  }

  StepLimitedSchedule(Schedule source, long stepLimit) {
    this.stepLimit = stepLimit;
    this.source = source;
  }

  @Override
  public void reset() {
    source.reset();
    stepsPassed = 0L;
  }

  @Override
  public OptionalLong next() {
    if (stepsPassed >= stepLimit) {
      return OptionalLong.empty();
    }
    stepsPassed += 1L;
    return source.next();
  }

  @Override
  public Schedule limitSteps(long totalSteps) {
    if (totalSteps >= stepLimit) {
      return this;
    } else {
      return new StepLimitedSchedule(source, totalSteps, stepsPassed);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(stepLimit, source, stepsPassed);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof StepLimitedSchedule that)) {
      return false;
    }

    return this.stepLimit == that.stepLimit
        && this.stepsPassed == that.stepsPassed
        && Objects.equals(this.source, that.source);
  }

  @Override
  public Schedule clone() {
    return new StepLimitedSchedule(source.clone(), stepLimit, stepsPassed);
  }

  @Override
  public String toString() {
    return "StepLimitedSchedule(source="
        + source
        + ",stepLimit="
        + stepLimit
        + ",stepsPassed="
        + stepsPassed
        + ")";
  }
}

class TimeLimitedSchedule implements Schedule {

  final long timeLimit;
  final Schedule source;
  long timePassed = 0L;

  private TimeLimitedSchedule(Schedule source, long timeLimit, long timePassed) {
    this.source = source;
    this.timeLimit = timeLimit;
    this.timePassed = timePassed;
  }

  TimeLimitedSchedule(Schedule source, long timeLimit) {
    this.source = source;
    this.timeLimit = timeLimit;
  }

  @Override
  public void reset() {
    source.reset();
    timePassed = 0L;
  }

  @Override
  public OptionalLong next() {
    OptionalLong next = source.next();
    if (next.isEmpty()) {
      return OptionalLong.empty();
    }

    long add = next.getAsLong();
    timePassed += add;
    if (timePassed > timeLimit) {
      return OptionalLong.empty();
    }

    return next;
  }

  @Override
  public Schedule limitTime(long totalTicks) {
    if (totalTicks >= timeLimit) {
      return this;
    } else {
      return new TimeLimitedSchedule(source, totalTicks, timePassed);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeLimit, timePassed, source);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof TimeLimitedSchedule that)) {
      return false;
    }

    return this.timeLimit == that.timeLimit
        && this.timePassed == that.timePassed
        && Objects.equals(this.source, that.source);
  }

  @Override
  public Schedule clone() {
    return new TimeLimitedSchedule(source.clone(), timeLimit, timePassed);
  }

  @Override
  public String toString() {
    return "TimeLimitedSchedule(source="
        + source
        + ",timeLimit="
        + timeLimit
        + ",timePassed="
        + timePassed
        + ")";
  }
}

record FixedRateSchedule(long period) implements Schedule {

  @Override
  public void reset() {}

  @Override
  public OptionalLong next() {
    return OptionalLong.of(period);
  }

  @Override
  public Schedule limitSteps(long totalSteps) {
    if (totalSteps == 1) {
      return Schedule.once(period);
    } else {
      return Schedule.super.limitSteps(totalSteps);
    }
  }

  @Override
  public Schedule append(Schedule andThen) {
    return this;
  }

  @Override
  public Schedule repeat() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof FixedRateSchedule that)) {
      return false;
    }

    return this.period == that.period;
  }

  @Override
  public Schedule clone() {
    return this;
  }

  @Override
  public String toString() {
    return "FixedRateSchedule(period=" + period + ")";
  }
}
