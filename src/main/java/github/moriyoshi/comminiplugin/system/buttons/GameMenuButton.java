package github.moriyoshi.comminiplugin.system.buttons;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.GameMessages;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.biggame.BigGameSystem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GameMenuButton extends ItemButton<MenuHolder<ComMiniPlugin>> {

  private GameMenuButton() {
    if (!BigGameSystem.isIn()) {
      setIcon(new ItemBuilder(Material.BEDROCK).name("<gray>ゲームは開催されていません").build());
      return;
    }
    var game = BigGameSystem.getGame();
    setIcon(
        new ItemBuilder(game.material)
            .name(game.name)
            .lore(game.description)
            .flags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            .addLore("", "<gray>クリックでゲームメニューを開く")
            .build());
  }

  private GameMenuButton(ItemStack stack) {
    super(stack);
  }

  public static GameMenuButton of() {
    return new GameMenuButton();
  }

  public static GameMenuButton back() {
    return new GameMenuButton(
        new ItemBuilder(Material.IRON_DOOR)
            .name(BigGameSystem.getGame().name + "<white>のメニューに戻る")
            .build());
  }

  @Override
  public void onClick(
      @NotNull MenuHolder<ComMiniPlugin> holder, @NotNull InventoryClickEvent event) {
    var player = (Player) event.getWhoClicked();
    if (!BigGameSystem.isIn()) {
      GameMessages.GAME_NOT_FOUND.send(player);
      return;
    }
    if (BigGameSystem.isStarted()) {
      ComMiniPlugin.MAIN.send(player, "<red>GameMenuは開けません");
      return;
    }
    BigGameSystem.getGame().createGameMenu(player).openInv(player);
  }
}
