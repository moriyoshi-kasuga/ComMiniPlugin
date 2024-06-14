package github.moriyoshi.comminiplugin.lib.item;

import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import org.bukkit.entity.Player;

public interface CoolityItem extends CooldownItem, DurabilityItem {

  @Override
  default void heldItem(Player player) {
    player.sendActionBar(
        BukkitUtil.mm(
            hasDurability()
                ? (inCooldown() ? getHasCooldownMessage(getCooldown()) : getCooldownReadyMessage())
                    + " "
                    + getHasDurabilityMessage(getDurability())
                : getNoDurabilityMessage()));
  }
}
