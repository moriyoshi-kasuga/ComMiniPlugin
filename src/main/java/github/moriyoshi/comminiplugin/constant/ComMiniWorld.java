package github.moriyoshi.comminiplugin.constant;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public final class ComMiniWorld {

  private ComMiniWorld() {
  }

  @NotNull
  public static final World GAME = Objects.requireNonNull(Bukkit.getWorld("game"));

  @NotNull
  public static final Location LOBBY = new Location(GAME, -0.5, 1.0, -0.5);

  public static boolean isGame(World world) {
    return GAME.equals(world);
  }
}
