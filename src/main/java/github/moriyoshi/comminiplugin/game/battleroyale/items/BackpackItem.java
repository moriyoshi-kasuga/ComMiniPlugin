package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BackpackItem extends CustomItem {
  public BackpackItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<#8B4513>バックパック")
            .lore("<gray>使用するとインベントリーを一つ拡張します。")
            .customModelData(15)
            .build());
  }

  public BackpackItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    useItemAmount();
    val inv = e.getPlayer().getInventory();
    for (int row = 3; row >= 0; row--) {
      for (int column = 0; column < 9; column++) {
        val slot = row * 9 + column;
        val item = inv.getItem(slot);
        if (item != null
            && !item.isEmpty()
            && ItemBuilder.getCustomItemFlag(item, CustomItemFlag.DISABLE_MOVE_INV).orElse(false)) {
          inv.setItem(slot, null);
          break;
        }
      }
    }
  }

  @Override
  public boolean canStack() {
    return true;
  }
}
