package github.moriyoshi.comminiplugin.game.battleroyale;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.game.battleroyale.items.AllOrNothingItem;
import github.moriyoshi.comminiplugin.game.battleroyale.items.BackpackItem;
import github.moriyoshi.comminiplugin.game.battleroyale.items.CurryBreadItem;
import github.moriyoshi.comminiplugin.game.battleroyale.items.HKPRItem;
import github.moriyoshi.comminiplugin.game.battleroyale.items.MagicMirrorItem;
import github.moriyoshi.comminiplugin.game.battleroyale.items.RecallClockItem;
import github.moriyoshi.comminiplugin.game.battleroyale.items.ScannerCompassItem;
import github.moriyoshi.comminiplugin.game.battleroyale.items.TinglyBallItem;
import github.moriyoshi.comminiplugin.game.battleroyale.items.UpgradeWingItem;
import github.moriyoshi.comminiplugin.game.battleroyale.items.VampireBowItem;
import github.moriyoshi.comminiplugin.game.battleroyale.items.VampireSwordItem;
import github.moriyoshi.comminiplugin.game.battleroyale.items.WingItem;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.loot.Entry;
import github.moriyoshi.comminiplugin.system.loot.LootTable;
import github.moriyoshi.comminiplugin.system.loot.Pool;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.RandomCollection;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class BRField {

  protected final RandomCollection<ItemStack> arrows;
  @Getter private final String name;
  private final World world;
  @Getter private final Location lobby;
  private final int max_radius_range;
  private final int min_border_range;
  @Getter private final TreasureLocation treasure;

  public BRField(String name, Location lobby, int max_radius_range, int min_border_range) {
    this.name = name;
    this.world = lobby.getWorld();
    this.lobby = lobby;
    this.max_radius_range = max_radius_range;
    this.min_border_range = min_border_range;
    this.treasure = new TreasureLocation(name);
    this.arrows =
        new RandomCollection<ItemStack>()
            .add(100, null)
            .add(50, new ItemBuilder(Material.ARROW).amount(3).build())
            .add(30, new ItemBuilder(Material.ARROW).amount(5).build())
            .add(10, new ItemBuilder(Material.ARROW).amount(7).build())
            .add(7, new ItemBuilder(Material.ARROW).amount(10).build())
            .add(3, new ItemBuilder(Material.ARROW).amount(15).build());
  }

  public void initialize() {
    treasure.clearPlayer();
    val border = world.getWorldBorder();
    border.setCenter(lobby);
    border.setSize(max_radius_range);
    border.setDamageBuffer(0);
  }

  public void startMove(double maxRadius, double minRadius, long time) {
    val random = new Random();
    val border = world.getWorldBorder();
    val moveX =
        Optional.of(random.nextDouble(minRadius, maxRadius))
                .map(i -> random.nextBoolean() ? i : -i)
                .orElse(0.0)
            / time
            / 20;

    val moveZ =
        Optional.of(random.nextDouble(minRadius, maxRadius))
                .map(i -> random.nextBoolean() ? i : -i)
                .orElse(0.0)
            / time
            / 20;

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
    border.setSize(
        min_border_range,
        (long) ((border.getSize() - (double) min_border_range) / size * (double) time));
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
    return new LootTable(
        new ArrayList<>() {
          {
            add(
                new Pool()
                    .add(new Entry(10, () -> new CurryBreadItem().getItem()))
                    .add(new Entry(5, () -> new TinglyBallItem().getItem()))
                    .add(new Entry(5, () -> new WingItem().getItem())));
            add(new Pool().add(new Entry(arrows::next)));
          }
        });
  }

  public LootTable getLevel2() {
    return new LootTable(
        new ArrayList<>() {
          {
            add(
                new Pool()
                    .add(new Entry(10, () -> new WingItem().getItem()))
                    .add(new Entry(10, () -> new TinglyBallItem().getItem()))
                    .add(new Entry(5, () -> new BackpackItem().getItem())));
            add(new Pool().add(new Entry(arrows::next)));
          }
        });
  }

  public LootTable getLevel3() {
    return new LootTable(
        new ArrayList<>() {
          {
            add(
                new Pool()
                    .add(new Entry(10, () -> new WingItem().getItem()))
                    .add(new Entry(10, () -> new BackpackItem().getItem()))
                    .add(new Entry(5, () -> new VampireBowItem().getItem())));
            add(new Pool().add(new Entry(arrows::next)));
          }
        });
  }

  public LootTable getLevel4() {
    return new LootTable(
        new ArrayList<>() {
          {
            add(
                new Pool()
                    .add(new Entry(10, () -> new ItemStack(Material.ENDER_PEARL)))
                    .add(new Entry(10, () -> new VampireBowItem().getItem()))
                    .add(new Entry(5, () -> new VampireSwordItem().getItem()))
                    .add(new Entry(5, () -> new UpgradeWingItem().getItem())));
          }
        });
  }

  public LootTable getLevel5() {
    return new LootTable(
        new ArrayList<>() {
          {
            add(
                new Pool()
                    .add(new Entry(5, () -> new ItemStack(Material.ENDER_PEARL)))
                    .add(new Entry(10, () -> new AllOrNothingItem().getItem()))
                    .add(new Entry(10, () -> new RecallClockItem().getItem()))
                    .add(new Entry(10, () -> new MagicMirrorItem().getItem()))
                    .add(new Entry(10, () -> new HKPRItem().getItem()))
                    .add(new Entry(10, () -> new ScannerCompassItem().getItem()))
                    .add(new Entry(10, () -> new VampireSwordItem().getItem()))
                    .add(new Entry(10, () -> new UpgradeWingItem().getItem())));
          }
        });
  }

  public sealed interface SIGNAL {

    record END() implements SIGNAL {}

    record MIN() implements SIGNAL {}

    record NONE(int restTime) implements SIGNAL {}
  }
}
