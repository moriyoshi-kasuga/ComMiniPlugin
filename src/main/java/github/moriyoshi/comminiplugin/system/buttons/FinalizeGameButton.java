package github.moriyoshi.comminiplugin.system.buttons;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.SequenceButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.AdminGameMenu;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class FinalizeGameButton extends
    SequenceButton<ComMiniPlugin, MenuHolder<ComMiniPlugin>, MenuHolder<ComMiniPlugin>, MenuHolder<ComMiniPlugin>> {

  public FinalizeGameButton() {
    super(new ItemButton<MenuHolder<ComMiniPlugin>>(
        new ItemBuilder(Material.ENDER_EYE)
            .name(GameSystem.inGame() ? GameSystem.nowGame().name + "<reset><red>を強制停止する"
                : "現在はゲーム中ではありません")
            .build()) {
      @Override
      public void onClick(@NotNull MenuHolder<ComMiniPlugin> holder,
          @NotNull InventoryClickEvent event) {
        GameSystem.finalizeGame();
      }
    },
        new RedirectButton<MenuHolder<ComMiniPlugin>>() {
          @Override
          public Inventory to(MenuHolder<ComMiniPlugin> MenuHolder, InventoryClickEvent event) {
            return new AdminGameMenu().getInventory();
          }
        });
  }
}