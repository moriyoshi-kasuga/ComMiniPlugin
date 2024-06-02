package github.moriyoshi.comminiplugin.item;

import github.moriyoshi.comminiplugin.util.Util;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PlayerCooldownItem extends InterfaceItem {

  Map<CustomItemKey, Integer> COOLDOWN = new HashMap<>();

  default boolean contains(UUID uuid) {
    return COOLDOWN.containsKey(getItemKey(uuid));
  }

  default int getCooldown(UUID uuid) {
    return COOLDOWN.getOrDefault(getItemKey(uuid), -1);
  }

  default void setCooldown(final int cooldown, UUID uuid) {
    if (1 > cooldown) {
      throw new IllegalArgumentException("クールダウンは 1 以上を指定してください");
    }
    COOLDOWN.put(getItemKey(uuid), cooldown);
  }

  default boolean inCooldown(UUID uuid) {
    return getCooldown(uuid) != -1;
  }

  @Override
  default Optional<BiConsumer<Player, ItemStack>> heldItem() {
    return Optional.of(
        (player, item) -> {
          val uuid = player.getUniqueId();
          player.sendActionBar(
              Util.mm(
                  inCooldown(uuid) ? getHasCooldownMessage(getCooldown(uuid)) : getReadyMessage()));
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
