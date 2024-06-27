package github.moriyoshi.comminiplugin.lib;

import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/** Rust の Result 型のように */
public sealed interface Result<T, E> {

  default boolean isOk() {
    return this instanceof Ok;
  }

  default boolean isErr() {
    return this instanceof Err;
  }

  default Optional<T> ok() {
    return switch (this) {
      case Ok(var value) -> Optional.of(value);
      default -> Optional.empty();
    };
  }

  default Optional<E> err() {
    return switch (this) {
      case Err(var value) -> Optional.of(value);
      default -> Optional.empty();
    };
  }

  /**
   * {@link Ok} の場合は値を返します
   *
   * @return value of ok
   * @throws UnwrapException if err
   */
  default T unwrap() {
    return switch (this) {
      case Ok(var value) -> value;
      case Err(var value) ->
          throw new UnwrapException("called `Result.unwrap()` on an `Err` value : " + value);
    };
  }

  /**
   * {@link Err} の場合は 引数の {@code defaultValue} を返します
   *
   * @param defaultValue default
   * @return value
   */
  @NotNull
  default T unwrapOr(T defaultValue) {
    return ok().orElse(defaultValue);
  }

  /**
   * {@link Err} の場合は 引数の {@code defaultValue} の {@link Supplier} で取り出し返します
   *
   * @param defaultValue supplier
   * @return value
   */
  default T unwrapOrElse(Supplier<T> defaultValue) {
    return ok().orElseGet(defaultValue);
  }

  /**
   * {@link Err} の場合は値を返します
   *
   * @return value of err
   * @throws UnwrapException if ok
   */
  default E unwrapErr() {
    return switch (this) {
      case Ok(var value) ->
          throw new UnwrapException("called `Result.unwrapErr()` on an `Ok` value : " + value);
      case Err(var value) -> value;
    };
  }

  default void throwOk() {
    if (this instanceof Err(var value)) {
      throw new UnwrapException("called `Result.unwrapOrThrow()` on an `Err` value : " + value);
    }
  }

  record Ok<T, E>(T value) implements Result<T, E> {
    public static <T, E> Ok<T, E> of(T value) {
      return new Ok<>(value);
    }
  }

  record Err<T, E>(E value) implements Result<T, E> {
    public static <T, E> Err<T, E> of(E value) {
      return new Err<>(value);
    }
  }

  final class UnwrapException extends RuntimeException {
    public UnwrapException(String message) {
      super(message);
    }
  }
}
