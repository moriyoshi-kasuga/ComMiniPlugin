package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CurryBreadItem extends CustomItem implements CustomItem.InteractMainHand {
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
  public void interactMainHand(PlayerInteractEvent e, final Player player) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    useItemAmount();
    player.heal(6);
    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
  }

  @Override
  public boolean canStack() {
    return true;
  }
}
