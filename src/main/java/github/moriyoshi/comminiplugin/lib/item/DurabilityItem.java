package github.moriyoshi.comminiplugin.lib.item;

import github.moriyoshi.comminiplugin.lib.IdentifierKey;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface DurabilityItem extends InterfaceItem {

  Map<IdentifierKey, Integer> DURABILITY = new HashMap<>();

  default int getDurability() {
    return Optional.ofNullable(DURABILITY.get(getItemKey()))
        .orElseThrow(() -> new RuntimeException(getItemKey() + "の耐久はありません"));
  }

  default void setDurability(final int durability) {
    if (1 > durability) {
      throw new IllegalArgumentException("耐久は 1 以上を指定してください");
    }
    DURABILITY.put(getItemKey(), durability);
  }

  default boolean hasDurability() {
    return DURABILITY.containsKey(getItemKey());
  }

  /**
   * 数字を1減らします
   *
   * @return true の場合はまだ耐久があります
   */
  default boolean useItemDurability() {
    int rest = getDurability();
    if (--rest > 0) {
      DURABILITY.put(getItemKey(), rest);
      return true;
    }
    DURABILITY.remove(getItemKey());
    endDurability();
    return false;
  }

  default void endDurability() {}

  @Override
  default void heldItem(Player player) {
    player.sendActionBar(
        BukkitUtil.mm(
            hasDurability() ? getHasDurabilityMessage(getDurability()) : getNoDurabilityMessage()));
  }

  /**
   * default action bar message
   *
   * @param durability rest durability
   * @return message
   */
  @NotNull
  default String getHasDurabilityMessage(int durability) {
    return "<yellow>durability: <gold>" + durability;
  }

  @NotNull
  default String getNoDurabilityMessage() {
    return "<red>no durability";
  }
}
