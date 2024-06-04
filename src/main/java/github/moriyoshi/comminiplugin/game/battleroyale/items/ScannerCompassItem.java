package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.constant.Messages;
import github.moriyoshi.comminiplugin.game.battleroyale.BRGame;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.Optional;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ScannerCompassItem extends CustomItem {
  public ScannerCompassItem() {
    super(
        new ItemBuilder(Material.RECOVERY_COMPASS)
            .name("<light_purple>スキャナーコンパス")
            .lore("<gray>すぐそばのチェストの方向を指すコンパス", "<gray>チェストのティアがコンパスの色に反映される。")
            .build());
  }

  public ScannerCompassItem(@NotNull ItemStack item) {
    super(item);
  }

  @Override
  public void interact(PlayerInteractEvent e) {
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
              itemUse();
              player.getInventory().addItem(field.getLevel5().random().getFirst());
            },
            () -> {
              Messages.GAME_NOT_START.send(player);
            });
  }
}
