package github.moriyoshi.comminiplugin.system;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import github.moriyoshi.comminiplugin.ComMiniPlugin;

public class GameListener implements Listener {

  private static final GameListener INSTANCE = new GameListener();

  public static GameListener getInstance() {
    return INSTANCE;
  }

  public static boolean isGamePlayer(Player p, Class<? extends Event> clazz) {
    return GameSystem.isStarted() && GameSystem.getGame().isGamePlayer(p, clazz);
  }

  public static boolean isDebugPlayer(Player p) {
    return ComMiniPlayer.getPlayer(p.getUniqueId()).isDebug();
  }

  private GameListener() {
    new BukkitRunnable() {

      @Override
      public void run() {
        Bukkit.getOnlinePlayers().forEach(p -> {
          if (ComMiniPlayer.getPlayer(p.getUniqueId()).isHunger()) {
            return;
          }
          p.setFoodLevel(20);
        });
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 20, 20);
  }

  @EventHandler
  public void join(PlayerJoinEvent e) {
    var p = e.getPlayer();
    if (GameSystem.isIn() && GameSystem.getGame().isGamePlayer(p, PlayerJoinEvent.class)
        && GameSystem.getGame().listener.join(e)) {
      return;
    }
    GameSystem.initializePlayer(p);
  }

  @EventHandler
  public void quit(PlayerQuitEvent e) {
    if (GameSystem.isIn() && GameSystem.getGame().isGamePlayer(e.getPlayer(), PlayerQuitEvent.class)) {
      GameSystem.getGame().listener.quit(e);
    }
  }

  @EventHandler
  public void death(PlayerDeathEvent e) {
    e.setCancelled(true);
    if (isGamePlayer(e.getPlayer(), PlayerDeathEvent.class)) {
      GameSystem.getGame().listener.death(e);
      return;
    }
    GameSystem.initializePlayer(e.getPlayer());
  }

  @EventHandler
  public void damage(EntityDamageEvent e) {
    if (e.getEntity() instanceof Player player) {
      if (e.getCause().equals(DamageCause.FALL)) {
        e.setCancelled(true);
      }
      if (GameSystem.isStarted() && isGamePlayer(player, EntityDamageEvent.class)) {
        GameSystem.getGame().listener.damage(e, player);
      }
    }
  }

  @EventHandler
  public void damageByEntity(EntityDamageByEntityEvent e) {
    if (!(e.getDamager() instanceof Player attacker)) {
      return;
    }

    if (isGamePlayer(attacker, EntityDamageByEntityEvent.class)) {
      GameSystem.getGame().listener.damageByEntity(e);
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
    if (isGamePlayer(e.getPlayer(), BlockBreakEvent.class)) {
      GameSystem.getGame().listener.blockBreak(e);
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
    if (isGamePlayer(e.getPlayer(), BlockPlaceEvent.class)) {
      GameSystem.getGame().listener.blockPlace(e);
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
  public void move(PlayerMoveEvent e) {
    var p = e.getPlayer();
    p.getActivePotionEffects().forEach(effect -> {
      if (effect.getType().equals(PotionEffectType.SLOWNESS) && effect.getAmplifier() == 138) {
        Location from = e.getFrom();
        Location to = e.getTo();
        from.setYaw(to.getYaw());
        from.setPitch(to.getPitch());
        e.setTo(from);
      }
    });
  }
}
