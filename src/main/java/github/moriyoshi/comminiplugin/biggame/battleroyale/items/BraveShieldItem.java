package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.item.CooldownItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BraveShieldItem extends CustomItem implements CooldownItem {
  public BraveShieldItem() {
    super(
        new ItemBuilder(Material.SHIELD)
            .name("<gray>名もなき勇者の盾")
            .lore("<gray>勇者が使っていた盾(古くなっている)", "<gray>最大20ダメージまで耐える")
            .customModelData(1)
            .build());
    setCooldown(200);
  }

  public BraveShieldItem(@NotNull ItemStack item) {
    super(item);
  }

  private void spanw(EntityDamageByEntityEvent e, Player player) {
    if (!inCooldown()) {
      setCooldown(200);
      return;
    }
    if (!player.isBlocking()) {
      return;
    }
    val rest = (double) getCooldown() / 10.0 - e.getDamage();
    if (0 >= rest) {
      useItemAmount();
      player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 2, 1);
      return;
    }
    e.setDamage(0);
    player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 2, 1);
    setCooldown((int) rest * 10);
  }

  @Override
  public void damageByEntityMainHand(EntityDamageByEntityEvent e, Player player) {
    spanw(e, player);
  }

  @Override
  public void damageByEntityOffHand(EntityDamageByEntityEvent e, Player player) {
    spanw(e, player);
  }

  @Override
  public @NotNull String getHasCooldownMessage(int cooldown) {
    return "<red>残り<u>" + (double) cooldown / 10.0 + "</u> ダメージ耐えられます";
  }

  @Override
  public boolean shouldAutoReduceCountDown() {
    return false;
  }
}
