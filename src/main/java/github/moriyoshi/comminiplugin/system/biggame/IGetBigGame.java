package github.moriyoshi.comminiplugin.system.biggame;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public interface IGetBigGame<T extends AbstractBigGame> {

  @NotNull
  @SuppressWarnings("unchecked")
  default T getGame() {
    return Objects.requireNonNull((T) BigGameSystem.getGame());
  }
}
