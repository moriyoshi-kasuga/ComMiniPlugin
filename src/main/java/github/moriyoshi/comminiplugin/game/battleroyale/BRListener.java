package github.moriyoshi.comminiplugin.game.battleroyale;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import github.moriyoshi.comminiplugin.system.AbstractGameListener;
import github.moriyoshi.comminiplugin.util.Util;
import lombok.val;

public class BRListener implements AbstractGameListener<BRGame> {

  @Override
  public void blockBreak(BlockBreakEvent e) {
    e.setCancelled(true);
  }

  @Override
  public void blockPlace(BlockPlaceEvent e) {
    e.setCancelled(true);
  }

  @Override
  public void quit(final PlayerQuitEvent e) {
    val p = e.getPlayer();
    val flag = getGame().players.remove(p.getUniqueId());
    if (!flag) {
      return;
    }
    reducePlayer(p);
  }

  @EventHandler
  public void tp(final PlayerTeleportEvent e) {
    val p = e.getPlayer();
    if (!getGame().isGamePlayer(p)) {
      return;
    }
    if (e.getCause().equals(TeleportCause.SPECTATE)
        && !getGame().getLobby().getWorld().getWorldBorder().isInside(e.getTo())) {
      getGame().prefix.send(p, "<red>範囲外にスペクテイターのテレポートは使えません");
      e.setCancelled(true);
    }
  }

  @Override
  public void death(PlayerDeathEvent e) {
    val p = e.getPlayer();
    p.setGameMode(GameMode.SPECTATOR);
    val game = getGame();
    game.runPlayers(pl -> Util.send(pl, e.deathMessage()));
    game.players.put(p.getUniqueId(), false);
    reducePlayer(p);
  }

  private void reducePlayer(final Player p) {
    val game = getGame();
    p.getInventory().clear();
    val alives = game.players.entrySet().stream().filter(entry -> entry.getValue()).toList();
    if (alives.size() != 1) {
      game.teleportLobby(p);
      return;
    }
    game.endGame(Bukkit.getPlayer(alives.get(0).getKey()));
  }

}
