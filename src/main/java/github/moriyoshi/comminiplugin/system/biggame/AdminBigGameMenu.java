package github.moriyoshi.comminiplugin.system.biggame;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.BigGameSystem;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class AdminBigGameMenu extends MenuHolder<ComMiniPlugin> {

  public AdminBigGameMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<red>GameMenu");
    int slot = 10;
    // TODO: AbstractBigGame を内包したクラスを作ってそこで materialとかname,descriptionを設定する
    for (final AbstractBigGame game : BigGameSystem.games.values()) {
      setButton(
          slot,
          new ItemButton<MenuHolder<ComMiniPlugin>>(
              new ItemBuilder(game.material).name(game.name).lore(game.description).build()) {
            @Override
            public void onClick(
                @NotNull final MenuHolder<ComMiniPlugin> holder,
                @NotNull final InventoryClickEvent event) {
              final Player player = (Player) event.getWhoClicked();
              if (BigGameSystem.initializeGame(player, game.id)) {
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
