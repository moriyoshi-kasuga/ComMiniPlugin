package github.moriyoshi.comminiplugin.util;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public final class BukkitUtil {

  public static List<Player> getSquarePlayers(BoundingBox square) {

    return Bukkit
        .getOnlinePlayers().stream()
        .map(p -> (Player) p)
        .filter(player -> square.contains(player.getLocation().toVector())).toList();
  }

  public static final Random random = new Random();

  public static boolean randomTeleport(Entity entity, Location center, int radius) {
    return randomTeleport(entity, center, radius, 100);
  }

  public static boolean randomTeleport(Entity entity, Location center, int radius, int maxTry) {
    var world = entity.getWorld();
    var bx = center.getBlockX();
    var bz = center.getBlockZ();
    var alerdy = new HashSet<Pair<Integer, Integer>>();
    for (int i = 0; i < maxTry; i++) {
      var x = random.nextInt(-radius, radius);
      var z = random.nextInt(-radius, radius);
      var pair = Pair.of(x, z);
      if (alerdy.contains(pair)) {
        continue;
      }
      alerdy.add(pair);
      var block = world.getHighestBlockAt(bx + x, bz + z,
          HeightMap.MOTION_BLOCKING);
      if (block.isCollidable()) {
        entity.teleportAsync(block.getLocation().add(x > 0 ? 0.5 : -0.5, 1.0, z > 0 ? 0.5 : -0.5));
        return true;
      }
    }
    return false;
  }
}
