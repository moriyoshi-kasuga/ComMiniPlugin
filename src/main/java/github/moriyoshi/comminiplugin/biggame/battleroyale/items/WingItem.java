package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import github.moriyoshi.comminiplugin.lib.JumpState;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItemFlag;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class WingItem extends CustomItem implements CustomItem.InteractMainHand {
  public WingItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<yellow>これを使えば近くまで")
            .lore("<gray>使うと垂直にジャンプしてそこからエリトラで滑空できます", "<gray>地面につくとエリトラは消えます")
            .customModelData(7)
            .build());
  }

  public WingItem(@NotNull ItemStack item) {
    super(item);
  }

  public static void setWing(Player player) {
    val equiments = player.getEquipment();
    val temp = equiments.getItem(EquipmentSlot.CHEST);
    val wing =
        new ItemBuilder(Material.ELYTRA)
            .name("<yellow>Wing")
            .customItemFlag(CustomItemFlag.DISABLE_MOVE_INV, true)
            .customItemFlag(CustomItemFlag.DISABLE_DROP, true)
            .build();
    val flag = !wing.isSimilar(temp);
    equiments.setItem(EquipmentSlot.CHEST, wing, false);
    new BukkitRunnable() {

      @SuppressWarnings("deprecation")
      @Override
      public void run() {
        if (!player.isOnGround()) {
          return;
        }
        if (flag) {
          equiments.setItem(EquipmentSlot.CHEST, temp);
        }
        this.cancel();
      }
    }.runTaskTimer(ComMiniPlugin.getPlugin(), 5, 1);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e, final Player player) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    useItemAmount();
    BukkitUtil.setVelocity(player, player.getVelocity().setY(2), JumpState.FREE);
    setWing(player);
  }

  @Override
  public boolean canStack() {
    return true;
  }
}
