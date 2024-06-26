package github.moriyoshi.comminiplugin.system.minigame;

import github.moriyoshi.comminiplugin.system.IGameListener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public interface AbstractMiniGameListener<T extends AbstractMiniGame>
    extends IGameListener, IGetMiniGame<T> {

  @Override
  default void quit(final PlayerQuitEvent e) {}

  @Override
  default void blockBreak(BlockBreakEvent e) {
    e.setCancelled(true);
  }

  @Override
  default void blockPlace(BlockPlaceEvent e) {
    e.setCancelled(true);
  }
}
