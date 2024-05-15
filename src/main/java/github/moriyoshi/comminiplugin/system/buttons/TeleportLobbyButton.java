package github.moriyoshi.comminiplugin.system.buttons;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class TeleportLobbyButton extends ItemButton<MenuHolder<ComMiniPlugin>> {

  private TeleportLobbyButton() {
    super(new ItemBuilder(Material.ENDER_PEARL).name("<blue>ロビーにテレポート").build());
  }

  public static TeleportLobbyButton of() {
    return new TeleportLobbyButton();
  }

  @Override
  public void onClick(@NotNull MenuHolder<ComMiniPlugin> holder,
      @NotNull InventoryClickEvent event) {
    event.getWhoClicked().teleport(ComMiniWorld.LOBBY);
  }
}
