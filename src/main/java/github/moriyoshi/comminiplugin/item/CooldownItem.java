package github.moriyoshi.comminiplugin.item;

import github.moriyoshi.comminiplugin.util.Util;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface CooldownItem extends InterfaceItem {

  Map<CustomItemKey, Integer> COOLDOWN = new HashMap<>();

  default int getCooldown() {
    if (!inCooldown()) {
      throw new RuntimeException(getItemKey() + "のクールダウンはありません");
    }
    return COOLDOWN.get(getItemKey());
  }

  default void setCooldown(final int cooldown) {
    if (1 > cooldown) {
      throw new IllegalArgumentException("クールダウンは 1 以上を指定してください");
    }
    COOLDOWN.put(getItemKey(), cooldown);
  }

  default boolean inCooldown() {
    return COOLDOWN.containsKey(getItemKey());
  }

  /**
   * 数字を1減らします
   *
   * @return true の場合はまだクールダウンがあります
   */
  default boolean countDown() {
    final int cooldown = getCooldown();
    if (1 >= cooldown) {
      COOLDOWN.remove(getItemKey());
      return false;
    }
    COOLDOWN.put(getItemKey(), cooldown - 1);
    return true;
  }

  @Override
  default Optional<BiConsumer<Player, ItemStack>> heldItem() {
    return Optional.of(
        (player, item) -> {
          player.sendActionBar(
              Util.mm(inCooldown() ? getHasCooldownMessage(getCooldown()) : getReadyMessage()));
        });
  }

  @NotNull
  default String getHasCooldownMessage(int cooldown) {
    return "<red>Now on " + cooldown + " cooldown";
  }

  @NotNull
  default String getReadyMessage() {
    return "<green>READY";
  }
}
