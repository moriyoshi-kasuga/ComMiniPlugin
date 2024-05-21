package github.moriyoshi.comminiplugin.system;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import github.moriyoshi.comminiplugin.util.PrefixUtil;

public abstract class AbstractMiniGame extends AbstractGame {

  public AbstractMiniGame(final String id, final String name, final String description, final Material material,
      final PrefixUtil prefix,
      final AbstractGameListener<?> listener) {
    super(id, name, description, material, prefix, listener);
  }

  public abstract boolean isCanPlay(Player player);
}
