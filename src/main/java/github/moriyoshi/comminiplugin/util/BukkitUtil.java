package github.moriyoshi.comminiplugin.util;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public final class BukkitUtil {

  public static List<Player> getSquarePlayers(BoundingBox square) {

    return Bukkit
        .getOnlinePlayers().stream()
        .map(p -> (Player) p)
        .filter(player -> square.contains(player.getLocation().toVector())).toList();
  }
}
