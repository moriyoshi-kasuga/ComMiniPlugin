package github.moriyoshi.comminiplugin.game.survivalsniper;

import lombok.val;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import de.tr7zw.changeme.nbtapi.NBT;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

  private static final String NAME = "name";

  private static final String DAMAGE = "damage";

  private static final String HEAD_SHOT = "head_shot";

  private static final String SOUND = "sound";

  public static Optional<Bullet> getFirstBullet(final Player p) {
    for (final ItemStack item : p.getInventory().getContents()) {
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

  public Bullet(final ItemStack item) {
    super(item);
    NBT.modify(item, nbt -> {
      val tag = Objects.requireNonNull(nbt.getCompound(nbtKey));
      name = tag.getString(NAME);
      damage = tag.getInteger(DAMAGE);
      headShot = tag.getInteger(HEAD_SHOT);
      sound = Sound.valueOf(tag.getString(SOUND));
    });
  }

  public Bullet(final ItemStack item, final String name, final int damage, final int headShot, final Sound sound) {
    super(item);
    NBT.modify(item, nbt -> {
      val tag = nbt.getOrCreateCompound(nbtKey);
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
  public void interact(final PlayerInteractEvent e) {
    e.setCancelled(false);
  }

  @Override
  public Optional<UUID> generateUUID() {
    return Optional.empty();
  }

  public void use(final Player p) {
    new ItemBuilder(getItem()).amount(getItem().getAmount() - 1);
    p.getWorld().playSound(p.getLocation(), getSound(), 6, 1);
  }

}
