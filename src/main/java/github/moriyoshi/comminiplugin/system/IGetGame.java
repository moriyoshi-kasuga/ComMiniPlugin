package github.moriyoshi.comminiplugin.system;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public interface IGetGame<T extends AbstractGame> {

  @NotNull
  @SuppressWarnings("unchecked")
  default T getGame() {
    return Objects.requireNonNull((T) GameSystem.getGame());
  }

}
