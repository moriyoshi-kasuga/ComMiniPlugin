package github.moriyoshi.comminiplugin.game.survivalsniper;

import de.tr7zw.changeme.nbtapi.NBT;
import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import github.moriyoshi.comminiplugin.util.Util;
import java.util.HashSet;
import java.util.UUID;
import lombok.val;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.type.Gate;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;

public class SSSniper extends CustomItem implements CooldownItem {

  private static final Component DEFAULT_NAME = Util.mm("<blue>スナイパー");
  private static final double BULLET_SIZE = 0.15;
  private static final int MAX_LENGTH = 100;
  private static final int DEFAULT_COOLDOWN_TICK = 40;

  public SSSniper() {
    this(
        new ItemBuilder(Material.SPYGLASS)
            .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
            .customItemFlag(CustomItemFlag.DISABLE_ITEM_SPAWN, true)
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_OTHER_INV, true)
            .name(DEFAULT_NAME)
            .customModelData(1)
            .build());
  }

  public SSSniper(final ItemStack item) {
    super(item);
  }

  @Override
  public void swapToOffHand(final PlayerSwapHandItemsEvent e) {
    e.setCancelled(true);
    val player = e.getPlayer();
    val eyeLoc = player.getEyeLocation();
    if (inCooldown()) {
      player.playSound(eyeLoc, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
      return;
    }
    val optBullet = SSBullet.getFirstBullet(player);
    if (optBullet.isEmpty()) {
      player.playSound(eyeLoc, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
      return;
    }
    val bullet = optBullet.get();
    bullet.use(player);
    setCooldown(DEFAULT_COOLDOWN_TICK);
    val world = eyeLoc.getWorld();
    val eyeVec = eyeLoc.toVector();

    double effectRange = -1;
    RayTraceResult result;
    val already = new HashSet<UUID>();
    while ((result =
            world.rayTrace(
                eyeLoc,
                eyeLoc.getDirection(),
                MAX_LENGTH,
                FluidCollisionMode.NEVER,
                true,
                BULLET_SIZE,
                entity -> {
                  if (entity.equals(player)) {
                    return false;
                  }
                  if (entity instanceof org.bukkit.entity.Minecart
                      || entity instanceof org.bukkit.entity.Boat) {
                    entity.remove();
                    return false;
                  }
                  if (entity instanceof ArmorStand) {
                    return false;
                  }
                  if (entity instanceof LivingEntity && !already.contains(entity.getUniqueId())) {
                    return true;
                  }
                  return false;
                },
                block -> {
                  if (block.getBlockData() instanceof Gate gate) {
                    return !gate.isOpen();
                  }
                  return true;
                }))
        != null) {
      if (result.getHitBlock() != null) {
        effectRange = Math.max(effectRange, eyeVec.distance(result.getHitPosition()));
        break;
      }
      LivingEntity entity = (LivingEntity) result.getHitEntity();
      if (entity == null) {
        break;
      }
      already.add(entity.getUniqueId());
      val hit = result.getHitPosition();
      val isHeadShot =
          BoundingBox.of(hit, hit)
              // HEAD SIZE
              .expand(0.41, 0.3, 0.41)
              .contains(entity.getEyeLocation().toVector());
      entity.damage(isHeadShot ? bullet.getHeadShot() : bullet.getDamage(), player);
      player.playSound(
          eyeLoc,
          isHeadShot ? Sound.ENTITY_EXPERIENCE_ORB_PICKUP : Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE,
          1,
          1);
    }
    val dir = eyeLoc.getDirection().multiply(0.5);
    for (int i = 1; i < (effectRange == -1 ? MAX_LENGTH : effectRange) * 2; i += 1) {
      eyeLoc.add(dir);
      world.spawnParticle(Particle.WAX_OFF, eyeLoc, 1, 0, 0, 0, 1, null, true);
    }
    new ItemBuilder(getItem()).type(Material.CLOCK);
  }

  @Override
  public void heldOfThis(final PlayerItemHeldEvent e) {
    final Player player = e.getPlayer();
    player.getWorld().playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 2, 1);
  }

  @Override
  public void heldItem(final Player player) {
    val bullet = SSBullet.getFirstBullet(player).map(SSBullet::getName).orElse(null);
    val item = getItem();
    val flag =
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
  }

  @Override
  public void endCountDown() {
    new ItemBuilder(getItem()).type(Material.SPYGLASS);
  }
}
