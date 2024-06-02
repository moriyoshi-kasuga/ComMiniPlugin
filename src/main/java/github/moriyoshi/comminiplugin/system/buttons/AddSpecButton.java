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
import org.jetbrains.annotations.NotNull;

public class AddSpecButton extends ItemButton<MenuHolder<ComMiniPlugin>> {

  private AddSpecButton() {
    if (!GameSystem.isStarted()) {
      setIcon(new ItemBuilder(Material.BEDROCK).name("<gray>ゲームは開始されていないので観戦できません").build());
      return;
    }
    setIcon(new ItemBuilder(Material.ENDER_EYE).name("<gray>観戦する").build());
  }

  public static AddSpecButton of() {
    return new AddSpecButton();
  }

  @Override
  public void onClick(
      @NotNull MenuHolder<ComMiniPlugin> holder, @NotNull InventoryClickEvent event) {
    var player = (Player) event.getWhoClicked();
    if (!GameSystem.isStarted()) {
      Messages.GAME_NOT_START.send(player);
      return;
    }
    if (GameSystem.getGame().addSpec(player)) {
      ComMiniPrefix.MAIN.send(player, "<gray>観戦を開始しました");
    } else {
      ComMiniPrefix.MAIN.send(player, "<red>観戦に入れません");
    }
  }
}
