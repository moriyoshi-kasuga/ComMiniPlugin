package github.moriyoshi.comminiplugin.item;

import java.util.HashMap;
import java.util.Map;

/**
 * CooldownItem
 */
public interface CooldownItem extends InterfaceItem {

  Map<CustomItemKey, Integer> COOLDOWN = new HashMap<>();

  default boolean contains() {
    return COOLDOWN.containsKey(getItemKey());
  }

  default int getCooldown() {
    return COOLDOWN.getOrDefault(getItemKey(), -1);
  }

  default boolean inCooldown() {
    return getCooldown() != -1;
  }

  default void setCooldown(int cooldown) {
    if (1 > cooldown) {
      throw new IllegalArgumentException("クールダウンは 1 以上を指定してください");
    }
    COOLDOWN.put(getItemKey(), cooldown);
  }

  default boolean countDown() {
    if (!contains()) {
      return false;
    }
    int cooldown;
    if (1 >= (cooldown = getCooldown())) {
      COOLDOWN.remove(getItemKey());
      return false;
    }
    COOLDOWN.put(getItemKey(), cooldown - 1);
    return true;
  }

}
