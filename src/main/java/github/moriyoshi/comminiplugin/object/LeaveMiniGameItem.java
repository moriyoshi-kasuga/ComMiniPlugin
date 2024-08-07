package github.moriyoshi.comminiplugin.object;

import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.GameSystem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LeaveMiniGameItem extends CustomItem implements CustomItem.InteractMainHand {

  public LeaveMiniGameItem() {
    this(
        new ItemBuilder(Material.ENDER_EYE)
            .name("<red>Leave MiniGame")
            .glow()
            .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_INV, true)
            .customItemFlag(CustomItemFlag.DISABLE_ITEM_SPAWN, true)
            .build());
  }

  public LeaveMiniGameItem(@NotNull final ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(final PlayerInteractEvent e, final Player player) {
    e.setCancelled(true);
    if (e.getAction().isLeftClick()) {
      return;
    }
    GameSystem.initializePlayer(player);
  }

  @Override
  public boolean canShowing() {
    return false;
  }
}
