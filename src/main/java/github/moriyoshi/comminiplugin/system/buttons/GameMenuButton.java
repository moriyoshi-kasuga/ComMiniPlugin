package github.moriyoshi.comminiplugin.system.buttons;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.constant.Messages;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GameMenuButton extends ItemButton<MenuHolder<ComMiniPlugin>> {

  private GameMenuButton() {
    if (!GameSystem.isIn()) {
      setIcon(new ItemBuilder(Material.BEDROCK).name("<gray>ゲームは開催されていません").build());
      return;
    }
    var game = GameSystem.getGame();
    setIcon(
        new ItemBuilder(game.material)
            .name(game.name)
            .lore(game.description)
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
            .name(GameSystem.getGame().name + "<white>のメニューに戻る")
            .build());
  }

  @Override
  public void onClick(
      @NotNull MenuHolder<ComMiniPlugin> holder, @NotNull InventoryClickEvent event) {
    var player = (Player) event.getWhoClicked();
    if (!GameSystem.isIn()) {
      Messages.GAME_NOT_FOUND.send(player);
      return;
    }
    if (GameSystem.isStarted()) {
      ComMiniPrefix.MAIN.send(player, "<red>GameMenuは開けません");
      return;
    }
    GameSystem.getGame().createGameMenu(player).openInv(player);
  }
}
