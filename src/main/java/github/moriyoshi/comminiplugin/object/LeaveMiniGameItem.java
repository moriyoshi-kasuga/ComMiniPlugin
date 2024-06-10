package github.moriyoshi.comminiplugin.object;

import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LeaveMiniGameItem extends CustomItem {

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
  public void interactMainHand(final PlayerInteractEvent e) {
    e.setCancelled(true);
    if (e.getAction().isLeftClick()) {
      return;
    }
    BukkitUtil.initializePlayer(e.getPlayer());
  }

  @Override
  public boolean canShowing() {
    return false;
  }
}
