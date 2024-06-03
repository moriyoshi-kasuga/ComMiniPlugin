package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShockWaveItem extends CustomItem {
  public ShockWaveItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<#874B2A>ショックウェーブ")
            .lore("<gray>左クリックで半径15ブロック以内の相手すベてを遠くに（縦横５～７ブロック）吹き飛ばす", "<gray>右クリックは自分を前に移動させる")
            // WARN: apply correct custom model data
            .customModelData(17)
            .build());
  }

  public ShockWaveItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    val player = e.getPlayer();
    val loc = player.getLocation();
    itemUse();
    if (e.getAction().isLeftClick()) {
      loc.getNearbyPlayers(15, p -> !player.equals(p))
          .forEach(
              p -> {
                p.setVelocity(
                    loc.toVector().subtract(p.getLocation().toVector()).normalize().multiply(2));
              });
      return;
    } else {
      loc.setPitch(Math.min(Math.max(loc.getPitch(), -20), 20));
      player.setVelocity(loc.getDirection().multiply(2));
    }
  }

  @Override
  public boolean canStack() {
    return true;
  }
}
