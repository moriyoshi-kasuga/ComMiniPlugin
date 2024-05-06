package github.moriyoshi.comminiplugin.system.buttons;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
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

  private GameMenuButton(ItemStack item) {
    super(item);
  }

  public static GameMenuButton of() {
    if (GameSystem.inGame() && !GameSystem.isStarted()) {
      var game = GameSystem.nowGame();
      return new GameMenuButton(
          new ItemBuilder(game.material).name(game.name).lore(game.description).build());
    }
    return new GameMenuButton(
        new ItemBuilder(Material.BEDROCK).name("<gray>ゲームは開催されていません").build());
  }

  public static GameMenuButton back() {
    if (GameSystem.inGame() && !GameSystem.isStarted()) {
      return new GameMenuButton(
          new ItemBuilder(Material.IRON_DOOR).name(
              GameSystem.nowGame().name + "<white>のメニューに戻る").build());
    }
    return new GameMenuButton(
        new ItemBuilder(Material.BEDROCK).name("<gray>ゲームは開催されていません").build());
  }

  @Override
  public void onClick(@NotNull MenuHolder<ComMiniPlugin> holder,
      @NotNull InventoryClickEvent event) {
    var p = (Player) event.getWhoClicked();
    if (GameSystem.inGame()) {
      GameSystem.nowGame().gameMenu(p).ifPresentOrElse(menu -> menu.openInv(p),
          () -> ComMiniPrefix.MAIN.send(p, "<red>Menuは開けません")
      );
    } else {
      ComMiniPrefix.MAIN.send(p, "<red>現在はゲーム中ではありません");
    }
  }

}
