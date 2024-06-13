package github.moriyoshi.comminiplugin.item;

import github.moriyoshi.comminiplugin.util.IdentifierKey;
import github.moriyoshi.comminiplugin.util.Util;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface CooldownItem extends InterfaceItem {

  Map<IdentifierKey, Integer> COOLDOWN = new HashMap<>();

  default int getCooldown() {
    return Optional.ofNullable(COOLDOWN.get(getItemKey()))
        .orElseThrow(() -> new RuntimeException(getItemKey() + "のクールダウンはありません"));
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

  default void removeCooldown() {
    COOLDOWN.remove(getItemKey());
    endCountDown();
  }

  /**
   * 数字を1減らします
   *
   * @return true の場合はまだクールダウンがあります
   */
  default boolean countDown() {
    int cooldown = getCooldown();
    if (--cooldown > 0) {
      COOLDOWN.put(getItemKey(), cooldown);
      return true;
    }
    COOLDOWN.remove(getItemKey());
    endCountDown();
    return false;
  }

  default void endCountDown() {}

  @Override
  default void heldItem(Player player) {
    player.sendActionBar(
        Util.mm(inCooldown() ? getHasCooldownMessage(getCooldown()) : getCooldownReadyMessage()));
  }

  /**
   * default action bar message (cooldown / 20)
   *
   * @param cooldown rest time
   * @return message
   */
  @NotNull
  default String getHasCooldownMessage(int cooldown) {
    return "<red>cooldown: %.1f".formatted((double) cooldown / 20.0);
  }

  @NotNull
  default String getCooldownReadyMessage() {
    return "<green>READY";
  }

  default boolean shouldAutoReduceCountDown() {
    return true;
  }
}
