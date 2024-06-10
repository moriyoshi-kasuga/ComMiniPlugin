package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.constant.Messages;
import github.moriyoshi.comminiplugin.game.battleroyale.BRGame;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.game.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.Optional;
import java.util.Random;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MagicMirrorItem extends CustomItem {
  public MagicMirrorItem() {
    super(
        new ItemBuilder(Material.PHANTOM_MEMBRANE)
            .name("<dark_purple>魔法の鏡")
            .lore("<gray>使用するとランダムな強力アイテムを一つ生成する。")
            .customModelData(14)
            .build());
  }

  public MagicMirrorItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    val player = e.getPlayer();
    if (!GameSystem.isIn(BRGame.class)) {
      Messages.GAME_NOT_FOUND.send(player);
      return;
    }
    Optional.ofNullable(GameSystem.getGame(BRGame.class).getField())
        .ifPresentOrElse(
            field -> {
              useItemAmount();
              if (new Random().nextBoolean()) {
                player.getInventory().addItem(field.getLevel4().random().getFirst());
              } else {
                player.getInventory().addItem(field.getLevel5().random().getFirst());
              }
            },
            () -> Messages.GAME_NOT_START.send(player)
        );
  }
}
