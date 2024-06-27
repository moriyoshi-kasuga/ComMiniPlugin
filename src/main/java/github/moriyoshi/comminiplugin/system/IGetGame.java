package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.lib.HasKey;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public interface IGetGame<T extends IGame> extends HasKey {

  @NotNull
  @SuppressWarnings("unchecked")
  default T getGame() {
    return Objects.requireNonNull((T) GameSystem.getGame(getKey()));
  }
}
