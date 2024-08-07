package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.item.CooldownItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class InvisibleCloakItem extends CustomItem
    implements CooldownItem, CustomItem.InteractMainHand {
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
  public void interactMainHand(PlayerInteractEvent e, final Player player) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    if (inCooldown()) {
      return;
    }
    setCooldown(600);
    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, true, false));
    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, true, false));
  }
}
