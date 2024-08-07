package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.item.CooldownItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.util.KeyUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class VampireSwordItem extends CustomItem
    implements CooldownItem, CustomItem.DamageToEntity {
  public VampireSwordItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<red>吸血剣")
            .lore("<gray>攻撃時に最大ハート3個分HPを吸収する")
            .addAttribute(
                Attribute.GENERIC_ATTACK_DAMAGE,
                new AttributeModifier(KeyUtil.createUUIDKey(), 6, Operation.ADD_NUMBER))
            .addAttribute(
                Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(KeyUtil.createUUIDKey(), -3.0, Operation.ADD_NUMBER))
            .flags(ItemFlag.HIDE_ATTRIBUTES)
            .customModelData(23)
            .build());
  }

  public VampireSwordItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void damageToEntity(EntityDamageByEntityEvent e, Player player) {
    if (inCooldown()) {
      return;
    }
    player.heal(Math.min(e.getFinalDamage(), 6));
    setCooldown(5 * 20);
    var loc = player.getLocation();
    loc.getWorld().playSound(loc, Sound.ITEM_WOLF_ARMOR_BREAK, SoundCategory.MASTER, 1, 1);
    loc.getWorld()
        .spawnParticle(
            Particle.DUST,
            loc,
            10,
            0.5,
            0.5,
            1,
            1,
            new Particle.DustOptions(Color.fromRGB(0xDB1616), 4),
            true);
  }
}
