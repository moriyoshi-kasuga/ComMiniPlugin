package github.moriyoshi.comminiplugin.dependencies.ui.util;

/**
 * {@link java.util.function.BiConsumer} の特殊化で、最初に受け入れられる値が int である。
 *
 * @param <T> 2番目に受け付けた値の型
 */
@FunctionalInterface
public interface IntBiConsumer<T> {

  /**
   * intと別の値を消費する。
   *
   * @param integer the int
   * @param value   the other value
   */
  void accept(int integer, T value);

}
