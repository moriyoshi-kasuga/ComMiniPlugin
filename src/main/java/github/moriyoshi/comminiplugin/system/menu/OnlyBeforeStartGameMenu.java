package github.moriyoshi.comminiplugin.system.menu;

import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

import github.moriyoshi.comminiplugin.constant.Messages;
import github.moriyoshi.comminiplugin.system.GameSystem;

public interface OnlyBeforeStartGameMenu extends InventoryHolder {

  default BukkitRunnable createAutoCloseTask() {
    return new BukkitRunnable() {

      @Override
      public void run() {
        if (GameSystem.isIn() && !GameSystem.isStarted()) {
          return;
        }
        getInventory().getViewers().forEach(human -> {
          Messages.GAME_FINAL_OR_START.send(human);
          human.closeInventory();
        });
      }

    };

  }

  default boolean isClosed() {
    if (GameSystem.isIn() && !GameSystem.isStarted()) {
      return true;
    }
    getInventory().getViewers().forEach(human -> {
      Messages.GAME_FINAL_OR_START.send(human);
      human.closeInventory();
    });
    return false;
  }

}
