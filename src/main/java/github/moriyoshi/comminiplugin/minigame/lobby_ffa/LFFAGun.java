package github.moriyoshi.comminiplugin.minigame.lobby_ffa;

import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.HashSet;
import java.util.UUID;
import lombok.val;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.type.Gate;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;

public class LFFAGun extends CustomItem implements CooldownItem {
  private final int MAX_LENGTH = 40;
  private final double BULLET_SIZE = 0.1;

  public LFFAGun() {
    this(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
            .customItemFlag(CustomItemFlag.DISABLE_ITEM_SPAWN, true)
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_INV, true)
            .name("<red>ハンドガン")
            .customModelData(28)
            .build());
  }

  public LFFAGun(final ItemStack item) {
    super(item);
  }

  @Override
  public void damageEntity(EntityDamageByEntityEvent e, Player player) {
    spawn(player);
  }

  private void spawn(Player player) {
    val eyeLoc = player.getEyeLocation();
    if (inCooldown()) {
      player.playSound(eyeLoc, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
      return;
    }
    setCooldown(10);
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
      entity.damage(isHeadShot ? 8 : 5, player);
      player.playSound(
          eyeLoc,
          isHeadShot ? Sound.ENTITY_EXPERIENCE_ORB_PICKUP : Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE,
          1,
          1);
    }
    val dir = eyeLoc.getDirection().multiply(0.5);
    for (int i = 1; i < (effectRange == -1 ? MAX_LENGTH : effectRange) * 2; i += 1) {
      eyeLoc.add(dir);
      world.spawnParticle(Particle.ASH, eyeLoc, 1, 0, 0, 0, 1, null, true);
    }
    new ItemBuilder(getItem()).type(Material.CLOCK);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    if (e.getAction().isRightClick()) {
      return;
    }
    e.setCancelled(true);
    spawn(e.getPlayer());
  }

  @Override
  public void endCountDown() {
    new ItemBuilder(getItem()).type(Material.PHANTOM_MEMBRANE);
  }
}
