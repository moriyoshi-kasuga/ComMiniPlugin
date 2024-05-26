package github.moriyoshi.comminiplugin.game.battleroyale;

import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.IGetGame;
import github.moriyoshi.comminiplugin.system.menu.OnlyBeforeStartGameMenu;

public class BRMenu extends MenuHolder<ComMiniPlugin> implements IGetGame<BRGame>, OnlyBeforeStartGameMenu {

  private final BukkitRunnable task = createTask();

  public BRMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<yellow>バトルロワイヤル");
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    this.task.cancel();
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {
    this.task.runTaskTimer(getPlugin(), 1, 1);
  }

}
