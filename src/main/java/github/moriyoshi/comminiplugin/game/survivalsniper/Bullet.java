package github.moriyoshi.comminiplugin.game.survivalsniper;

import java.util.Optional;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import de.tr7zw.changeme.nbtapi.NBT;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class Bullet extends CustomItem {
  private static final String NAME = "name";
  private static final String DAMAGE = "damage";
  private static final String HEAD_SHOT = "head_shot";
  private static final String SOUND = "sound";

  public Bullet() {
    this(new ItemBuilder(Material.IRON_NUGGET).name("Example").build(), "Example", 5, 10,
        Sound.ENTITY_FIREWORK_ROCKET_BLAST);
  }

  public Bullet(ItemStack item) {
    super(item);
    NBT.modify(item, nbt -> {
      var tag = nbt.getCompound(nbtKey);
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
  public boolean shouldGenerateUUID() {
    return false;
  }

  private String name;
  private int damage;
  private int headShot;
  private Sound sound;

  public String getName() {
    return name;
  }

  public int getDamage() {
    return damage;
  }

  public int getHeadShot() {
    return headShot;
  }

  public Sound getSound() {
    return sound;
  }

  @Override
  public @NotNull String getIdentifier() {
    return "bullet";
  }

  public void use(Player p) {
    new ItemBuilder(getItem()).amount(getItem().getAmount() - 1);
    p.getWorld().playSound(p.getLocation(), sound, 6, 1);
  }

  public static Optional<Bullet> getFirstBullet(Player p) {
    for (ItemStack item : p.getInventory().getContents()) {
      if (CustomItem.equalsItem(item, Bullet.class)) {
        return Optional.of(new Bullet(item));
      }
    }
    return Optional.empty();
  }

  public static enum WARHEAD {
    COPPER(Material.COPPER_INGOT, 3, "軽量な弾", 3, 5, 8, Sound.BLOCK_SHROOMLIGHT_BREAK),
    IRON(Material.IRON_INGOT, 1, "シンプルな弾", 5, 8, 7, Sound.ENTITY_FIREWORK_ROCKET_BLAST),
    GOLD(Material.GOLD_INGOT, 2, "重厚な弾", 8, 12, 6, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST),
    EMERALD(Material.EMERALD, 4, "高価な弾", 12, 16, 5, Sound.ENTITY_ENDER_EYE_DEATH),
    DIAMOND(Material.DIAMOND, 5, "強力な弾", 16, 20, 4, Sound.ENTITY_GENERIC_EXPLODE),
    AMETHYST(Material.AMETHYST_SHARD, 6, "鋭利な弾", 20, 40, 3, Sound.BLOCK_AMETHYST_CLUSTER_BREAK);

    public final Material material;
    public final int model;
    public final String name;
    public final int damage;
    public final int headShot;
    public final int successAmount;
    public final Sound sound;

    WARHEAD(Material material, int model, String name, int damage, int headShot, int successAmount, Sound sound) {
      this.material = material;
      this.model = model;
      this.name = name;
      this.damage = damage;
      this.headShot = headShot;
      this.successAmount = successAmount;
      this.sound = sound;
    }
  }

  public static enum WARTAIL {
    WOOD(Material.OAK_BUTTON, (m) -> m.name().contains("BUTTON") && m.isFuel(), "木", "木材系のボタン", 1),
    STONE(Material.STONE_BUTTON, (m) -> m == Material.STONE_BUTTON, "石", "焼き石のボタン", 2),
    COAL_BLOCK(Material.COAL_BLOCK, (m) -> m == Material.COAL_BLOCK, "石炭", "石炭のブロック", 3);

    public final Material icon;
    public final Predicate<Material> predicate;
    public final String name;
    public final String description;
    public final int plusDamage;

    WARTAIL(Material icon, Predicate<Material> predicate, String name, String description, int plusDamage) {
      this.icon = icon;
      this.predicate = predicate;
      this.name = name;
      this.description = description;
      this.plusDamage = plusDamage;
    }
  }
}
