package github.moriyoshi.comminiplugin.game.battleroyale;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.buttons.GameStartButton;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class BRAdminMenu extends MenuHolder<ComMiniPlugin> {
  public BRAdminMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>OP:バトルロワイヤル");
    setButton(16, new GameStartButton());
    setButton(13, new ItemButton<>(new ItemBuilder(Material.DIRT).name("<green>山フィールド").build()) {

      @Override
      public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
        super.onClick(holder, event);
      }

    });
  }
}
