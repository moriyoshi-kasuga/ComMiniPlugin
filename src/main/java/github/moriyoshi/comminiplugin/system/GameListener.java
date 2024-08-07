package github.moriyoshi.comminiplugin.system;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.block.CustomBlock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

public class GameListener implements Listener {

  private static final GameListener INSTANCE = new GameListener();
  private static final Map<UUID, BiConsumer<Projectile, ProjectileHitEvent>> projectileHitMap =
      new HashMap<>();
  private static final Map<UUID, BiConsumer<Projectile, EntityDamageByEntityEvent>>
      projectileDamageMap = new HashMap<>();
  private static final Map<UUID, Location> disableMoveLocation = new HashMap<>();

  private GameListener() {
    new BukkitRunnable() {

      @Override
      public void run() {
        Bukkit.getOnlinePlayers()
            .forEach(
                p -> {
                  val gp = ComMiniPlayer.getPlayer(p.getUniqueId());
                  if (!gp.isHunger()) {
                    p.setFoodLevel(20);
                  }
                });
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 20, 20);
  }

  public static GameListener getInstance() {
    return INSTANCE;
  }

  public static boolean isDebugPlayer(Player p) {
    return ComMiniPlayer.getPlayer(p.getUniqueId()).isDebug();
  }

  public static void addProjectileHitListener(
      UUID id, BiConsumer<Projectile, ProjectileHitEvent> l) {
    projectileHitMap.put(id, l);
  }

  public static void addProjectileDamageListener(
      UUID id, BiConsumer<Projectile, EntityDamageByEntityEvent> l) {
    projectileDamageMap.put(id, l);
  }

  @Nullable
  public AbstractGame getGameFromPlayer(UUID uuid) {
    return GameSystem.getGame(ComMiniPlayer.getPlayer(uuid).getJoinGameKey());
  }

  @EventHandler(priority = EventPriority.LOW)
  public void join(PlayerJoinEvent e) {
    val p = e.getPlayer();
    GameSystem.initializePlayer(p);
    if (ComMiniPlayer.getPlayer(p.getUniqueId()).isShouldLoadResourcePack()) {
      new BukkitRunnable() {
        @Override
        public void run() {
          ResourcePackUtil.send(p);
        }
      }.runTaskLater(ComMiniPlugin.getPlugin(), 10L);
    }
    Bukkit.getOnlinePlayers().stream()
        .filter(
            player -> {
              if (player.equals(p)) {
                return false;
              }
              return ComMiniPlayer.getPlayer(player.getUniqueId()).getJoinGameKey() == null;
            })
        .forEach(
            player -> {
              p.showPlayer(ComMiniPlugin.getPlugin(), player);
              player.showPlayer(ComMiniPlugin.getPlugin(), p);
            });
  }

  @EventHandler(priority = EventPriority.LOW)
  public void quit(PlayerQuitEvent e) {
    val game = getGameFromPlayer(e.getPlayer().getUniqueId());
    if (game != null) game.listener.quit(e);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void death(PlayerDeathEvent e) {
    e.setCancelled(true);
    val p = e.getPlayer();
    val game = getGameFromPlayer(p.getUniqueId());
    if (game != null) {
      game.listener.death(e);
      return;
    }
    new BukkitRunnable() {

      @Override
      public void run() {
        GameSystem.initializePlayer(p);
      }
    }.runTask(ComMiniPlugin.getPlugin());
  }

  @EventHandler(priority = EventPriority.LOW)
  public void damage(EntityDamageEvent e) {
    if (e.getEntity() instanceof Player player) {
      if (e.getCause().equals(DamageCause.FALL)) {
        e.setCancelled(true);
      }
      val game = getGameFromPlayer(player.getUniqueId());
      if (game != null) game.listener.damage(e, player);
    }
  }

  @Nullable
  public static Player getEntityToPlayer(Entity entity) {
    if (entity instanceof Player p) {
      return p;
    } else if (entity instanceof final Projectile projectile
        && projectile.getShooter() instanceof Player p) {
      return p;
    }
    return null;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void damageByEntity(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Projectile projectile) {
      val consumer = projectileDamageMap.remove(projectile.getUniqueId());
      if (consumer != null) {
        consumer.accept(projectile, e);
      }
    }

    val attacker = getEntityToPlayer(e.getDamager());
    val victim = e.getEntity() instanceof Player p ? p : null;

    if (attacker != null && victim != null) {
      val game = getGameFromPlayer(attacker.getUniqueId());
      if (game != null) {
        val game2 = getGameFromPlayer(victim.getUniqueId());
        if (game == game2) {
          game.listener.damageByEntity(e, attacker, victim);
          return;
        }
      }
    }
    if (attacker != null && isCancelLobbyPlayer(attacker)) {
      e.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void blockBreak(BlockBreakEvent e) {
    val p = e.getPlayer();
    val game = getGameFromPlayer(p.getUniqueId());
    if (game != null) {
      game.listener.blockBreak(e);
      return;
    }
    if (isCancelLobbyPlayer(p)) {
      e.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void blockPlace(BlockPlaceEvent e) {
    val p = e.getPlayer();
    val game = getGameFromPlayer(p.getUniqueId());
    if (game != null) {
      game.listener.blockPlace(e);
      return;
    }
    if (isCancelLobbyPlayer(p)) {
      e.setCancelled(true);
    }
  }

  @SuppressWarnings("deprecation")
  @EventHandler(priority = EventPriority.LOW)
  public void jump(PlayerJumpEvent e) {
    var p = e.getPlayer();
    if (!p.isOnGround()) {
      return;
    }
    for (val effect : p.getActivePotionEffects())
      if (effect.getType().equals(PotionEffectType.SLOWNESS) && effect.getAmplifier() == 138) {
        e.setCancelled(true);
        return;
      }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void effectEvent(EntityPotionEffectEvent e) {
    if (e.getEntity() instanceof Player player && e.getNewEffect() == null) {
      val effect = e.getOldEffect();
      if (effect.getType().equals(PotionEffectType.SLOWNESS) && effect.getAmplifier() == 138) {
        disableMoveLocation.remove(player.getUniqueId());
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void teleport(PlayerTeleportEvent e) {
    var p = e.getPlayer();
    for (val effect : p.getActivePotionEffects()) {
      if (effect.getType().equals(PotionEffectType.SLOWNESS) && effect.getAmplifier() == 138) {
        disableMoveLocation.remove(p.getUniqueId());
        return;
      }
    }
  }

  @SuppressWarnings("deprecation")
  @EventHandler(priority = EventPriority.LOW)
  public void move(PlayerMoveEvent e) {
    var p = e.getPlayer();
    for (val effect : p.getActivePotionEffects()) {
      if (effect.getType().equals(PotionEffectType.SLOWNESS) && effect.getAmplifier() == 138) {
        Location loc = disableMoveLocation.get(p.getUniqueId());
        if (loc == null) {
          if (!p.isOnGround()) {
            return;
          }
          disableMoveLocation.put(p.getUniqueId(), loc);
          return;
        }
        Location to = e.getTo();
        loc.setYaw(to.getYaw());
        loc.setPitch(to.getPitch());
        e.setTo(loc);
        return;
      }
    }
    val to = e.getTo().clone();
    for (val loc : List.of(to, to.clone().subtract(0, 0.1, 0))) {
      if (CustomBlock.isCustomBlock(loc)) {
        CustomBlock.getCustomBlock(loc).walk(e);
        return;
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void regain(EntityRegainHealthEvent e) {
    if (!(e.getEntity() instanceof Player p)) {
      return;
    }
    if (e.getRegainReason() == RegainReason.SATIATED
        && !ComMiniPlayer.getPlayer(p.getUniqueId()).isCanFoodRegain()) {
      e.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void projectileHit(ProjectileHitEvent e) {
    Optional.ofNullable(projectileHitMap.get(e.getEntity().getUniqueId()))
        .ifPresent(
            consumer -> {
              val entity = e.getEntity();
              consumer.accept(entity, e);
              if (e.isCancelled() && e.getHitEntity() != null) {
                return;
              }
              projectileHitMap.remove(entity.getUniqueId());
            });
  }

  public boolean isCancelLobbyPlayer(Player player) {
    if (isDebugPlayer(player)) {
      return false;
    }
    return player.getGameMode() != GameMode.CREATIVE;
  }
}
