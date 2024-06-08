package github.moriyoshi.comminiplugin.system;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.block.CustomBlock;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.util.NMSUtil;
import github.moriyoshi.comminiplugin.util.ResourcePackUtil;
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
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class GameListener implements Listener {

  private static final GameListener INSTANCE = new GameListener();
  private static final Map<UUID, BiConsumer<Projectile, ProjectileHitEvent>> projectileHitMap =
      new HashMap<>();
  private static final Map<UUID, BiConsumer<Projectile, EntityDamageByEntityEvent>>
      projectileDamageMap = new HashMap<>();
  private static final Map<UUID, Location> disableMoveLocation = new HashMap<>();

  private GameListener() {
    new BukkitRunnable() {
      private String oldHash = ResourcePackUtil.getComMiniResourcePackHash();

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
        val newHash = ResourcePackUtil.getComMiniResourcePackHash();
        if (!oldHash.equalsIgnoreCase(newHash)) {
          oldHash = newHash;
          ComMiniPrefix.MAIN.cast("<red>リソースパックの更新があります!<gray>メニューから更新してください");
        }
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 20, 20);
  }

  public static GameListener getInstance() {
    return INSTANCE;
  }

  public static boolean isGamePlayer(Player p, Class<? extends Event> clazz) {
    return GameSystem.isStarted() && GameSystem.getGame().isGamePlayer(p, clazz);
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

  @EventHandler
  public void join(PlayerJoinEvent e) {
    var p = e.getPlayer();
    if (ComMiniPlayer.getPlayer(p.getUniqueId()).isShouldLoadResourcePack()) {
      ResourcePackUtil.updateComMiniResoucePack(p);
    }
    NMSUtil.sendPlayerHidePackt(
        player -> !player.equals(p) && ComMiniPlayer.getPlayer(player.getUniqueId()).isJoinGame(),
        List.of(p.getUniqueId()));
    if (GameSystem.isIn()
        && GameSystem.getGame().isGamePlayer(p, PlayerJoinEvent.class)
        && GameSystem.getGame().listener.join(e)) {
      return;
    }
    new BukkitRunnable() {

      @Override
      public void run() {
        GameSystem.initializePlayer(e.getPlayer());
      }
    }.runTask(ComMiniPlugin.getPlugin());
  }

  @EventHandler
  public void quit(PlayerQuitEvent e) {
    if (GameSystem.isIn()
        && GameSystem.getGame().isGamePlayer(e.getPlayer(), PlayerQuitEvent.class)) {
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
    new BukkitRunnable() {

      @Override
      public void run() {
        GameSystem.initializePlayer(e.getPlayer());
      }
    }.runTask(ComMiniPlugin.getPlugin());
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
    if (e.getDamager() instanceof Projectile projectile) {
      Optional.ofNullable(projectileDamageMap.remove(projectile.getUniqueId()))
          .ifPresent(consumer -> consumer.accept(projectile, e));
    }

    if (GameSystem.isStarted()
        && (!(e.getDamager() instanceof Player player)
            || isGamePlayer(player, EntityDamageByEntityEvent.class))) {
      GameSystem.getGame().listener.damageByEntity(e);
      return;
    }

    if (e.getDamager() instanceof Player attacker) {
      if (isDebugPlayer(attacker)) {
        return;
      }
      if (attacker.getGameMode() != GameMode.CREATIVE) {
        e.setCancelled(true);
      }
    }
    if (e.getDamager() instanceof Projectile projectile
        && projectile.getShooter() instanceof Player attacker) {
      if (isDebugPlayer(attacker)) {
        return;
      }
      if (attacker.getGameMode() != GameMode.CREATIVE) {
        e.setCancelled(true);
      }
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

  @SuppressWarnings("deprecation")
  @EventHandler
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

  @EventHandler
  public void effectEvent(EntityPotionEffectEvent e) {
    if (e.getEntity() instanceof Player player && e.getNewEffect() == null) {
      val effect = e.getOldEffect();
      if (effect.getType().equals(PotionEffectType.SLOWNESS) && effect.getAmplifier() == 138) {
        disableMoveLocation.remove(player.getUniqueId());
      }
    }
  }

  @SuppressWarnings("deprecation")
  @EventHandler
  public void move(PlayerMoveEvent e) {
    var p = e.getPlayer();
    for (val effect : p.getActivePotionEffects())
      if (effect.getType().equals(PotionEffectType.SLOWNESS) && effect.getAmplifier() == 138) {
        Location loc = disableMoveLocation.get(p.getUniqueId());
        if (loc == null) {
          if (!p.isOnGround()) {
            return;
          }
          loc = e.getTo();
          disableMoveLocation.put(p.getUniqueId(), loc);
        }
        Location to = e.getTo();
        loc.setYaw(to.getYaw());
        loc.setPitch(to.getPitch());
        e.setTo(loc);
        return;
      }
    val to = e.getTo().clone();
    for (val loc : List.of(to, to.clone().subtract(0, 0.1, 0))) {
      if (CustomBlock.isCustomBlock(loc)) {
        CustomBlock.getCustomBlock(loc).walk(e);
        return;
      }
    }
  }

  @EventHandler
  public void regain(EntityRegainHealthEvent e) {
    if (!(e.getEntity() instanceof Player p)) {
      return;
    }
    if (e.getRegainReason() == RegainReason.SATIATED
        && !ComMiniPlayer.getPlayer(p.getUniqueId()).isCanFoodRegain()) {
      e.setCancelled(true);
    }
  }

  @EventHandler
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
}
