package github.moriyoshi.comminiplugin.system.minigame;

import github.moriyoshi.comminiplugin.util.HasKey;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public interface IGetMiniGame<T extends AbstractMiniGame> extends HasKey {

  @NotNull
  @SuppressWarnings("unchecked")
  default T getMiniGame() {
    return Objects.requireNonNull((T) MiniGameSystem.getMiniGame(getKey()));
  }
}
