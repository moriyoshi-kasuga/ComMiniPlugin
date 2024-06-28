package github.moriyoshi.comminiplugin.system;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class AdminBigGameMenu extends MenuHolder<ComMiniPlugin> {

  public AdminBigGameMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<red>GameMenu");
    int slot = 10;
    for (val game : AllBigGames.values()) {
      setButton(
          slot,
          new ItemButton<MenuHolder<ComMiniPlugin>>(
              new ItemBuilder(game.icon).name(game.name).lore(game.description).build()) {
            @Override
            public void onClick(
                @NotNull final MenuHolder<ComMiniPlugin> holder,
                @NotNull final InventoryClickEvent event) {
              final Player player = (Player) event.getWhoClicked();
              if (BigGameSystem.initializeGame(player, game.getSupplier(player))) {
                ((RedirectButton<MenuHolder<?>>)
                        (MenuHolder, event1) ->
                            BigGameSystem.getGame().createAdminMenu().getInventory())
                    .onClick(holder, event);
              }
            }
          });
      slot++;
    }
  }
}
