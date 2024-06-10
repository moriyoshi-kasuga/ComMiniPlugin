package github.moriyoshi.comminiplugin.object;

import github.moriyoshi.comminiplugin.command.MenuCommand;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MenuItem extends CustomItem {

  public MenuItem() {
    this(
        new ItemBuilder(Material.BOOK)
            .name("<red>Menu")
            .glow()
            .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
            .customItemFlag(CustomItemFlag.DISABLE_ITEM_SPAWN, true)
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_OTHER_INV, true)
            .build());
  }

  public MenuItem(@NotNull final ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(final PlayerInteractEvent e) {
    e.setCancelled(true);
    if (e.getAction().isLeftClick()) {
      return;
    }
    MenuCommand.open(e.getPlayer());
  }
}
