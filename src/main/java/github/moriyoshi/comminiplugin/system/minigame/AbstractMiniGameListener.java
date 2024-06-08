package github.moriyoshi.comminiplugin.system.minigame;

import github.moriyoshi.comminiplugin.system.InterfaceGameListener;
import org.bukkit.event.player.PlayerQuitEvent;

public interface AbstractMiniGameListener<T extends AbstractMiniGame>
    extends InterfaceGameListener, IGetMiniGame<T> {

  @Override
  default void quit(final PlayerQuitEvent e) {
    getMiniGame().leavePlayer(e.getPlayer());
  }
}
