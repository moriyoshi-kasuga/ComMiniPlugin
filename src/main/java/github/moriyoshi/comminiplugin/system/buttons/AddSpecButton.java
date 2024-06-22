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
import org.jetbrains.annotations.NotNull;

public class AddSpecButton extends ItemButton<MenuHolder<ComMiniPlugin>> {

  private AddSpecButton() {
    if (!BigGameSystem.isStarted()) {
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
    if (!BigGameSystem.isStarted()) {
      GameMessages.GAME_NOT_START.send(player);
      return;
    }
    if (BigGameSystem.getGame().addSpec(player)) {
      ComMiniPlugin.MAIN.send(player, "<gray>観戦を開始しました");
    } else {
      ComMiniPlugin.MAIN.send(player, "<red>観戦に入れません");
    }
  }
}
