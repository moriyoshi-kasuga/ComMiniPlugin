package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CurryBreadItem extends CustomItem {
  public CurryBreadItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<#8B4513>カレーパン")
            .lore("<gray>体力を3ハート回復")
            .customModelData(17)
            .build());
  }

  public CurryBreadItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    itemUse();
    val player = e.getPlayer();
    player.heal(3);
    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
  }

  @Override
  public boolean canStack() {
    return true;
  }
}
