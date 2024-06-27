package github.moriyoshi.comminiplugin.biggame.battleroyale;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.BigGameSystem;

import java.util.stream.Collectors;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BRTreasureItem extends CustomItem {
  public BRTreasureItem() {
    this(
        new ItemBuilder(Material.ENDER_CHEST)
            .name("<red>Treasure Creater")
            .lore(
                "<gray>RightClick to create",
                "<gray>if look block is already treasure,",
                "<gray> open treasure settings menu",
                "<gray>Left Click to remove!",
                "<gray>how to choose (number range 0~5)",
                "<gray> (0 does not place a treasure)",
                "<gray> first: 1 (single select)",
                "<gray> second: 1,2,3,4 (multiple select)",
                "<gray> third: 2~3 (range select, equals 2,3)",
                "<gray> fourth: 40%5,10%0 (random select)",
                "<gray>  (Can be changed percent at will.)")
            .build());
  }

  public BRTreasureItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interactMainHand(PlayerInteractEvent e) {
    e.setCancelled(true);
    val player = e.getPlayer();
    if (!BigGameSystem.isIn(BRBigBigGame.class)) {
      ComMiniPlugin.SYSTEM.send(player, "<red>please start in battleroyale game");
      return;
    }
    if (BigGameSystem.isStarted()) {
      ComMiniPlugin.SYSTEM.send(player, "<red>if use settings menu, please before br game");
      return;
    }
    val game = BigGameSystem.getGame(BRBigBigGame.class);
    if (game.getField() == null) {
      ComMiniPlugin.SYSTEM.send(player, "<red>宝箱を配置するフィールドを設定してから実行してください");
      return;
    }
    val block = player.getTargetBlockExact(5);
    if (block == null || block.isEmpty()) {
      ComMiniPlugin.SYSTEM.send(player, "<red>please look at a target block");
      return;
    }
    if (!block.isSolid()) {
      ComMiniPlugin.SYSTEM.send(player, "<red>please look at a solid block");
      return;
    }
    val treasure = game.getField().getTreasure();
    if (e.getAction().isLeftClick()) {
      if (treasure.removeLocation(block.getLocation()) == null) {
        ComMiniPlugin.SYSTEM.send(player, "<red>please look at a treasure block");
        return;
      }
      ComMiniPlugin.SYSTEM.send(player, "<green>successfully removed treasure block");
    } else {
      val values = treasure.getLocationData(block.getLocation());
      if (values != null) {
        if (values.stream().allMatch(p -> p.getSecond() == 1)) {
          ComMiniPlugin.SYSTEM.send(
              player,
              "<red>treasure values: "
                  + values.stream().map(p -> p.getFirst() + "lv").collect(Collectors.joining(",")));
          return;
        }
        ComMiniPlugin.SYSTEM.send(
            player,
            "<red>treasure values: "
                + values.stream()
                    .map(p -> String.format("%d%%%dlv", p.getSecond(), p.getFirst()))
                    .collect(Collectors.joining(",")));
        return;
      }
      treasure.addLocation(block.getLocation(), player);
    }
  }

  @Override
  public boolean canStack() {
    return true;
  }

  @Override
  public boolean canShowing() {
    return false;
  }
}
