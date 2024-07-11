package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.item.CooldownItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.lib.JumpState;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class UpgradeWingItem extends CustomItem implements CooldownItem ,CustomItem.InteractMainHand{
  public UpgradeWingItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<yellow>これを使えばどこまでも")
            .lore("<gray>使うと視線の先に飛んでそこからエリトラで滑空できます", "<gray>地面につくとエリトラは消えます")
            .customModelData(8)
            .build());
  }

  public UpgradeWingItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e,final Player player) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    if (inCooldown()) {
      return;
    }
    setCooldown(60 * 20);
    val loc = player.getLocation();
    loc.setPitch(-30);
    BukkitUtil.setVelocity(player, loc.getDirection().multiply(2).setY(2), JumpState.FREE);
    WingItem.setWing(player);
  }
}
