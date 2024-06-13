package github.moriyoshi.comminiplugin.util;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.object.MenuItem;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import github.moriyoshi.comminiplugin.system.GameListener;
import io.papermc.paper.entity.TeleportFlag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public final class BukkitUtil {

  public static final BlockFace[] axis = {
    BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
  };

  private static final Map<UUID, Entity> fallingBlocks = new HashMap<>();

  public static List<Player> getSquarePlayers(final BoundingBox square) {

    return Bukkit.getOnlinePlayers().stream()
        .map(p -> (Player) p)
        .filter(player -> square.contains(player.getLocation().toVector()))
        .toList();
  }

  public static boolean randomTeleport(
      final Entity entity, final Location center, final int radius) {
    return randomTeleport(entity, center, radius, 100);
  }

  public static boolean randomTeleport(
      final Entity entity, final Location center, final int radius, final int maxTry) {
    return randomTeleport(
        entity, center.getWorld(), center.getBlockX(), center.getBlockZ(), radius, maxTry);
  }

  public static boolean randomTeleport(
      final Entity entity, final World world, final int bx, final int bz, final int radius) {
    return randomTeleport(entity, world, bx, bz, radius, 100);
  }

  public static boolean randomTeleport(
      final Entity entity,
      final World world,
      final int bx,
      final int bz,
      final int radius,
      final int maxTry) {
    for (int i = 0; i < maxTry; i++) {
      val random = new Random();
      val x = random.nextInt(-radius, radius);
      val z = random.nextInt(-radius, radius);
      val block = world.getHighestBlockAt(bx + x, bz + z, HeightMap.WORLD_SURFACE);
      if (block.isSolid() && block.isCollidable()) {
        entity.teleport(
            block.getLocation().add(0.5, 1, 0.5),
            TeleportCause.PLUGIN,
            TeleportFlag.Relative.YAW,
            TeleportFlag.Relative.PITCH);
        return true;
      }
    }
    return false;
  }

  public static void disableMove(final Player player, final int tick) {
    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, tick, 138, true, false));
  }

  public static BlockFace convertYawToBlockFace(final float yaw) {
    return axis[Math.round(yaw / 90f) & 0x3];
  }

  public static float convertBlockFaceToYaw(final BlockFace face) {
    val first = face.name().split("_")[0];
    return switch (first) {
      case "NORTH" -> 180;
      case "WEST" -> 90;
      case "EAST" -> -90;
      default -> 0;
    };
  }

  public static void clear() {
    fallingBlocks
        .values()
        .forEach(
            falling -> {
              if (!falling.isDead()) {
                falling.remove();
              }
            });
    fallingBlocks.clear();
  }

  public static void removeFalling(final UUID uuid) {
    val temp = fallingBlocks.get(uuid);
    if (temp != null) {
      temp.remove();
    }
  }

  public static boolean isFalling(final UUID uuid) {
    return fallingBlocks.containsKey(uuid);
  }

  public static void setVelocity(
      final Player player, final Vector velocity, final JumpState state) {
    val uuid = player.getUniqueId();
    val loc = player.getLocation();
    val falling =
        loc.getWorld()
            .spawn(
                loc,
                Snowball.class,
                entity -> {
                  entity.setInvisible(true);
                  entity.setInvulnerable(true);
                  entity.setSilent(true);
                  entity.setGravity(true);
                  entity.setVelocity(velocity);
                  entity.setVisibleByDefault(false);
                });
    GameListener.addProjectileHitListener(
        falling.getUniqueId(),
        (projectile, event) -> {
          event.setCancelled(true);
        });

    val temp = fallingBlocks.put(player.getUniqueId(), falling);
    if (temp != null) {
      temp.remove();
    }
    switch (state) {
      case FREE ->
          new BukkitRunnable() {

            private int rest = 3;

            @Override
            public void run() {
              if (falling.isDead() || 0 >= --rest) {
                falling.remove();
                fallingBlocks.remove(uuid);
                this.cancel();
                return;
              }
              player.setVelocity(falling.getVelocity());
            }
          }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 1);
      case DOWN ->
          new BukkitRunnable() {

            @Override
            public void run() {
              if (falling.isDead() || 0 >= falling.getVelocity().getY()) {
                falling.remove();
                fallingBlocks.remove(uuid);
                this.cancel();
                return;
              }
              player.setVelocity(falling.getVelocity());
            }
          }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 1);
      case FIXED ->
          new BukkitRunnable() {

            @Override
            public void run() {
              if (falling.isDead()) {
                falling.remove();
                fallingBlocks.remove(uuid);
                this.cancel();
                return;
              }
              player.setVelocity(falling.getVelocity());
            }
          }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 1);
    }
  }

  /**
   * サーバー参加時やロビーに返す時、ゲーム終了時に使えるメゾット
   *
   * @param p target player
   */
  public static void initializePlayer(Player p) {
    initializeGamePlayer(p);
    p.getInventory().addItem(new MenuItem().getItem());
    p.teleport(ComMiniWorld.LOBBY);
    p.playerListName(null);
  }

  public static void initializeGamePlayer(Player p) {
    p.getInventory().clear();
    p.setGameMode(GameMode.SURVIVAL);
    p.clearActivePotionEffects();
    p.setHealth(20);
    p.setExperienceLevelAndProgress(0);
    ComMiniPlayer.getPlayer(p.getUniqueId()).initialize();
  }
}
