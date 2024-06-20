package github.moriyoshi.comminiplugin.system.buttons;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.GameMessages;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.game.GameSystem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GameHelpMenuButton extends ItemButton<MenuHolder<ComMiniPlugin>> {

  private GameHelpMenuButton() {
    if (!GameSystem.isIn()) {
      setIcon(new ItemBuilder(Material.BEDROCK).name("<gray>ゲームは開催されていません").build());
      return;
    }
    setIcon(new ItemBuilder(Material.BOOK).glow().name("<yellow>ヘルプメニュー").build());
  }

  private GameHelpMenuButton(ItemStack stack) {
    super(stack);
  }

  public static GameHelpMenuButton of() {
    return new GameHelpMenuButton();
  }

  @Override
  public void onClick(
      @NotNull MenuHolder<ComMiniPlugin> holder, @NotNull InventoryClickEvent event) {
    var player = event.getWhoClicked();
    if (!GameSystem.isIn()) {
      GameMessages.GAME_NOT_FOUND.send(player);
      return;
    }
    GameSystem.getGame().createHelpMenu().openInv(player);
  }
}
