package github.moriyoshi.comminiplugin.game.survivalsniper;

import de.tr7zw.changeme.nbtapi.NBT;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
public class Bullet extends CustomItem {

  @RequiredArgsConstructor
  public enum WARHEAD {
    COPPER(Material.COPPER_INGOT, 3, "<color:#FF5733>軽量な弾", 3, 5, 8,
        Sound.BLOCK_SHROOMLIGHT_BREAK),
    IRON(Material.IRON_INGOT, 1, "<white>シンプルな弾", 5, 8, 7,
        Sound.ENTITY_FIREWORK_ROCKET_BLAST),
    GOLD(Material.GOLD_INGOT, 2, "<yellow>重厚な弾", 8, 12, 6,
        Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST),
    EMERALD(Material.EMERALD, 4, "<green>高価な弾", 12, 15, 5, Sound.ENTITY_ENDER_EYE_DEATH),
    DIAMOND(Material.DIAMOND, 5, "<blue>強力な弾", 15, 18, 4, Sound.ENTITY_GENERIC_EXPLODE),
    AMETHYST(Material.AMETHYST_SHARD, 6, "<light_purple>鋭利な弾", 18, 40, 1,
        Sound.BLOCK_AMETHYST_CLUSTER_BREAK);

    public final Material material;
    public final int model;
    public final String name;
    public final int damage;
    public final int headShot;
    public final int successAmount;
    public final Sound sound;

  }

  @RequiredArgsConstructor
  public enum WARTAIL {
    WOOD(Material.OAK_BUTTON, (m) -> m.name().contains("BUTTON") && m.isFuel(), "木",
        "木材系のボタン", 0),
    STONE(Material.STONE_BUTTON, (m) -> m == Material.STONE_BUTTON, "石", "焼き石のボタン", 1),
    COAL_BLOCK(Material.COAL_BLOCK, (m) -> m == Material.COAL_BLOCK, "石炭", "石炭のブロック", 2);

    public final Material icon;
    public final Predicate<Material> predicate;
    public final String name;
    public final String description;
    public final int plusDamage;

  }

  @RequiredArgsConstructor
  public enum OptionalItem {
    // TODO: 火薬で弾の攻撃範囲を広くする
    GUNPOWDER(
        new ItemBuilder(Material.GUNPOWDER).name("<red>爆発的ダメージ!")
            .lore("<gray>追加するとダメージ" + GUNPOWDER_MULTIPLE + "倍")
            .amount(GUNPOWDER_AMOUNT).build(),
        (i) -> i.getType() == Material.GUNPOWDER && i.getAmount() >= GUNPOWDER_AMOUNT,
        (triple) -> Triple.of("<u>" + triple.getLeft(),
            (int) (triple.getMiddle() * GUNPOWDER_MULTIPLE),
            (int) (triple.getRight() * GUNPOWDER_MULTIPLE)),
        (item) -> item.setAmount(item.getAmount() - GUNPOWDER_AMOUNT));

    public final ItemStack material;
    public final Predicate<ItemStack> predicate;
    public final Function<Triple<String, Integer, Integer>, Triple<String, Integer, Integer>> wrapper;
    public final Consumer<ItemStack> finalize;
  }

  private static final String NAME = "name";

  private static final String DAMAGE = "damage";

  private static final String HEAD_SHOT = "head_shot";

  private static final String SOUND = "sound";

  private static final double GUNPOWDER_MULTIPLE = 1.2;
  private static final int GUNPOWDER_AMOUNT = 2;

  public static Optional<Bullet> getFirstBullet(Player p) {
    for (ItemStack item : p.getInventory().getContents()) {
      if (CustomItem.equalsItem(item, Bullet.class)) {
        return Optional.of(new Bullet(item));
      }
    }
    return Optional.empty();
  }

  private String name;

  private int damage;

  private int headShot;

  private Sound sound;

  public Bullet() {
    this(new ItemBuilder(Material.IRON_NUGGET).name("Example").build(), "Example", 5, 10,
        Sound.ENTITY_FIREWORK_ROCKET_BLAST);
  }

  public Bullet(ItemStack item) {
    super(item);
    NBT.modify(item, nbt -> {
      var tag = Objects.requireNonNull(nbt.getCompound(nbtKey));
      name = tag.getString(NAME);
      damage = tag.getInteger(DAMAGE);
      headShot = tag.getInteger(HEAD_SHOT);
      sound = Sound.valueOf(tag.getString(SOUND));
    });
  }

  public Bullet(ItemStack item, String name, int damage, int headShot, Sound sound) {
    super(item);
    NBT.modify(item, nbt -> {
      var tag = nbt.getOrCreateCompound(nbtKey);
      tag.setString(NAME, name);
      tag.setInteger(DAMAGE, damage);
      tag.setString(SOUND, sound.name());
      tag.setInteger(HEAD_SHOT, headShot);
    });
    this.name = name;
    this.damage = damage;
    this.headShot = headShot;
    this.sound = sound;
  }

  @Override
  public @NotNull String getIdentifier() {
    return "bullet";
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    e.setCancelled(false);
  }

  @Override
  public Optional<UUID> generatUUID() {
    return Optional.empty();
  }

  public void use(Player p) {
    new ItemBuilder(getItem()).amount(getItem().getAmount() - 1);
    p.getWorld().playSound(p.getLocation(), getSound(), 6, 1);
  }

}
