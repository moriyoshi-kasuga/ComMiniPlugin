package github.moriyoshi.comminiplugin.game.battleroyale;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.buttons.GameStartButton;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;

public class BRAdminMenu extends MenuHolder<ComMiniPlugin> {
  public BRAdminMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>OP:バトルロワイヤル");
    setButton(16, new GameStartButton());
    setButton(13, new ItemButton<>(new ItemBuilder(Material.DIRT).name("<green>バイオームフィールド").build()) {

      @Override
      public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
        val game = GameSystem.getGame(BRGame.class);
        game.setField(new BRField("biome", new Location(ComMiniWorld.GAME_WORLD, 1000.5, 60, 1000.5), 400, 50, 60 * 7));
      }

    });
  }
}
