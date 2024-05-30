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
        .lore("<gray>Click to create treasure at the look block!",
            "<gray>if look block is already treasure,",
            "<gray> open treasure settings menu",
            "<gray>how to choose (number range 1~5)",
            "<gray> first: 1 (single select)", "<gray> second: 1,2,3,4 (multiple select)",
            "<gray> third: 2~3 (range select, equals 2,3)")
        .build());
    ;
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
    val block = e.getClickedBlock();
    if (block == null || block.isEmpty()) {
      ComMiniPrefix.SYSTEM.send(player, "<red>please look at a target block");
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
        ComMiniPrefix.SYSTEM.send(player, "<red>treasure values: "
            + treasure.getLocationData(block.getLocation()).stream().map(i -> String.valueOf(i))
                .collect(Collectors.joining(",")));
        return;
      }
      treasure.addLocation(block.getLocation(), player);
    }
  }

  @Override
  public boolean canShowing() {
    return false;
  }
}
