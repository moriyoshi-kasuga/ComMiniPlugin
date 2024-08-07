package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BackpackItem extends CustomItem implements CustomItem.InteractMainHand {
  public BackpackItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<#8B4513>バックパック")
            .lore("<gray>使用するとインベントリーを一列拡張します。")
            .customModelData(15)
            .build());
  }

  public BackpackItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e, final Player player) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    useItemAmount();
    val inv = e.getPlayer().getInventory();
    for (int row = 2; row >= 0; row--) {
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
