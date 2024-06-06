package github.moriyoshi.comminiplugin.system.menu;

import github.moriyoshi.comminiplugin.constant.Messages;
import github.moriyoshi.comminiplugin.system.GameSystem;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

public interface OnlyBeforeStartGameMenu extends InventoryHolder {

  default BukkitRunnable createAutoCloseTask() {
    return new BukkitRunnable() {

      @Override
      public void run() {
        if (GameSystem.isIn()) {
          val message = additional();
          if (message != null) {
            val flag = Component.IS_NOT_EMPTY.test(message);
            com.google.common.collect.Lists.newArrayList(getInventory().getViewers())
                .forEach(
                    human -> {
                      if (flag) {
                        human.sendMessage(message);
                      }
                      human.closeInventory();
                    });
            return;
          }

          if (!GameSystem.isStarted()) {
            return;
          }
        }
        com.google.common.collect.Lists.newArrayList(getInventory().getViewers())
            .forEach(
                human -> {
                  Messages.GAME_FINAL_OR_START.send(human);
                  human.closeInventory();
                });
      }
    };
  }

  default boolean isClosed() {
    if (GameSystem.isIn()) {
      val message = additional();
      if (message != null) {
        val flag = Component.IS_NOT_EMPTY.test(message);
        com.google.common.collect.Lists.newArrayList(getInventory().getViewers())
            .forEach(
                human -> {
                  if (flag) {
                    human.sendMessage(message);
                  }
                  human.closeInventory();
                });
        return true;
      }

      if (!GameSystem.isStarted()) {
        return false;
      }
    }
    com.google.common.collect.Lists.newArrayList(getInventory().getViewers())
        .forEach(
            human -> {
              Messages.GAME_FINAL_OR_START.send(human);
              human.closeInventory();
            });
    return true;
  }

  default Component additional() {
    return null;
  }
}
