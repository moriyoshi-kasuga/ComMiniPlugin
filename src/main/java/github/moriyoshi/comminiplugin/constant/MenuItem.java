package github.moriyoshi.comminiplugin.constant;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.command.MenuCommand;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class MenuItem extends CustomItem {

  public MenuItem() {
    this(new ItemBuilder(Material.BOOK).name("<red>Menu").glow().build());
  }

  public MenuItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    MenuCommand.open(e.getPlayer());
  }

  @Override
  public @NotNull String getIdentifier() {
    return "menu";
  }

  @Override
  public void dropItem(PlayerDropItemEvent e) {
    e.setCancelled(true);
  }

  @Override
  public boolean canMoveOtherInv(InventoryClickEvent e) {
    return false;
  }

}
