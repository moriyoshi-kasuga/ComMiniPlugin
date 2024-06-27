package github.moriyoshi.comminiplugin.system.buttons;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.GameMessages;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.BigGameSystem;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GameStartButton extends ItemButton<MenuHolder<ComMiniPlugin>> {
  private GameStartButton(ItemStack item) {
    super(item);
  }

  public static GameStartButton of() {
    if (!BigGameSystem.isIn()) {
      return new GameStartButton(
          new ItemBuilder(Material.BEDROCK).name(GameMessages.GAME_NOT_FOUND.message).build());
    }
    if (BigGameSystem.isStarted()) {
      return new GameStartButton(
          new ItemBuilder(Material.BEDROCK).name(GameMessages.GAME_ALREADY_START.message).build());
    }
    return new GameStartButton(
        new ItemBuilder(BigGameSystem.getGame().getIcon()).name("<red>Start").build());
  }

  @Override
  public void onClick(
      @NotNull MenuHolder<ComMiniPlugin> holder, @NotNull InventoryClickEvent event) {
    val player = event.getWhoClicked();
    if (!BigGameSystem.isIn()) {
      GameMessages.GAME_NOT_FOUND.send(player);
      return;
    }
    if (BigGameSystem.isStarted()) {
      GameMessages.GAME_ALREADY_START.send(player);
      return;
    }
    BigGameSystem.startGame(((Player) event.getWhoClicked()));
  }
}
