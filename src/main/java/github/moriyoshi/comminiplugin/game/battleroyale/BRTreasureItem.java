package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;

public class BRTreasureItem extends CustomItem {
  public BRTreasureItem() {
    this(new ItemBuilder(Material.ENDER_CHEST).name("<red>Treasure Creater")
        .lore("<gray>RightClick to create",
            "<gray>if look block is already treasure,",
            "<gray> open treasure settings menu",
            "<gray>Left Click to remove!",
            "<gray>how to choose (number range 0~5)",
            "<gray> (0 does not place a treasure)",
            "<gray> first: 1 (single select)",
            "<gray> second: 1,2,3,4 (multiple select)",
            "<gray> third: 2~3 (range select, equals 2,3)",
            "<gray> fourth: 40%5,10%0 (random select,Can be changed percent at will.)")
        .build());
  }

  public BRTreasureItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
    val player = e.getPlayer();
    if (!GameSystem.isIn(BRGame.class)) {
      ComMiniPrefix.SYSTEM.send(player, "<red>please start in battleroyale game");
      return;
    }
    if (GameSystem.isStarted()) {
      ComMiniPrefix.SYSTEM.send(player, "<red>if use settings menu, please before br game");
      return;
    }
    val game = GameSystem.getGame(BRGame.class);
    if (game.getField() == null) {
      ComMiniPrefix.SYSTEM.send(player, "<red>宝箱を配置するフィールドを設定してから実行してください");
      return;
    }
    val block = player.getTargetBlockExact(5);
    if (block == null || block.isEmpty()) {
      ComMiniPrefix.SYSTEM.send(player, "<red>please look at a target block");
      return;
    }
    if (!block.isSolid()) {
      ComMiniPrefix.SYSTEM.send(player, "<red>please look at a solid block");
      return;
    }
    val treasure = game.getField().getTreasure();
    if (e.getAction().isLeftClick()) {
      if (!treasure.containsLocation(block.getLocation())) {
        ComMiniPrefix.SYSTEM.send(player, "<red>please look at a treasure block");
        return;
      }
      treasure.removeLocation(block.getLocation());
      ComMiniPrefix.SYSTEM.send(player, "<green>successfully removed treasure block");
    } else {
      if (treasure.containsLocation(block.getLocation())) {
        val values = treasure.getLocationData(block.getLocation());
        if (values.stream().allMatch(p -> p.getSecond() == 1)) {
          ComMiniPrefix.SYSTEM.send(player, "<red>treasure values: "
              + values.stream().map(p -> String.valueOf(p.getFirst()))
                  .collect(Collectors.joining(",")));
          return;
        }
        ComMiniPrefix.SYSTEM.send(player, "<red>treasure values: "
            + values.stream().map(p -> String.format("%d%%%d", p.getSecond(), p.getFirst()))
                .collect(Collectors.joining(",")));
        return;
      }
      treasure.addLocation(block.getLocation(), player);
      block.setType(Material.BEDROCK);
    }
  }

  @Override
  public boolean canShowing() {
    return false;
  }
}
