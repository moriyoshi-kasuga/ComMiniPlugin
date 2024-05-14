package github.moriyoshi.comminiplugin.system.game;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import github.moriyoshi.comminiplugin.util.PrefixUtil;

public abstract class AbstractMiniGame extends AbstractGame {

  public AbstractMiniGame(String id, String name, String description, Material material, PrefixUtil prefix,
      AbstractGameListener<?> listener) {
    super(id, name, description, material, prefix, listener);
  }

  public abstract boolean isCanPlay(Player player);
}
