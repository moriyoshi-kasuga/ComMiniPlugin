package github.moriyoshi.comminiplugin.system.buttons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.Messages;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;

public class GameStartButton extends ItemButton<MenuHolder<ComMiniPlugin>> {
  private GameStartButton(ItemStack item) {
    super();
  }

  public static GameStartButton of() {
    if (!GameSystem.isIn()) {
      return new GameStartButton(new ItemBuilder(Material.BEDROCK).name("<red>not initialize game").build());
    }
    if (GameSystem.isStarted()) {
      return new GameStartButton(new ItemBuilder(Material.BEDROCK).name("<red>game is already started").build());
    }
    return new GameStartButton(new ItemBuilder(GameSystem.getGame().material).name("<red>Start").build());

  }

  @Override
  public void onClick(@NotNull MenuHolder<ComMiniPlugin> holder, @NotNull InventoryClickEvent event) {
    val player = event.getWhoClicked();
    if (!GameSystem.isIn()) {
      Messages.GAME_NOT_FOUND.send(player);
      return;
    }
    if (GameSystem.isStarted()) {
      Messages.GAME_ALREADY_START.send(player);
      return;
    }
    GameSystem.startGame(((Player) event.getWhoClicked()));
  }
}
