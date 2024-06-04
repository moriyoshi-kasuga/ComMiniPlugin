package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.UUID;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpeedBootsItem extends CustomItem implements CooldownItem {
  public SpeedBootsItem() {
    super(
        new ItemBuilder(Material.LEATHER_BOOTS)
            .name("<aqua>スピードブーツ")
            .lore("<gray>履くことで移動速度が一時的に上がる魔法のブーツ。", "<gray>シフト1秒間で10秒間さらにスピードアップ (cooldown: 60秒)")
            .addAttribute(
                Attribute.GENERIC_MOVEMENT_SPEED,
                new AttributeModifier(
                    UUID.randomUUID(),
                    "speed boots",
                    0.01,
                    Operation.ADD_NUMBER,
                    EquipmentSlotGroup.ARMOR))
            .flags(ItemFlag.HIDE_ATTRIBUTES)
            .customModelData(10)
            .build());
  }

  public SpeedBootsItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    if (inCooldown()) {
      return;
    }
  }

  @Override
  public void shiftItem(PlayerToggleSneakEvent e, @Nullable EquipmentSlot equipmentSlot) {
    if (!e.isSneaking()) {
      return;
    }
    if (inCooldown()) {
      return;
    }
    if (equipmentSlot == null) {
      return;
    }
    if (!equipmentSlot.isArmor()) {
      return;
    }
    val p = e.getPlayer();
    new BukkitRunnable() {

      private int rest = 20;

      @Override
      public void run() {
        if (!p.isSneaking()) {
          this.cancel();
          return;
        }
        if (--rest > 0) {
          if (rest % 5 == 0) {
            p.getWorld()
                .playSound(
                    p.getLocation(),
                    Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                    SoundCategory.MASTER,
                    1,
                    1);
          }
          return;
        }
        val world = p.getWorld();
        val loc = p.getLocation();
        world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.MASTER, 3, 1);
        world.spawnParticle(Particle.EXPLOSION, loc, 10, 1.5, 1.5, 1.5, 1, null, true);
        setCooldown(1200);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0, true, false));
        this.cancel();
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 1, 1);
  }

  @Override
  public void runTick(Player player) {
    if (inCooldown()) {
      if (countDown()) {
        new ItemBuilder(getItem())
            .name("<aqua>スピードブーツ: cooldown <u>" + getCooldown() / 20 + "</u>秒");
      } else {
        new ItemBuilder(getItem()).name("<aqua>スピードブーツ");
      }
    }
  }

  @Override
  public boolean shouldAutoReduceCountDown() {
    return false;
  }
}
