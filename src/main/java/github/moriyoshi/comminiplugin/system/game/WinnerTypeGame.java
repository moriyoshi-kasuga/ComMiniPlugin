package github.moriyoshi.comminiplugin.system.game;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.system.InterfaceGame;
import github.moriyoshi.comminiplugin.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.scheduler.BukkitRunnable;

public interface WinnerTypeGame extends InterfaceGame {

  default void endGame(final String winner) {
    runPlayers(p -> getPrefix().send(p, "<red><u>" + winner + "</u>が勝ちました"));
    new BukkitRunnable() {

      @Override
      public void run() {
        GameSystem.finalGame();
      }
    }.runTaskLater(ComMiniPlugin.getPlugin(), 100);
  }

  default void endGame(final Component winner) {
    runPlayers(
        p ->
            getPrefix()
                .send(
                    p,
                    winner
                        .decorate(TextDecoration.UNDERLINED)
                        .append(
                            Util.mm("<white>が勝ちました")
                                .decoration(TextDecoration.UNDERLINED, false))));
    new BukkitRunnable() {

      @Override
      public void run() {
        GameSystem.finalGame();
      }
    }.runTaskLater(ComMiniPlugin.getPlugin(), 100);
  }
}
