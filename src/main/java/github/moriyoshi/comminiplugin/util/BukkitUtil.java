package github.moriyoshi.comminiplugin.util;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.constant.MenuItem;
import github.moriyoshi.comminiplugin.system.GamePlayer;

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
    return randomTeleport(entity, center.getWorld(), center.getBlockX(), center.getBlockZ(), radius,
        maxTry);
  }

  public static boolean randomTeleport(Entity entity, World world, int bx, int bz, int radius) {
    return randomTeleport(entity, world, bx, bz, radius, 100);
  }

  public static boolean randomTeleport(Entity entity, World world, int bx, int bz, int radius,
      int maxTry) {
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
          HeightMap.WORLD_SURFACE);
      if (block.isSolid() && block.isCollidable()) {
        entity.teleport(block.getLocation().add(0.5, 1.0, 0.5)
            .setDirection(entity.getLocation().getDirection()));
        return true;
      }
    }
    return false;
  }

  public static void initializePlayer(Player p) {
    GamePlayer.getPlayer(p.getUniqueId()).initialize();
    p.getInventory().clear();
    p.getInventory().addItem(new MenuItem().getItem());
    p.setExperienceLevelAndProgress(0);
    p.teleport(ComMiniWorld.LOBBY);
    p.setGameMode(GameMode.SURVIVAL);
  }
}
