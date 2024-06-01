package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.game.battleroyale.items.WingItem;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.loot.Entry;
import github.moriyoshi.comminiplugin.system.loot.LootTable;
import github.moriyoshi.comminiplugin.system.loot.Pool;
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
    world.getWorldBorder().setCenter(lobby);
    world.getWorldBorder().setSize(max_radius_range);
  }

  public void startMove(double maxRadius, double minRadius, long time) {
    val random = new Random();
    val border = world.getWorldBorder();
    val moveX = Optional.of(random.nextDouble(minRadius, maxRadius)).map(i -> random.nextBoolean() ? i : -i)
        .orElse(0.0) / time / 20;
    val moveZ = Optional.of(random.nextDouble(minRadius, maxRadius)).map(i -> random.nextBoolean() ? i : -i)
        .orElse(0.0) / time / 20;

    new BukkitRunnable() {

      private final Location center = border.getCenter();
      private long rest = time * 20;

      @Override
      public void run() {
        if (!GameSystem.isIn()) {
          this.cancel();
          return;
        }
        if (--rest == 0) {
          startMove(maxRadius, minRadius, time);
          this.cancel();
          return;
        }
        border.setCenter(center.add(moveX, 0, moveZ));
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 1);
  }

  public void startContraction(Location center, double size, int time, Consumer<SIGNAL> task) {
    val border = world.getWorldBorder();
    border.setCenter(center);
    border.setSize(min_border_range, (long) ((border.getSize() - (double) min_border_range) / size * (double) time));
    new BukkitRunnable() {

      private int temp = time + 1;

      @Override
      public void run() {
        if (!GameSystem.isIn()) {
          this.cancel();
          return;
        }
        if (0 >= --temp) {
          border.setSize(border.getSize());
          task.accept(new SIGNAL.END());
          this.cancel();
          return;
        }
        if (min_border_range >= border.getSize()) {
          border.setSize(border.getSize());
          task.accept(new SIGNAL.MIN());
          this.cancel();
          return;
        }
        task.accept(new SIGNAL.NONE(temp));
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 20);
  }

  public void stop() {
    val border = world.getWorldBorder();
    border.reset();
    treasure.clearTreasures();
    treasure.clearPlayer();
    treasure.saveFile();
  }

  public LootTable getLevel1() {
    return new LootTable(new ArrayList<>() {
      {
        add(new Pool()
            .add(new Entry(() -> new WingItem().getItem())));
      }
    });
  }

  public LootTable getLevel2() {
    return new LootTable(new ArrayList<>() {
      {
        add(new Pool()
            .add(new Entry(() -> new WingItem().getItem())));
      }
    });
  }

  public LootTable getLevel3() {
    return new LootTable(new ArrayList<>() {
      {
        add(new Pool()
            .add(new Entry(() -> new WingItem().getItem())));
      }
    });
  }

  public LootTable getLevel4() {
    return new LootTable(new ArrayList<>() {
      {
        add(new Pool()
            .add(new Entry(() -> new WingItem().getItem())));
      }
    });
  }

  public LootTable getLevel5() {
    return new LootTable(new ArrayList<>() {
      {
        add(new Pool()
            .add(new Entry(() -> new WingItem().getItem())));
      }
    });
  }

  public sealed interface SIGNAL {
    public record END() implements SIGNAL {
    }

    public record MIN() implements SIGNAL {
    }

    public record NONE(int restTime) implements SIGNAL {
    }
  }
}
