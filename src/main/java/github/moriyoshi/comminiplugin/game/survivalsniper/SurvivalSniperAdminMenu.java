package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class SurvivalSniperAdminMenu extends MenuHolder<ComMiniPlugin> {

  public SurvivalSniperAdminMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>OP:サバイバルスナイパー");
    setButton(
        13,
        new ItemButton<MenuHolder<ComMiniPlugin>>(
            new ItemBuilder(Material.SPYGLASS).name("<red>Start").build()) {
          @Override
          public void onClick(@NotNull MenuHolder<ComMiniPlugin> holder,
              @NotNull InventoryClickEvent event) {
            GameSystem.startGame(((Player) event.getWhoClicked()));
          }
        }
    );
  }
}
