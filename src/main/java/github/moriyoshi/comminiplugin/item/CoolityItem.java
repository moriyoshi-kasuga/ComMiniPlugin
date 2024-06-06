package github.moriyoshi.comminiplugin.item;

import github.moriyoshi.comminiplugin.util.Util;
import org.bukkit.entity.Player;

public interface CoolityItem extends CooldownItem, DurabilityItem {

  @Override
  default void heldItem(Player player) {
    player.sendActionBar(
        Util.mm(
            hasDurability()
                ? (inCooldown() ? getHasCooldownMessage(getCooldown()) : getCooldownReadyMessage())
                    + " "
                    + getHasDurabilityMessage(getDurability())
                : getNoDurabilityMessage()));
  }
}
