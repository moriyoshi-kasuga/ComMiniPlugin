package github.moriyoshi.comminiplugin.biggame.battleroyale.items;

import github.moriyoshi.comminiplugin.lib.block.CustomBlock;
import github.moriyoshi.comminiplugin.constant.GameMessages;
import github.moriyoshi.comminiplugin.biggame.battleroyale.BRBigBigGame;
import github.moriyoshi.comminiplugin.biggame.battleroyale.TreasureChest;
import github.moriyoshi.comminiplugin.lib.item.CoolityItem;
import github.moriyoshi.comminiplugin.lib.item.CustomItem;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.BigGameSystem;

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
    if (!BigGameSystem.isIn(BRBigBigGame.class)) {
      GameMessages.GAME_NOT_FOUND.send(player);
      return;
    }
    Optional.ofNullable(BigGameSystem.getGame(BRBigBigGame.class).getField())
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
                  () -> BigGameSystem.getGame(BRBigBigGame.class).prefix.send(player, "<red>宝箱が見つかりません"));
            },
            () -> GameMessages.GAME_NOT_START.send(player));
  }
}
