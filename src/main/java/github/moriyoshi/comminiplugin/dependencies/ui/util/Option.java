package github.moriyoshi.comminiplugin.dependencies.ui.util;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * {@link java.util.Optional} と同様ですが、NULL値を許容します。
 */
public interface Option<T> {

  /**
   * 値を持つOptionを構築する。nullも可。
   *
   * @param value the value
   * @param <T>   the type of the value
   * @return a new Option
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  static <T> Option<T> some(T value) {
    return new Some(value);
  }

  /**
   * 値のないOptionを取得する。
   *
   * @param <T> unused
   * @return an empty Option
   */
  @SuppressWarnings("unchecked")
  static <T> Option<T> none() {
    return None.INSTANCE;
  }

  /**
   * このOptionが値を含むかどうかをテストする。
   *
   * @return このOptionが値を含んでいればtrue、そうでなければfalse。
   */
  boolean isPresent();

  /**
   * このOptionから値を取得します。
   *
   * @return the value
   * @throws NoSuchElementException このオプションに値がない場合
   */
  T get() throws NoSuchElementException;

}

class Some<T> implements Option<T> {

  private final T value;

  Some(T value) {
    this.value = value;
  }

  @Override
  public boolean isPresent() {
    return true;
  }

  @Override
  public T get() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Some that)) {
      return false;
    }

    return Objects.equals(this.value, that.value);
  }

  @Override
  public String toString() {
    return "Some(" + value + ")";
  }

}

@SuppressWarnings("rawtypes")
class None implements Option {

  static final None INSTANCE = new None();

  private None() {
  }

  @Override
  public boolean isPresent() {
    return false;
  }

  @Override
  public Object get() throws NoSuchElementException {
    throw new NoSuchElementException("None");
  }

  @Override
  public String toString() {
    return "None";
  }

}
