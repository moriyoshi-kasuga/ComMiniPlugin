package github.moriyoshi.comminiplugin.system.buttons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class TeleportLobbyButton extends ItemButton<MenuHolder<ComMiniPlugin>> {
  private TeleportLobbyButton() {
    super(new ItemBuilder(Material.ENDER_PEARL).name("<blue>ロビーにテレポート").build());
  }

  public static TeleportLobbyButton of() {
    return new TeleportLobbyButton();
  }

  @Override
  public void onClick(MenuHolder<ComMiniPlugin> holder, @NotNull InventoryClickEvent event) {
    var p = (Player) event.getWhoClicked();
    if (GameSystem.inGame() && GameSystem.nowGame().isGamePlayer(p)) {
      ComMiniPrefix.MAIN.send(p, "<red>あなたはロビーにテレポートできません");
      return;
    }
    p.teleport(ComMiniWorld.LOBBY);
  }
}