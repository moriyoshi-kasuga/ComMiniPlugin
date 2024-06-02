package github.moriyoshi.comminiplugin.system.gametype;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.InterfaceGame;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public interface WinnerTypeGame extends InterfaceGame {

  default void endGame(final Player winner) {
    runPlayers(p -> getPrefix().send(p, "<red><u>" + winner.getName() + "</u>が勝ちました"));
    new BukkitRunnable() {

      @Override
      public void run() {
        GameSystem.finalGame();
      }
    }.runTaskLater(ComMiniPlugin.getPlugin(), 100);
  }
}
