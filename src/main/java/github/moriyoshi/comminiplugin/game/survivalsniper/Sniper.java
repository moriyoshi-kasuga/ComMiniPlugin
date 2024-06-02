package github.moriyoshi.comminiplugin.game.survivalsniper;

import de.tr7zw.changeme.nbtapi.NBT;
import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.Util;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Sniper extends CustomItem implements CooldownItem {

  private static final Component DEFAULT_NAME = Util.mm("<blue>スナイパー");
  private static final Vector EYE_SIZE = new Vector(0.3, 0.3, 0.3);
  private static final Vector BULLET_SIZE = new Vector(0.1, 0.1, 0.1);
  private static final int MAX_LENGTH = 1000;
  private static final int DEFAULT_COOLDOWN_TICK = 40;

  public Sniper() {
    this(new ItemBuilder(Material.SPYGLASS).name(DEFAULT_NAME).customModelData(1).build());
  }

  public Sniper(final ItemStack item) {
    super(item);
  }

  @Override
  public void swapToOffHand(final PlayerSwapHandItemsEvent e) {
    e.setCancelled(true);
    val p = e.getPlayer();
    val eyeLoc = p.getEyeLocation();
    if (inCooldown()) {
      p.playSound(eyeLoc, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
      return;
    }
    val optBullet = Bullet.getFirstBullet(p);
    if (optBullet.isEmpty()) {
      p.playSound(eyeLoc, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
      return;
    }
    val bullet = optBullet.get();
    bullet.use(p);
    setCooldown(DEFAULT_COOLDOWN_TICK);
    val world = eyeLoc.getWorld();
    val vec = eyeLoc.getDirection().normalize().multiply(0.1);
    val loc = eyeLoc.clone();
    val already = new HashMap<LivingEntity, Boolean>();
    for (int i = 1; i < MAX_LENGTH; i += 1) {
      loc.add(vec);
      val v = loc.toVector();
      val block = loc.getBlock();
      if (block.isCollidable() && block.getBoundingBox().contains(v)) {
        break;
      }
      if (i % 5 == 0) {
        world.spawnParticle(Particle.WAX_OFF, loc, 1, 0, 0, 0, 1, null, true);
      }
      loc.getNearbyEntities(1, 1, 1)
          .forEach(
              entity -> {
                if (entity != p
                    && entity
                        .getBoundingBox()
                        .overlaps(v.clone().add(BULLET_SIZE), v.clone().subtract(BULLET_SIZE))) {
                  if (!(entity instanceof final LivingEntity living)) {
                    if (entity instanceof org.bukkit.entity.Minecart
                        || entity instanceof org.bukkit.entity.Boat) {
                      entity.remove();
                    }
                    return;
                  }
                  if (already.getOrDefault(living, false)) {
                    return;
                  }
                  val eye = living.getEyeLocation().toVector();
                  val min = eye.clone().subtract(EYE_SIZE);
                  val max = eye.clone().add(EYE_SIZE);
                  val isHeadShot = BoundingBox.of(min, max).contains(v);
                  already.put(living, isHeadShot);
                }
              });
    }
    already.forEach(
        (entity, isHeadShot) -> {
          entity.damage(isHeadShot ? bullet.getHeadShot() : bullet.getDamage(), p);
          p.playSound(
              eyeLoc,
              isHeadShot
                  ? Sound.ENTITY_EXPERIENCE_ORB_PICKUP
                  : Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE,
              1,
              1);
        });
    p.getInventory().setItemInMainHand(new ItemBuilder(getItem()).type(Material.CLOCK).build());
  }

  @Override
  public void heldOfThis(final PlayerItemHeldEvent e) {
    super.heldOfThis(e);
    final Player player = e.getPlayer();
    player.getWorld().playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 2, 1);
  }

  @Override
  public void interact(final PlayerInteractEvent e) {
    e.setCancelled(false);
  }

  @Override
  public @NotNull Optional<Consumer<Player>> heldItem(final ItemStack item) {
    return Optional.of(
        player -> {
          final String bullet = Bullet.getFirstBullet(player).map(Bullet::getName).orElse(null);
          final boolean flag =
              NBT.modify(
                  item,
                  nbt -> {
                    val compound = nbt.getCompound(nbtKey);
                    if (compound.hasTag("nextBullet")) {
                      return !compound.getString("nextBullet").equals(bullet);
                    }
                    return true;
                  });
          if (flag) {
            new ItemBuilder(item)
                .name(
                    DEFAULT_NAME.append(
                        StringUtils.isEmpty(bullet)
                            ? Util.mm("<gray>: <red>None")
                            : Util.mm("<gray>: <white>" + bullet)));
            NBT.modify(
                item,
                nbt -> {
                  nbt.getCompound(nbtKey).setString("nextBullet", bullet);
                });
          }
        });
  }

  @Override
  public void runTick(final Player player) {
    if (!countDown()) {
      new ItemBuilder(getItem()).type(Material.SPYGLASS);
    }
  }

  @Override
  public void dropItem(final PlayerDropItemEvent e) {
    e.setCancelled(true);
  }

  @Override
  public boolean canMoveOtherInv(final InventoryClickEvent e) {
    return false;
  }

  @Override
  public void itemSpawn(final ItemSpawnEvent e) {
    e.getEntity().remove();
  }
}
