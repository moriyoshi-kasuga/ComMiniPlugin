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

public final class BukkitUtil {

  public static final Random random = new Random();

  public static List<Player> getSquarePlayers(BoundingBox square) {

    return Bukkit
        .getOnlinePlayers().stream()
        .map(p -> (Player) p)
        .filter(player -> square.contains(player.getLocation().toVector())).toList();
  }

  public static boolean randomTeleport(Entity entity, Location center, int radius) {
    return randomTeleport(entity, center, radius, 100);
  }

  public static boolean randomTeleport(Entity entity, Location center, int radius, int maxTry) {
    return randomTeleport(entity, center.getWorld(), center.getBlockX(), center.getBlockZ(), radius,
        maxTry);
  }

  public static boolean randomTeleport(Entity entity, World world, int bx, int bz, int radius) {
    return randomTeleport(entity, world, bx, bz, radius, 100);
  }

  public static boolean randomTeleport(Entity entity, World world, int bx, int bz, int radius,
      int maxTry) {
    for (int i = 0; i < maxTry; i++) {
      var x = random.nextInt(-radius, radius);
      var z = random.nextInt(-radius, radius);
      var block = world.getHighestBlockAt(bx + x, bz + z, HeightMap.WORLD_SURFACE);
      if (block.isSolid() && block.isCollidable()) {
        entity.teleportAsync(block.getLocation(), TeleportCause.PLUGIN, TeleportFlag.Relative.YAW,
            TeleportFlag.Relative.PITCH);
        return true;
      }
    }
    return false;
  }

  public static void disableMove(Player player, int tick) {
    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, tick, 138, true, false));
  }
}
