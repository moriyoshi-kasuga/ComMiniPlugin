package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.JumpState;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class UpgradeWingItem extends CustomItem implements CooldownItem {
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
  public void interactMainHand(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    if (inCooldown()) {
      return;
    }
    setCooldown(60 * 20);
    val player = e.getPlayer();
    val loc = player.getLocation();
    loc.setPitch(-30);
    BukkitUtil.setVelocity(player, loc.getDirection().multiply(2).setY(2), JumpState.FREE);
    WingItem.setWing(player);
  }
}
