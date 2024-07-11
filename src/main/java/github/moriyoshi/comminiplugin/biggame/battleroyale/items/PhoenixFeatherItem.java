package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.lib.item.PlayerCooldownItem;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PhoenixFeatherItem extends CustomItem
    implements PlayerCooldownItem, CustomItem.InteractMainHand {

  public PhoenixFeatherItem() {
    this(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<red>フェニックスの羽")
            .lore("<gray>伝説の鳥フェニックスの羽根。", "<gray>持つ者に驚異的な回復力を与える。", "<gray>一度だけ即座に体力を全回復する。")
            .customModelData(9)
            .build());
  }

  public PhoenixFeatherItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e, final Player player) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    if (inCooldown(player.getUniqueId())) {
      return;
    }
    useItemAmount();
    setCooldown(90 * 20, player.getUniqueId());
    player.heal(300);
    val loc = e.getPlayer().getLocation();
    val world = loc.getWorld();
    world.playSound(loc, Sound.ENTITY_VILLAGER_WORK_CLERIC, 3, 1);
    world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 3, 1);
  }

  @Override
  public boolean canStack() {
    return true;
  }
}
