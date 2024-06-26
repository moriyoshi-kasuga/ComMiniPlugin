package github.moriyoshi.comminiplugin.system.minigame;

import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.HasGameKey;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public interface IGetMiniGame<T extends AbstractMiniGame> extends HasGameKey {

  @NotNull
  @SuppressWarnings("unchecked")
  default T getGame() {
    return Objects.requireNonNull((T) GameSystem.getGame(getKey()));
  }
}
