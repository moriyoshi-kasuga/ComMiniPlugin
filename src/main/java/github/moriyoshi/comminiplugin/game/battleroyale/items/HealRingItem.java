package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class HealRingItem extends CustomItem {
  public HealRingItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<green>治癒の腕輪")
            .lore("<gray>使用すると、しばらく体力自動回復が付与される")
            .customModelData(25)
            .build());
  }

  public HealRingItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    useItemAmount();
    val player = e.getPlayer();
    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 0));
    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_TRIAL_SPAWNER_OPEN_SHUTTER, 1, 1);
  }
}
