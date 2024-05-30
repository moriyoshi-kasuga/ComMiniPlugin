package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import lombok.Getter;
import lombok.val;

public class BRField {

  @Getter
  private final String name;

  private final World world;
  @Getter
  private final Location lobby;
  private final int max_radius_range;
  private final int min_border_range;

  @Getter
  private final TreasureLocation treasure;

  public BRField(String name, Location lobby, int max_radius_range, int min_border_range) {
    this.name = name;
    this.world = lobby.getWorld();
    this.lobby = lobby;
    this.max_radius_range = max_radius_range;
    this.min_border_range = min_border_range;
    this.treasure = new TreasureLocation(name);
  }

  public void initialize() {
    treasure.clearPlayer();
    world.getWorldBorder().setSize(max_radius_range);
  }

  public void start(Location center, double size, int time, Consumer<Boolean> task) {
    val border = world.getWorldBorder();
    border.setCenter(center);
    border.setSize(min_border_range, (long) ((border.getSize() - (double) min_border_range) / size * (double) time));

    new BukkitRunnable() {

      private double temp = border.getSize() - min_border_range;

      @Override
      public void run() {
        if (min_border_range >= border.getSize()) {
          task.accept(true);
          this.cancel();
          return;
        }
        if (temp >= border.getSize()) {
          task.accept(false);
          this.cancel();
          return;
        }
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 20, 20);
  }

  public void stop() {
    val border = world.getWorldBorder();
    border.reset();
    treasure.clearPlayer();
    treasure.saveFile();
  }

}
