package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class InvisibleCloakItem extends CustomItem implements CooldownItem {
  public InvisibleCloakItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<#C1C7C1>透明マント")
            .lore("<gray>エルフの職人が織り上げた透明なマント。", "<gray>装備すると10秒間姿を消し、足が速くなる。")
            .customModelData(10)
            .build());
  }

  public InvisibleCloakItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    if (inCooldown()) {
      return;
    }
    setCooldown(600);
    val player = e.getPlayer();
    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, true, false));
    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, true, false));
  }
}
