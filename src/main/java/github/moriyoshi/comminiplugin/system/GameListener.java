package github.moriyoshi.comminiplugin.system;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.block.CustomBlock;
import github.moriyoshi.comminiplugin.system.biggame.BigGameSystem;
import github.moriyoshi.comminiplugin.system.minigame.AbstractMiniGame;
import github.moriyoshi.comminiplugin.system.minigame.MiniGameSystem;
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

  public static boolean isGamePlayer(Player p) {
    return BigGameSystem.isStarted() && BigGameSystem.getGame().isGamePlayer(p);
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

  public Optional<AbstractMiniGame> getMiniGameOptional(UUID uuid) {
    return Optional.ofNullable(
        MiniGameSystem.getMiniGame(ComMiniPlayer.getPlayer(uuid).getJoinGameIdentifier()));
  }

  public AbstractMiniGame getMiniGame(UUID uuid) {
    return MiniGameSystem.getMiniGame(ComMiniPlayer.getPlayer(uuid).getJoinGameIdentifier());
  }

  @EventHandler(priority = EventPriority.LOW)
  public void join(PlayerJoinEvent e) {
    val p = e.getPlayer();
    GameSystem.initializePlayer(p);
    if (ComMiniPlayer.getPlayer(p.getUniqueId()).isShouldLoadResourcePack()) {
      ResourcePackSystem.updateComMiniResourcePack(p);
    }
    Bukkit.getOnlinePlayers().stream()
        .filter(
            player -> {
              if (player.equals(p)) {
                return false;
              }
              val id = ComMiniPlayer.getPlayer(player.getUniqueId()).getJoinGameIdentifier();
              return id == null || id.isMiniGameKey();
            })
        .forEach(
            player -> {
              p.showPlayer(ComMiniPlugin.getPlugin(), player);
              player.showPlayer(ComMiniPlugin.getPlugin(), p);
            });
  }

  @EventHandler(priority = EventPriority.LOW)
  public void quit(PlayerQuitEvent e) {
    val p = e.getPlayer();
    if (BigGameSystem.isIn() && BigGameSystem.getGame().isGamePlayer(p)) {
      BigGameSystem.getGame().listener.quit(e);
      return;
    }
    getMiniGameOptional(p.getUniqueId()).ifPresent(minigame -> minigame.listener.quit(e));
  }

  @EventHandler(priority = EventPriority.LOW)
  public void death(PlayerDeathEvent e) {
    e.setCancelled(true);
    val p = e.getPlayer();
    if (isGamePlayer(p)) {
      BigGameSystem.getGame().listener.death(e);
      return;
    }

    val minigame = getMiniGame(p.getUniqueId());
    if (minigame != null) {
      minigame.listener.death(e);
      return;
    }
    new BukkitRunnable() {

      @Override
      public void run() {
        GameSystem.initializePlayer(e.getPlayer());
      }
    }.runTask(ComMiniPlugin.getPlugin());
  }

  @EventHandler(priority = EventPriority.LOW)
  public void damage(EntityDamageEvent e) {
    if (e.getEntity() instanceof Player player) {
      if (e.getCause().equals(DamageCause.FALL)) {
        e.setCancelled(true);
      }
      if (BigGameSystem.isStarted() && isGamePlayer(player)) {
        BigGameSystem.getGame().listener.damage(e, player);
        return;
      }
      getMiniGameOptional(player.getUniqueId())
          .ifPresent(minigame -> minigame.listener.damage(e, player));
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
      val game = BigGameSystem.getGame();
      if (BigGameSystem.isStarted() && game.isGamePlayer(attacker) && game.isGamePlayer(victim)) {
        game.listener.damageByEntity(e, attacker, victim);
        return;
      }
      val minigame = getMiniGame(attacker.getUniqueId());
      if (minigame != null
          && minigame == getMiniGame(victim.getUniqueId())
          && minigame.isGamePlayer(attacker)
          && minigame.isGamePlayer(victim)) {
        minigame.listener.damageByEntity(e, attacker, victim);
        return;
      }
    }
    if (attacker != null && isCancelLobbyPlayer(attacker)) {
      e.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void blockBreak(BlockBreakEvent e) {
    val p = e.getPlayer();
    if (isGamePlayer(p)) {
      BigGameSystem.getGame().listener.blockBreak(e);
      return;
    }
    val minigame = getMiniGame(p.getUniqueId());
    if (minigame != null) {
      minigame.listener.blockBreak(e);
      return;
    }
    if (isCancelLobbyPlayer(p)) {
      e.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void blockPlace(BlockPlaceEvent e) {
    val p = e.getPlayer();
    if (isGamePlayer(e.getPlayer())) {
      BigGameSystem.getGame().listener.blockPlace(e);
      return;
    }
    val minigame = getMiniGame(p.getUniqueId());
    if (minigame != null) {
      minigame.listener.blockPlace(e);
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
