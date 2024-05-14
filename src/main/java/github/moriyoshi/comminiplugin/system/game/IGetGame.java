package github.moriyoshi.comminiplugin.system.game;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public interface IGetGame<T extends AbstractGame> {

  @SuppressWarnings("unchecked")
  @NotNull
  default T getGame() {
    return Objects.requireNonNull((T) GameSystem.getNowGame());
  }

}
