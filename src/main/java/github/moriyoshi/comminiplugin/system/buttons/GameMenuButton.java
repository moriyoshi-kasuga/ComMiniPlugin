package github.moriyoshi.comminiplugin.system.buttons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class GameMenuButton extends ItemButton<MenuHolder<ComMiniPlugin>> {
  private GameMenuButton(ItemStack item) {
    super(item);
  }

  public static GameMenuButton of() {
    if (GameSystem.inGame()) {
      var game = GameSystem.nowGame();
      return new GameMenuButton(new ItemBuilder(game.material).name(game.name).lore(game.description).build());
    }
    return new GameMenuButton(new ItemBuilder(Material.BEDROCK).name("<gray>ゲームは開催されていません").build());
  }

  @Override
  public void onClick(@NotNull MenuHolder<ComMiniPlugin> holder, @NotNull InventoryClickEvent event) {
    var p = (Player) event.getWhoClicked();
    if (GameSystem.inGame()) {
      GameSystem.nowGame().gameMenu(p).ifPresentOrElse(menu -> menu.openInv(p), () -> {
        ComMiniPrefix.MAIN.send(p, "<red>Menuは開けません");
      });
    }
  }

}