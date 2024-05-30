package github.moriyoshi.comminiplugin.game.battleroyale.items;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;

public class WingItem extends CustomItem {
  public WingItem() {
    super(new ItemBuilder(Material.PHANTOM_MEMBRANE).name("<yellow>これを使えばどこまでも")
        .lore("<gray>使うと垂直にジャンプしてそこからエリトラで滑空することができます", "<gray>地面につくとエリトラは消えます").customModelData(7).build());
  }

  public WingItem(@NotNull ItemStack item) {
    super(item);
  }

  public static void setWing(Player player) {
    player.getEquipment().setItem(EquipmentSlot.CHEST,
        new ItemBuilder(Material.ELYTRA).name("<yellow>Wing").customItemFlag(CustomItemFlag.MOVE_INV, false).build(),
        false);
    new BukkitRunnable() {

      @SuppressWarnings("deprecation")
      @Override
      public void run() {
        if (!player.isOnGround()) {
          return;
        }
        player.getEquipment().setItem(EquipmentSlot.CHEST, null);
        this.cancel();
      }

    }.runTaskTimer(ComMiniPlugin.getPlugin(), 10, 1);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    itemUse();
    val player = e.getPlayer();
    player.setVelocity(player.getVelocity().setY(2));
    setWing(player);
  }

  @Override
  public Optional<UUID> generateUUID() {
    return Optional.empty();
  }

}
