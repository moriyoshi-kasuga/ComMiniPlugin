package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.ArrayList;
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
import github.moriyoshi.comminiplugin.util.Util;
import lombok.Getter;
import lombok.val;
import net.kyori.adventure.bossbar.BossBar;

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

  public void startMove(BossBar bossBar) {
    // TODO: ここにサイズは変えずにボーダーを動かしまくる
  }

  public void startContraction(BossBar bossBar, Location center, double size, int time, Consumer<SIGNAL> task) {
    val border = world.getWorldBorder();
    border.setCenter(center);
    border.setSize(min_border_range, (long) ((border.getSize() - (double) min_border_range) / size * (double) time));

    new BukkitRunnable() {

      private int temp = time;

      @Override
      public void run() {
        if (!GameSystem.isIn()) {
          this.cancel();
          return;
        }
        if (0 >= --temp) {
          bossBar.name(Util.mm("<aqua>ボーダー停止中")).progress(0f);
          border.setSize(border.getSize());
          task.accept(SIGNAL.END);
          this.cancel();
          return;
        }
        if (min_border_range >= border.getSize()) {
          bossBar.name(Util.mm("<yellow>最小サイズになりました")).progress(0f);
          border.setSize(border.getSize());
          task.accept(SIGNAL.MIN);
          this.cancel();
          return;
        }
        bossBar.name(Util.mm("<red>ボーダー収縮残り: <u>" + time + "</u>秒")).progress((float) temp / (float) time);
        task.accept(SIGNAL.NONE);
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 20, 20);
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

  public enum SIGNAL {
    END,
    MIN,
    NONE
  }
}
