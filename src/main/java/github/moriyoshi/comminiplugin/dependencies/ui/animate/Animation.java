package github.moriyoshi.comminiplugin.dependencies.ui.animate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * アニメーションは、{@link Frame}のコンテナです。 アニメーションは、{@link AnimationRunner}を使って実行できます。
 */
@SuppressWarnings("all")
public interface Animation {

  /**
   * フレームの配列からアニメーションを作成する。
   *
   * @param frames フレームを指定します。
   * @return 新しいアニメーションを返す
   */
  static Animation ofFrames(Frame<?, ?>... frames) {
    return new SimpleAnimation(List.of(frames));
  }

  /**
   * フレーム一覧からアニメーションを作成する。
   *
   * @param frames フレーム
   * @return 新アニメーション
   */
  static Animation ofFrames(List<? extends Frame<?, ?>> frames) {
    return new SimpleAnimation(frames);
  }

  /**
   * 無限のフレーム数をゆったりと生成するアニメーションを作成する。
   *
   * @param seed      第1フレーム
   * @param nextFrame 次のフレームを計算する方法を知っている関数です。
   * @param <F>       フレームの種類
   * @return 新アニメーション
   */
  static <F extends Frame<?, ?>> Animation infinite(F seed, UnaryOperator<F> nextFrame) {
    return new InfiniteAnimation<>(seed, nextFrame);
  }

  /**
   * アニメーションを初期状態に戻す。
   */
  void reset();

  /**
   * アニメーションの次のフレームを取得します
   *
   * @return the next frame
   */
  Frame<?, ?> nextFrame();

  /**
   * このアニメーションに別のフレームがあるかどうかをテストします。
   * このアニメーションに少なくとも1つのフレームがあればtrueを、そうでなければfalseを返す。
   */
  boolean hasNextFrame();

  /**
   * このアニメーションをオートリセットアニメーションにして、このアニメーションが終了したときに自動的にやり直すようにします。
   *
   * @return このアニメーションをループさせるアニメーション
   */
  default Animation continuously() {
    return new ContinuousAnimation(this);
  }

  /**
   * このアニメーションに別のアニメーションを追加する。
   *
   * @param next 附属アニメーション
   * @return 現在のアニメーションから次のアニメーションに進むアニメーションです。
   */
  default Animation andThen(Animation next) {
    return new ConcatAnimation(this, next);
  }

  /**
   * アニメーションを固定フレーム数で制限する。
   *
   * @param numberOfFrames 最大フレーム数
   * @return このアニメーションと同じで、{@code numberOfFrames}フレーム数だけ制限されるアニメーション。
   */
  default Animation limit(int numberOfFrames) {
    return new LimitAnimation(numberOfFrames, this);
  }

}

class LimitAnimation implements Animation {

  private final int limit;
  private final Animation wrapped;
  private int count;

  private LimitAnimation(int limit, Animation wrapped, int count) {
    this.limit = limit;
    this.wrapped = wrapped;
    this.count = count;
  }

  LimitAnimation(int limit, Animation wrapped) {
    if (limit < 0) {
      throw new IllegalArgumentException("negative limit: " + limit);
    }
    Objects.requireNonNull(wrapped, "wrapped cannot be null");

    this.limit = limit;
    this.wrapped = wrapped;
  }

  @Override
  public void reset() {
    count = 0;
    wrapped.reset();
  }

  @Override
  public Frame<?, ?> nextFrame() {
    count += 1;
    return wrapped.nextFrame();
  }

  @Override
  public boolean hasNextFrame() {
    return count < limit && wrapped.hasNextFrame();
  }

  @Override
  public Animation limit(int numberOfFrames) {
    if (numberOfFrames < limit) {
      return new LimitAnimation(Math.min(numberOfFrames, limit), wrapped, count);
    } else {
      return this;
    }
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
    if (!(obj instanceof LimitAnimation that)) {
      return false;
    }

    return this.limit == that.limit
        && Objects.equals(this.wrapped, that.wrapped)
        && this.count == that.count;
  }

  @Override
  public String toString() {
    return "LimitAnimation(limit=" + limit + ",wrapped=" + wrapped + ",count=" + count + ")";
  }

}

class ConcatAnimation implements Animation {

  private final Animation one, two;

  ConcatAnimation(Animation one, Animation two) {
    this.one = Objects.requireNonNull(one, "one cannot be null");
    this.two = Objects.requireNonNull(two, "two cannot be null");
  }

  @Override
  public void reset() {
    one.reset();
    two.reset();
  }

  @Override
  public Frame<?, ?> nextFrame() {
    if (one.hasNextFrame()) {
      return one.nextFrame();
    }
    return two.nextFrame();
  }

  @Override
  public boolean hasNextFrame() {
    return one.hasNextFrame() || two.hasNextFrame();
  }

  @Override
  public int hashCode() {
    return Objects.hash(one, two);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ConcatAnimation that)) {
      return false;
    }

    return Objects.equals(this.one, that.one)
        && Objects.equals(this.two, that.two);
  }

  @Override
  public String toString() {
    return "ConcatAnimation(one=" + one + ",two=" + two + ")";
  }

}

class ContinuousAnimation implements Animation {

  private final Animation wrapped;

  ContinuousAnimation(Animation wrapped) {
    this.wrapped = Objects.requireNonNull(wrapped, "wrapped cannot be null");
  }

  @Override
  public void reset() {
    wrapped.reset();
  }

  @Override
  public Frame<?, ?> nextFrame() {
    if (!wrapped.hasNextFrame()) {
      wrapped.reset();
    }
    return wrapped.nextFrame();
  }

  @Override
  public boolean hasNextFrame() {
    return true;
  }

  @Override
  public Animation continuously() {
    return this;
  }

  @Override
  public Animation andThen(Animation next) {
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(wrapped);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ContinuousAnimation that)) {
      return false;
    }

    return Objects.equals(this.wrapped, that.wrapped);
  }

  @Override
  public String toString() {
    return "ContinuousAnimation(wrapped=" + wrapped + ")";
  }

}

@SuppressWarnings("all")
class InfiniteAnimation<F extends Frame<?, ?>> implements Animation {

  private final F startingFrame;
  private final UnaryOperator<F> nextFrame;

  private F state;

  InfiniteAnimation(F startingFrame, UnaryOperator<F> nextFrame) {
    this.startingFrame = Objects.requireNonNull(startingFrame, "startingFrame cannot be null");
    this.nextFrame = Objects.requireNonNull(nextFrame, "nextFrame cannot be null");

    state = startingFrame;
  }

  @Override
  public void reset() {
    state = startingFrame;
  }

  @Override
  public F nextFrame() {
    F value = state;
    state = nextFrame.apply(state);
    return value;
  }

  @Override
  public boolean hasNextFrame() {
    return true;
  }

  @Override
  public Animation continuously() {
    return this;
  }

  @Override
  public Animation andThen(Animation next) {
    return this;
  }

  @Override
  public int hashCode() {
    return Objects.hash(startingFrame, nextFrame, state);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof InfiniteAnimation that)) {
      return false;
    }

    return Objects.equals(this.startingFrame, that.startingFrame)
        && Objects.equals(this.nextFrame, that.nextFrame)
        && Objects.equals(this.state, that.state);
  }

  @Override
  public String toString() {
    return "InfiniteAnimation(startingFrame=" + startingFrame + ",nextFrame=" + nextFrame
        + ",state=" + state + ")";
  }

}

class SimpleAnimation implements Animation {

  private final List<? extends Frame<?, ?>> frames;
  private int currentIndex;

  private SimpleAnimation(int index, List<? extends Frame<?, ?>> frames) {
    this.currentIndex = index;
    this.frames = frames;
  }

  SimpleAnimation(List<? extends Frame<?, ?>> frames) {
    Objects.requireNonNull(frames, "frames cannot be null");
    if (frames.isEmpty()) {
      throw new IllegalArgumentException("frames cannot be empty");
    }

    this.frames = new ArrayList<>(frames);
  }

  @Override
  public void reset() {
    this.currentIndex = 0;
  }

  @Override
  public Frame<?, ?> nextFrame() {
    return frames.get(currentIndex++);
  }

  @Override
  public boolean hasNextFrame() {
    return currentIndex < frames.size();
  }

  @Override
  public Animation andThen(Animation next) {
    if (next instanceof SimpleAnimation that) {

      ArrayList<Frame<?, ?>> newFrames = new ArrayList<>(this.frames.size() + that.frames.size());
      newFrames.addAll(this.frames);
      newFrames.addAll(that.frames);
      return new SimpleAnimation(currentIndex, newFrames);
    } else {
      return Animation.super.andThen(next);
    }
  }

  @Override
  public Animation limit(int numberOfFrames) {
    if (numberOfFrames < frames.size()) {
      return new SimpleAnimation(currentIndex,
          frames.subList(0, Math.min(frames.size(), numberOfFrames)));
    } else {
      return this;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(currentIndex, frames);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof SimpleAnimation that)) {
      return false;
    }

    return this.currentIndex == that.currentIndex
        && Objects.equals(this.frames, that.frames);
  }

  @Override
  public String toString() {
    return "SimpleAnimation(currentIndex=" + currentIndex + ",frames=" + frames + ")";
  }

}
