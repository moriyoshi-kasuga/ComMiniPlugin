package github.moriyoshi.comminiplugin.util;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;

import io.papermc.paper.entity.TeleportFlag;
import lombok.val;

public final class BukkitUtil {

  public static final Random random = new Random();

  public static List<Player> getSquarePlayers(final BoundingBox square) {

    return Bukkit
        .getOnlinePlayers().stream()
        .map(p -> (Player) p)
        .filter(player -> square.contains(player.getLocation().toVector())).toList();
  }

  public static boolean randomTeleport(final Entity entity, final Location center, final int radius) {
    return randomTeleport(entity, center, radius, 100);
  }

  public static boolean randomTeleport(final Entity entity, final Location center, final int radius, final int maxTry) {
    return randomTeleport(entity, center.getWorld(), center.getBlockX(), center.getBlockZ(), radius,
        maxTry);
  }

  public static boolean randomTeleport(final Entity entity, final World world, final int bx, final int bz,
      final int radius) {
    return randomTeleport(entity, world, bx, bz, radius, 100);
  }

  public static boolean randomTeleport(final Entity entity, final World world, final int bx, final int bz,
      final int radius,
      final int maxTry) {
    for (int i = 0; i < maxTry; i++) {
      val x = random.nextInt(-radius, radius);
      val z = random.nextInt(-radius, radius);
      val block = world.getHighestBlockAt(bx + x, bz + z, HeightMap.WORLD_SURFACE);
      if (block.isSolid() && block.isCollidable()) {
        entity.teleportAsync(block.getLocation().add(0.5, 1, 0.5), TeleportCause.PLUGIN, TeleportFlag.Relative.YAW,
            TeleportFlag.Relative.PITCH);
        return true;
      }
    }
    return false;
  }

  public static void disableMove(final Player player, final int tick) {
    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, tick, 138, true, false));
  }
}
