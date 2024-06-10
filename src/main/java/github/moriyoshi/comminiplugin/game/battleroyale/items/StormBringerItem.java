package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.item.CooldownItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StormBringerItem extends CustomItem implements CooldownItem {
  public StormBringerItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<yellow>ストームブリンガー")
            .lore(
                "<gray>雷を呼び寄せる力を宿した古代のハンマー。",
                "<gray>持つ者に雷神の力を与え、敵を破壊する。",
                "<gray>視線の先のブロックの半径10ブロック以内のプレイヤーに雷を降らす。")
            .customModelData(11)
            .build());
  }

  public StormBringerItem(@NotNull ItemStack item) {
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
    val player = e.getPlayer();
    val target = player.getTargetBlockExact(50);
    if (target == null || target.isEmpty()) {
      ComMiniPrefix.MAIN.send(player, "<red>対象のブロックを見つけられません。");
      return;
    }
    setCooldown(1200);
    val world = player.getWorld();
    world.strikeLightningEffect(target.getLocation()).setFlashCount(3);
    target
        .getLocation()
        .getNearbyPlayers(10, p -> !p.equals(player))
        .forEach(
            p -> {
              world.strikeLightningEffect(p.getLocation()).setFlashCount(3);
              p.damage(10);
            });
  }
}
