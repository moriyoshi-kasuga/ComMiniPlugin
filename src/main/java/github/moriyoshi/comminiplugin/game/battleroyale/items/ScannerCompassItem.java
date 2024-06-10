package github.moriyoshi.comminiplugin.game.battleroyale.items;

import github.moriyoshi.comminiplugin.block.CustomBlock;
import github.moriyoshi.comminiplugin.constant.Messages;
import github.moriyoshi.comminiplugin.game.battleroyale.BRGame;
import github.moriyoshi.comminiplugin.game.battleroyale.TreasureChest;
import github.moriyoshi.comminiplugin.item.CoolityItem;
import github.moriyoshi.comminiplugin.item.CustomItem;
import github.moriyoshi.comminiplugin.system.game.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.jetbrains.annotations.NotNull;

public class ScannerCompassItem extends CustomItem implements CoolityItem {
  public ScannerCompassItem() {
    super(
        new ItemBuilder(Material.COMPASS)
            .name("<light_purple>スキャナーコンパス")
            .lore("<gray>すぐそばのチェストの方向を指すコンパス", "<gray>チェストのティアがコンパスの色に反映される。")
            .customModelData(100)
            .build());
    setDurability(8);
  }

  public ScannerCompassItem(@NotNull ItemStack item) {
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
              if (!hasDurability()) {
                return;
              }
              if (inCooldown()) {
                return;
              }
              useItemDurability();
              setCooldown(10 * 20);
              val base = player.getLocation();
              val first =
                  field.getTreasure().getLocations().keySet().stream()
                      .filter(loc -> CustomBlock.isCustomBlock(loc, TreasureChest.class))
                      .min(
                          (l1, l2) -> {
                            val loc1 = l1.distanceSquared(base);
                            val loc2 = l2.distanceSquared(base);
                            return Double.compare(loc1, loc2);
                          });
              first.ifPresentOrElse(
                  loc ->
                      new ItemBuilder(getItem())
                          .customModelData(
                              CustomBlock.getCustomBlock(loc, TreasureChest.class).getLevel())
                          .changeMeta(
                              (Consumer<CompassMeta>)
                                  meta -> {
                                    meta.setLodestone(loc);
                                    meta.setLodestoneTracked(false);
                                  }),
                  () -> GameSystem.getGame(BRGame.class).prefix.send(player, "<red>宝箱が見つかりません"));
            },
            () -> Messages.GAME_NOT_START.send(player));
  }
}
