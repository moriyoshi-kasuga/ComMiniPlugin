package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
    if (getGame().isStarted()) {
      reducePlayer(p);
    }
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

  @Override
  public void damageByEntity(EntityDamageByEntityEvent e) {
    val player = (Player) e.getDamager();
    val item = player.getInventory().getItemInMainHand();
    if (item.isEmpty()) {
      getGame().prefix.send(player, "<red>素手での殴りは禁止されています!");
      e.setCancelled(true);
    }
  }

  private void reducePlayer(final Player p) {
    val game = getGame();
    p.getInventory().clear();
    val alives = game.players.entrySet().stream().filter(Entry::getValue).toList();
    if (alives.size() != 1) {
      game.teleportLobby(p);
      return;
    }
    game.endGame(Bukkit.getPlayer(alives.getFirst().getKey()));
  }

}
