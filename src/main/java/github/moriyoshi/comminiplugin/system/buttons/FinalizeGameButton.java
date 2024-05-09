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
import org.jetbrains.annotations.NotNull;

public class FinalizeGameButton extends
    SequenceButton<ComMiniPlugin, MenuHolder<ComMiniPlugin>, MenuHolder<ComMiniPlugin>, MenuHolder<ComMiniPlugin>> {

  public FinalizeGameButton() {
    super(new ItemButton<>(
              new ItemBuilder(Material.ENDER_EYE)
                  .name(GameSystem.inGame() ? GameSystem.getNowGame().name + "<reset><red>を強制停止する"
                      : "現在はゲーム中ではありません")
                  .build()) {
            @Override
            public void onClick(@NotNull MenuHolder<ComMiniPlugin> holder,
                @NotNull InventoryClickEvent event) {
              GameSystem.finalizeGame();
            }
          },
        (RedirectButton<MenuHolder<ComMiniPlugin>>) (MenuHolder, event) -> new AdminGameMenu().getInventory()
    );
  }
}
