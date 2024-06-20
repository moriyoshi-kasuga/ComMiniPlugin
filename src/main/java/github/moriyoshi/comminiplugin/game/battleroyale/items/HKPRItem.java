package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.constant.GameMessages;
import github.moriyoshi.comminiplugin.game.battleroyale.BRGame;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.system.game.GameSystem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HKPRItem extends CustomItem {
  public HKPRItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<yellow>HKPR")
            .lore("<gray>一番近くのプレイヤーと位置を交換します")
            .customModelData(16)
            .build());
  }

  public HKPRItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    val player = e.getPlayer();
    if (!GameSystem.isStarted(BRGame.class)) {
      GameMessages.GAME_NOT_START.send(player);
      return;
    }
    useItemAmount();
    val game = GameSystem.getGame(BRGame.class);
    val loc = player.getLocation();
    Bukkit.getOnlinePlayers().stream()
        .filter(p -> !player.equals(p) && game.isPlayingPlayer(p))
        .min(
            (p1, p2) -> {
              val loc1 = p1.getLocation().distanceSquared(loc);
              val loc2 = p2.getLocation().distanceSquared(loc);
              return Double.compare(loc1, loc2);
            })
        .ifPresentOrElse(
            p -> {
              player.teleport(p.getLocation());
              game.prefix.send(player, "<yellow>入れ替わりました");
              p.teleport(loc);
              game.prefix.send(p, "<yellow>HKPR で 他のプレイヤーと入れ替わりました!");
            },
            () -> game.prefix.send(player, "<red>他にプレイヤーはいません"));
  }
}
