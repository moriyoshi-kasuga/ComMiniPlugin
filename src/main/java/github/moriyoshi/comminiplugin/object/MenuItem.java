package github.moriyoshi.comminiplugin.object;

import github.moriyoshi.comminiplugin.command.MenuCommand;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
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
            .build());
  }

  public MenuItem(@NotNull final ItemStack item) {
    super(item);
  }

  @Override
  public void interact(final PlayerInteractEvent e) {
    e.setCancelled(true);
    MenuCommand.open(e.getPlayer());
  }

  @Override
  public @NotNull String getIdentifier() {
    return "menu";
  }

  @Override
  public boolean canMoveOtherInv(final InventoryClickEvent e) {
    return false;
  }
}
