package github.moriyoshi.comminiplugin.item;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public interface CooldownItem extends InterfaceItem {

  Map<CustomItemKey, Integer> COOLDOWN = new HashMap<>();

  default boolean contains() {
    return COOLDOWN.containsKey(getItemKey());
  }

  default boolean contains(Player player) {
    return COOLDOWN.containsKey(getItemKey(player));
  }

  default int getCooldown() {
    return COOLDOWN.getOrDefault(getItemKey(), -1);
  }

  default int getCooldown(Player player) {
    return COOLDOWN.getOrDefault(getItemKey(player), -1);
  }

  default void setCooldown(final int cooldown) {
    if (1 > cooldown) {
      throw new IllegalArgumentException("クールダウンは 1 以上を指定してください");
    }
    COOLDOWN.put(getItemKey(), cooldown);
  }

  default void setCooldown(final int cooldown, Player player) {
    if (1 > cooldown) {
      throw new IllegalArgumentException("クールダウンは 1 以上を指定してください");
    }
    COOLDOWN.put(getItemKey(player), cooldown);
  }

  default boolean inCooldown() {
    return getCooldown() != -1;
  }

  default boolean inCooldown(Player player) {
    return getCooldown(player) != -1;
  }

  default boolean countDown() {
    if (!contains()) {
      return false;
    }
    final int cooldown;
    if (1 >= (cooldown = getCooldown())) {
      COOLDOWN.remove(getItemKey());
      return false;
    }
    COOLDOWN.put(getItemKey(), cooldown - 1);
    return true;
  }

  default boolean countDown(Player player) {
    if (!contains(player)) {
      return false;
    }
    final int cooldown = getCooldown(player);
    if (1 >= cooldown) {
      COOLDOWN.remove(getItemKey(player));
      return false;
    }
    COOLDOWN.put(getItemKey(player), cooldown - 1);
    return true;
  }
}
