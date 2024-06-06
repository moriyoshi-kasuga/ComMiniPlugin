package github.moriyoshi.comminiplugin.game.battleroyale;

import github.moriyoshi.comminiplugin.system.AbstractGameListener;
import github.moriyoshi.comminiplugin.util.Util;
import java.util.Map.Entry;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

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
  public void damage(EntityDamageEvent e, Player player) {
    if (e.getCause().equals(DamageCause.FLY_INTO_WALL)) {
      e.setCancelled(true);
    }
  }

  @Override
  public void damageByEntity(EntityDamageByEntityEvent e) {
    if (!getGame().isCanPvP() && e.getEntity() instanceof Player) {
      if (e.getDamager() instanceof final Player attacker && getGame().isGamePlayer(attacker)) {
        getGame().prefix.send(attacker, "<red>まだPvPはできません");
        e.setCancelled(true);
        return;
      }
      if (e.getDamager() instanceof final Projectile projectile
          && projectile.getShooter() instanceof Player attacker
          && getGame().isGamePlayer(attacker)) {
        getGame().prefix.send(attacker, "<red>まだPvPはできません");
        e.setCancelled(true);
      }
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
    game.endGame(Bukkit.getPlayer(alives.getFirst().getKey()).getName());
  }

  @EventHandler
  public void interact(PlayerInteractEvent e) {
    val player = e.getPlayer();
    if (getGame().isGamePlayer(player) && e.hasBlock()) {
      val block = e.getClickedBlock();
      if (!(block.getState() instanceof Container)) {
        return;
      }
      val field = getGame().getField();
      if (field == null) {
        return;
      }
      if (!field.getTreasure().containsLocation(block.getLocation())) {
        e.setCancelled(true);
        getGame().prefix.send(player, "<red>宝箱以外のチェストは開けません");
      }
    }
  }
}
