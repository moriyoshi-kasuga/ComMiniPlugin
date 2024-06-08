package github.moriyoshi.comminiplugin.system.game;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class AdminGameMenu extends MenuHolder<ComMiniPlugin> {

  public AdminGameMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<red>GameMenu");
    int slot = 10;
    for (final AbstractGame game : GameSystem.games.values()) {
      setButton(
          slot,
          new ItemButton<MenuHolder<ComMiniPlugin>>(
              new ItemBuilder(game.material).name(game.name).lore(game.description).build()) {
            @Override
            public void onClick(
                @NotNull final MenuHolder<ComMiniPlugin> holder,
                @NotNull final InventoryClickEvent event) {
              final Player player = (Player) event.getWhoClicked();
              if (GameSystem.initializeGame(player, game.id)) {
                ((RedirectButton<MenuHolder<?>>)
                        (MenuHolder, event1) -> game.createAdminMenu().getInventory())
                    .onClick(holder, event);
              }
            }
          });
      slot++;
    }
  }
}
