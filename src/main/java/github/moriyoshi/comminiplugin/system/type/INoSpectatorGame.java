package github.moriyoshi.comminiplugin.system.type;

import github.moriyoshi.comminiplugin.system.IGame;
import org.bukkit.entity.Player;

public interface INoSpectatorGame extends IGame {
  @Override
  default void innerAddSpec(Player player) {}

  @Override
  default boolean predicateSpec(Player player) {
    return false;
  }
}
