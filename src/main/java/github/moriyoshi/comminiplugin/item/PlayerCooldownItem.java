package github.moriyoshi.comminiplugin.item;

import github.moriyoshi.comminiplugin.util.IdentifierKey;
import github.moriyoshi.comminiplugin.util.Util;
import github.moriyoshi.comminiplugin.util.tuple.Triple;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.val;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface PlayerCooldownItem extends InterfaceItem {

  Map<IdentifierKey, Triple<Integer, Boolean, Optional<Consumer<UUID>>>> COOLDOWN = new HashMap<>();

  static void allCountDown() {
    for (Map.Entry<IdentifierKey, Triple<Integer, Boolean, Optional<Consumer<UUID>>>> entry : COOLDOWN.entrySet()) {
      val triple = entry.getValue();
      if (!triple.getSecond()) {
        continue;
      }
      var num = triple.getFirst();
      if (0 >= --num) {
        COOLDOWN.remove(entry.getKey());
        triple.getThird().ifPresent(consumer -> consumer.accept(entry.getKey().uuid()));
        return;
      }
      COOLDOWN.put(entry.getKey(), Triple.of(num, true, triple.getThird()));
    }
  }

  default int getCooldown(UUID uuid) {
    return Optional.ofNullable(COOLDOWN.get(getItemKey(uuid)))
        .map(Triple::getFirst)
        .orElseThrow(() -> new RuntimeException(getItemKey(uuid) + "のクールダウンはありません"));
  }

  default void setCooldown(final int cooldown, UUID uuid) {
    if (1 > cooldown) {
      throw new IllegalArgumentException("クールダウンは 1 以上を指定してください");
    }
    COOLDOWN.put(
        getItemKey(uuid), Triple.of(cooldown, shouldAutoReduceCountDown(), endCountDown()));
  }

  default boolean inCooldown(UUID uuid) {
    return COOLDOWN.containsKey(getItemKey(uuid));
  }

  @Override
  default void heldItem(Player player) {
    val uuid = player.getUniqueId();
    player.sendActionBar(
        Util.mm(inCooldown(uuid) ? getHasCooldownMessage(getCooldown(uuid)) : getReadyMessage()));
  }

  @NotNull
  default String getHasCooldownMessage(int cooldown) {
    return "<red>Now on %.1f cooldown".formatted((double) cooldown / 20.0);
  }

  @NotNull
  default String getReadyMessage() {
    return "<green>READY";
  }

  default Optional<Consumer<UUID>> endCountDown() {
    return Optional.empty();
  }

  default boolean shouldAutoReduceCountDown() {
    return true;
  }
}
