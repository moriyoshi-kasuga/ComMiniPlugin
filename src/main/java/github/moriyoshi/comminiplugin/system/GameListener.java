package github.moriyoshi.comminiplugin.system;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;

public class GameListener implements Listener {

  private static final GameListener INSTANCE = new GameListener();

  public static GameListener getInstance() {
    return INSTANCE;
  }

  private GameListener() {
    new BukkitRunnable() {

      @Override
      public void run() {
        Bukkit.getOnlinePlayers().forEach(p -> {
          if (GamePlayer.getPlayer(p.getUniqueId()).isHunger()) {
            return;
          }
          p.setFoodLevel(20);
        });
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 20, 20);
  }

  public static boolean isGamePlayer(Player p) {
    return GameSystem.isStarted() && GameSystem.nowGame().isGamePlayer(p);
  }

  public static boolean isDebugPlayer(Player p) {
    return GamePlayer.getPlayer(p.getUniqueId()).isDebug();
  }

  @EventHandler
  public void join(PlayerJoinEvent e) {
    var p = e.getPlayer();
    GameSystem.initializePlayer(p);
    if (isGamePlayer(p)) {
      GameSystem.nowGame().listener.join(e);
      return;
    }
  }

  @EventHandler
  public void quit(PlayerQuitEvent e) {
    if (isGamePlayer(e.getPlayer())) {
      GameSystem.nowGame().listener.quit(e);
    }
  }

  @EventHandler
  public void death(PlayerDeathEvent e) {
    e.setCancelled(true);
    if (isGamePlayer(e.getPlayer())) {
      GameSystem.nowGame().listener.death(e);
      return;
    }
    e.getPlayer().teleport(ComMiniWorld.LOBBY);
  }

  @EventHandler
  public void damage(EntityDamageEvent e) {
    if (e.getEntity() instanceof Player attacker
        && !isGamePlayer(attacker)
        && e.getCause().equals(DamageCause.FALL)) {
      e.setCancelled(true);
      return;
    }
    if (GameSystem.isStarted()) {
      GameSystem.nowGame().listener.damage(e);
    }
  }

  @EventHandler
  public void damageByEntity(EntityDamageByEntityEvent e) {
    if (!(e.getDamager() instanceof Player attacker)) {
      return;
    }

    if (isGamePlayer(attacker)) {
      GameSystem.nowGame().listener.damageByEntity(e);
      return;
    }
    if (isDebugPlayer(attacker)) {
      return;
    }
    if (attacker.getGameMode() != GameMode.CREATIVE) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void blockBreak(BlockBreakEvent e) {
    if (isGamePlayer(e.getPlayer())) {
      GameSystem.nowGame().listener.blockBreak(e);
      return;
    }
    if (isDebugPlayer(e.getPlayer())) {
      return;
    }
    if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void blockPlace(BlockPlaceEvent e) {
    if (isGamePlayer(e.getPlayer())) {
      GameSystem.nowGame().listener.blockPlace(e);
      return;
    }
    if (isDebugPlayer(e.getPlayer())) {
      return;
    }
    if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent e) {
    var player = GamePlayer.getPlayer(e.getPlayer().getUniqueId());
    if (player.getDisableMoveTick() == -1) {
      return;
    }
    Location from = e.getFrom();
    Location to = Objects.requireNonNull(e.getTo());
    from.setYaw(to.getYaw());
    from.setPitch(to.getPitch());
    e.setTo(from);
  }

}
