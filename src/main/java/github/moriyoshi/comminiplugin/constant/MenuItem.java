package github.moriyoshi.comminiplugin.constant;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.buttons.AddSpecButton;
import github.moriyoshi.comminiplugin.system.buttons.GameMenuButton;
import github.moriyoshi.comminiplugin.system.buttons.TeleportLobbyButton;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

/**
 * MenuItem
 */
public class MenuItem extends CustomItem {

  public MenuItem() {
    this(new ItemBuilder(Material.BOOK).name("<red>Menu").glow().build());
  }

  public MenuItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    if (open(e.getPlayer())) {
      return;
    }
    ComMiniPrefix.MAIN.send(e.getPlayer(), "<red>あなたはmenuを開けません");
  }

  @Override
  public @NotNull String getIdentifier() {
    return "menu";
  }

  public static boolean open(Player p) {
    if (GameSystem.inGame() && GameSystem.isStarted() && GameSystem.nowGame().isGamePlayer(p)) {
      return false;
    }
    new InnerMenu().openInv(p);
    return true;
  }

  private static class InnerMenu extends MenuHolder<ComMiniPlugin> {
    public InnerMenu() {
      super(ComMiniPlugin.getPlugin(), 27, "<yellow>Menu");
      setButton(11, TeleportLobbyButton.of());
      setButton(13, GameMenuButton.of());
      setButton(14, AddSpecButton.of());
    }
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
