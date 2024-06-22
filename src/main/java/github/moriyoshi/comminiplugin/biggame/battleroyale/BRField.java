package github.moriyoshi.comminiplugin.biggame.battleroyale;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.AllOrNothingItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.BackpackItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.BraveShieldItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.CowBallItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.CurryBreadItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.HKPRItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.HealRingItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.InvisibleCloakItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.MagicMirrorItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.NauseaBallItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.PhoenixFeatherItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.RecallClockItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.ScannerCompassItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.ShockWaveItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.SpeedBootsItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.StormBringerItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.TinglyBallItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.UpgradeWingItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.VampireBowItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.VampireSwordItem;
import github.moriyoshi.comminiplugin.biggame.battleroyale.items.WingItem;
import github.moriyoshi.comminiplugin.lib.RandomCollection;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.biggame.BigGameSystem;
import github.moriyoshi.comminiplugin.system.loot.Entry;
import github.moriyoshi.comminiplugin.system.loot.LootTable;
import github.moriyoshi.comminiplugin.system.loot.Pool;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

public class BRField {

  protected final RandomCollection<ItemStack> arrows;
  @Getter private final String name;
  private final World world;
  @Getter private final Location lobby;
  private final int max_radius_range;
  private final int min_border_range;
  @Getter private final TreasureLocation treasure;
  @Getter private final int border_first_before_move_time;
  @Getter private final int border_interval;
  @Getter private final int border_contraction_time;
  @Getter private final int border_contraction_size;
  @Getter private final int border_before_move_time;
  private final BoundingBox box;

  public BRField(
      String name,
      Location lobby,
      int max_radius_range,
      int min_border_range,
      int border_first_before_move_time,
      int border_interval,
      int border_contraction_time,
      int border_contraction_size,
      int border_before_move_time) {
    this.name = name;
    this.world = lobby.getWorld();
    this.lobby = lobby;
    this.max_radius_range = max_radius_range;
    this.min_border_range = min_border_range;
    this.border_first_before_move_time = border_first_before_move_time;
    this.border_interval = border_interval;
    this.border_contraction_time = border_contraction_time;
    this.border_contraction_size = border_contraction_size;
    this.border_before_move_time = border_before_move_time;

    this.treasure = new TreasureLocation(name);
    this.arrows =
        new RandomCollection<ItemStack>()
            .add(50, new ItemBuilder(Material.ARROW).amount(3).build())
            .add(30, new ItemBuilder(Material.ARROW).amount(5).build())
            .add(10, new ItemBuilder(Material.ARROW).amount(7).build())
            .add(7, new ItemBuilder(Material.ARROW).amount(10).build())
            .add(3, new ItemBuilder(Material.ARROW).amount(15).build());
    this.box = BoundingBox.of(lobby, max_radius_range / 2, 400, max_radius_range / 2);
  }

  public void initialize() {
    treasure.clearPlayer();
    world.getNearbyEntities(box, entity -> entity instanceof Item).forEach(Entity::remove);
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
        if (!BigGameSystem.isIn()) {
          this.cancel();
          return;
        }
        if (--rest == 0) {
          startMove(maxRadius, minRadius, time);
          this.cancel();
          return;
        }
        center.add(moveX, 0, moveZ);
        if (box.contains(center.toVector())) {
          border.setCenter(center);
        } else {
          startMove(maxRadius, minRadius, time);
          this.cancel();
        }
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 1);
  }

  /**
   * {@code size} を {@code time} 秒かけて収縮します
   *
   * @param size size
   * @param time time
   * @param task task
   */
  public void startContraction(double size, int time, Consumer<SIGNAL> task) {
    val border = world.getWorldBorder();
    val random = new Random();
    val center = border.getCenter();
    val x = random.nextDouble(-size, size) / time / 40;
    val z = random.nextDouble(-size, size) / time / 40;
    border.setSize(
        min_border_range, (long) ((border.getSize() - min_border_range) / (size / time)));
    new BukkitRunnable() {

      private int temp = time + 1;
      private int tick = 0;

      @Override
      public void run() {
        if (!BigGameSystem.isIn()) {
          this.cancel();
          return;
        }
        center.add(x, 0, z);
        border.setCenter(center);
        if (0 >= --tick) {
          tick = 21;
        } else {
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
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 0, 1);
  }

  public void stop() {
    val border = world.getWorldBorder();
    border.reset();
    world.getNearbyEntities(box, entity -> entity instanceof Item).forEach(Entity::remove);
    treasure.clearTreasures();
    treasure.clearPlayer();
    treasure.saveFile();
  }

  protected Pool getLevel1MainPool() {
    return new Pool(3, 0, 3);
  }

  public LootTable getLevel1() {
    return new LootTable(
        new ArrayList<>() {
          {
            add(
                getLevel1MainPool()
                    .add(new Entry(10, () -> new CurryBreadItem().getItem()))
                    .add(new Entry(10, () -> new HealRingItem().getItem()))
                    .add(new Entry(10, () -> new NauseaBallItem().getItem()).setItemRolls(1, 2))
                    .add(new Entry(10, () -> new TinglyBallItem().getItem()).setItemRolls(1, 2))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.FIREWORK_ROCKET)
                                    .changeMeta((Consumer<FireworkMeta>) meta -> meta.setPower(1))
                                    .build()))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.POTION)
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta -> meta.setBasePotionType(PotionType.LEAPING))
                                    .build()))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.POTION)
                                    .name("<aqua>!SPEED!")
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta -> {
                                              meta.addCustomEffect(
                                                  new PotionEffect(
                                                      PotionEffectType.SPEED, 30 * 20, 0),
                                                  true);
                                            })
                                    .build()))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.POTION)
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta -> meta.setBasePotionType(PotionType.NIGHT_VISION))
                                    .build()))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.SPLASH_POTION)
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta -> meta.setBasePotionType(PotionType.HEALING))
                                    .build()))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.SPLASH_POTION)
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta -> meta.setBasePotionType(PotionType.WATER))
                                    .build()))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.POTION)
                                    .name("<red>!Regeneration!")
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta -> {
                                              meta.addCustomEffect(
                                                  new PotionEffect(
                                                      PotionEffectType.REGENERATION, 30 * 20, 0),
                                                  true);
                                            })
                                    .build()))
                    .add(new Entry(10, () -> new ItemStack(Material.SPECTRAL_ARROW)))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.TIPPED_ARROW)
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta -> meta.setBasePotionType(PotionType.SLOWNESS))
                                    .build()))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.TIPPED_ARROW)
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta -> meta.setBasePotionType(PotionType.POISON))
                                    .build())));
            add(new Pool().add(new Entry(1, arrows::next)));
          }
        });
  }

  protected Pool getLevel2MainPool() {
    return new Pool(2, 0, 3);
  }

  public LootTable getLevel2() {
    return new LootTable(
        new ArrayList<>() {
          {
            add(
                getLevel2MainPool()
                    .add(new Entry(10, () -> new CurryBreadItem().getItem()))
                    .add(new Entry(10, () -> new ShockWaveItem().getItem()))
                    .add(new Entry(10, () -> new TinglyBallItem().getItem()).setItemRolls(1, 3))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.SPLASH_POTION)
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta ->
                                                meta.setBasePotionType(PotionType.STRONG_HEALING))
                                    .build()))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.SPLASH_POTION)
                                    .name("<red>!POWER!")
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta -> {
                                              meta.addCustomEffect(
                                                  new PotionEffect(
                                                      PotionEffectType.STRENGTH, 30 * 20, 0),
                                                  true);
                                            })
                                    .build()))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.FIREWORK_ROCKET)
                                    .changeMeta((Consumer<FireworkMeta>) meta -> meta.setPower(2))
                                    .build()))
                    .add(new Entry(10, () -> new BackpackItem().getItem()))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.TIPPED_ARROW)
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta ->
                                                meta.setBasePotionType(PotionType.LONG_WEAKNESS))
                                    .build()))
                    .add(new Entry(3, () -> new ItemStack(Material.LEATHER_CHESTPLATE)))
                    .add(new Entry(3, () -> new ItemStack(Material.LEATHER_LEGGINGS)))
                    .add(new Entry(5, () -> new SpeedBootsItem().getItem())));
            add(new Pool().add(new Entry(1, arrows::next)));
          }
        });
  }

  protected Pool getLevel3MainPool() {
    return new Pool(2, 0, 2);
  }

  public LootTable getLevel3() {
    return new LootTable(
        new ArrayList<>() {
          {
            add(
                getLevel3MainPool()
                    .add(new Entry(10, () -> new BackpackItem().getItem()))
                    .add(new Entry(10, () -> new ShockWaveItem().getItem()))
                    .add(new Entry(10, () -> new SpeedBootsItem().getItem()))
                    .add(new Entry(10, () -> new AllOrNothingItem().getItem()))
                    .add(new Entry(5, () -> new ItemStack(Material.LEATHER_CHESTPLATE)))
                    .add(new Entry(5, () -> new ItemStack(Material.LEATHER_LEGGINGS)))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.FIREWORK_ROCKET)
                                    .changeMeta((Consumer<FireworkMeta>) meta -> meta.setPower(3))
                                    .build()))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.SPLASH_POTION)
                                    .name("<green>!POISON!")
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta -> {
                                              meta.addCustomEffect(
                                                  new PotionEffect(
                                                      PotionEffectType.POISON, 10 * 20, 0),
                                                  true);
                                            })
                                    .build()))
                    .add(
                        new Entry(
                            10,
                            () ->
                                new ItemBuilder(Material.SPLASH_POTION)
                                    .name("<gray>!WEAKNESS!")
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta -> {
                                              meta.addCustomEffect(
                                                  new PotionEffect(
                                                      PotionEffectType.WEAKNESS, 10 * 20, 0),
                                                  true);
                                            })
                                    .build()))
                    .add(
                        new Entry(
                            5,
                            () ->
                                new ItemBuilder(Material.TIPPED_ARROW)
                                    .changeMeta(
                                        (Consumer<PotionMeta>)
                                            meta -> meta.setBasePotionType(PotionType.HEALING))
                                    .build()))
                    .add(new Entry(5, () -> new CowBallItem().getItem()).setItemRolls(1, 2))
                    .add(new Entry(5, () -> new BackpackItem().getItem()))
                    .add(new Entry(5, () -> new RecallClockItem().getItem()))
                    .add(new Entry(5, () -> new VampireBowItem().getItem())));
            add(new Pool().add(new Entry(1, arrows::next)));
          }
        });
  }

  protected Pool getLevel4MainPool() {
    return new Pool(1, 0, 2);
  }

  public LootTable getLevel4() {
    return new LootTable(
        new ArrayList<>() {
          {
            add(
                getLevel4MainPool()
                    .add(new Entry(10, () -> new VampireBowItem().getItem()))
                    .add(new Entry(10, () -> new RecallClockItem().getItem()))
                    .add(new Entry(10, () -> new CowBallItem().getItem()).setItemRolls(1, 2))
                    .add(new Entry(10, () -> new InvisibleCloakItem().getItem()))
                    .add(new Entry(10, () -> new HKPRItem().getItem()))
                    .add(new Entry(10, () -> new WingItem().getItem()))
                    .add(new Entry(10, () -> new ItemStack(Material.ENDER_PEARL)))
                    .add(new Entry(5, () -> new PhoenixFeatherItem().getItem()))
                    .add(new Entry(5, () -> new VampireSwordItem().getItem()))
                    .add(new Entry(5, () -> new UpgradeWingItem().getItem())));
          }
        });
  }

  protected Pool getLevel5MainPool() {
    return new Pool(1, 0, 1);
  }

  public LootTable getLevel5() {
    return new LootTable(
        new ArrayList<>() {
          {
            add(
                getLevel5MainPool()
                    .add(new Entry(5, () -> new ItemStack(Material.ENDER_PEARL)))
                    .add(new Entry(10, () -> new MagicMirrorItem().getItem()))
                    .add(new Entry(10, () -> new HKPRItem().getItem()))
                    .add(new Entry(10, () -> new ItemStack(Material.TOTEM_OF_UNDYING)))
                    .add(new Entry(10, () -> new ScannerCompassItem().getItem()))
                    .add(new Entry(10, () -> new PhoenixFeatherItem().getItem()))
                    .add(new Entry(10, () -> new StormBringerItem().getItem()))
                    .add(new Entry(10, () -> new ItemStack(Material.ENCHANTED_GOLDEN_APPLE)))
                    .add(new Entry(10, () -> new BraveShieldItem().getItem()))
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
