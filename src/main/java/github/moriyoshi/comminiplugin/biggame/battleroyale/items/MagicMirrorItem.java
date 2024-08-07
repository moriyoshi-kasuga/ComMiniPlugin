package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import github.moriyoshi.comminiplugin.biggame.battleroyale.BRBigGame;
import github.moriyoshi.comminiplugin.constant.GameMessages;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.BigGameSystem;
import java.util.Optional;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MagicMirrorItem extends CustomItem implements CustomItem.InteractMainHand {
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
  public void interactMainHand(PlayerInteractEvent e, final Player player) {
    if (e.getAction().isLeftClick()) {
      return;
    }
    if (!BigGameSystem.isIn(BRBigGame.class)) {
      GameMessages.GAME_NOT_FOUND.send(player);
      return;
    }
    Optional.ofNullable(BigGameSystem.getGame(BRBigGame.class).getField())
        .ifPresentOrElse(
            field -> {
              useItemAmount();
              if (new Random().nextBoolean()) {
                player.getInventory().addItem(field.getLevel4().random().getFirst());
              } else {
                player.getInventory().addItem(field.getLevel5().random().getFirst());
              }
            },
            () -> GameMessages.GAME_NOT_START.send(player));
  }
}
