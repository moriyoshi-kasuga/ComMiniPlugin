package github.moriyoshi.comminiplugin.biggame.battleroyale;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.system.BigGameSystem;
import github.moriyoshi.comminiplugin.system.buttons.GameStartButton;
import github.moriyoshi.comminiplugin.system.menu.OnlyBeforeStartGameMenu;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class BRAdminMenu extends MenuHolder<ComMiniPlugin> implements OnlyBeforeStartGameMenu {

  public BRAdminMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>OP:バトルロワイヤル");
    setButton(16, GameStartButton.of());
    setButton(
        13,
        new FieldButton(
            Material.DIRT,
            "<green>バイオームフィールド",
            () ->
                new BRField(
                    "biome",
                    new Location(ComMiniWorld.GAME_WORLD, 1000.5, 61, 1000.5),
                    400,
                    70,
                    100,
                    60,
                    50,
                    70,
                    10)));
    setButton(
        14,
        new FieldButton(
            Material.IRON_BLOCK,
            "<gray>施設",
            () ->
                new BRField(
                    "facility",
                    new Location(ComMiniWorld.GAME_WORLD, 10000.5, 17, 100000.5),
                    100,
                    20,
                    30,
                    10,
                    10,
                    10,
                    5)));
    setButton(
        3,
        new ItemButton<>(new ItemBuilder(Material.GREEN_CONCRETE).name("<green>宝箱の設置開始").build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            Optional.ofNullable(BigGameSystem.getGame(BRBigGame.class).getField())
                .ifPresentOrElse(
                    field -> {
                      field.getTreasure().addPlayer((Player) event.getWhoClicked());
                      ComMiniPlugin.SYSTEM.send(event.getWhoClicked(), "<green>宝箱設定開始しました");
                      event.getWhoClicked().getInventory().addItem(new BRTreasureItem().getItem());
                    },
                    () ->
                        ComMiniPlugin.SYSTEM.send(
                            event.getWhoClicked(), "<red>宝箱を配置するフィールドを設定してから実行してください"));
          }
        });
    setButton(
        5,
        new ItemButton<>(new ItemBuilder(Material.RED_CONCRETE).name("<red>宝箱の設置終了").build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            Optional.ofNullable(BigGameSystem.getGame(BRBigGame.class).getField())
                .ifPresentOrElse(
                    field -> {
                      field.getTreasure().removePlayer((Player) event.getWhoClicked());
                      ComMiniPlugin.SYSTEM.send(event.getWhoClicked(), "<red>宝箱設定終了しました");
                    },
                    () ->
                        ComMiniPlugin.SYSTEM.send(
                            event.getWhoClicked(), "<red>宝箱を配置するフィールドを設定してから実行してください"));
          }
        });
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    if (isClosed()) {
      return;
    }
    super.onClick(event);
  }

  private static class FieldButton extends ItemButton<MenuHolder<ComMiniPlugin>> {
    private final Supplier<BRField> fieldSupplier;
    private final String name;

    public FieldButton(Material material, String name, Supplier<BRField> fieldSupplier) {
      super(new ItemBuilder(material).name(name).build());
      this.fieldSupplier = fieldSupplier;
      this.name = name;
    }

    @Override
    public void onClick(
        @NotNull MenuHolder<ComMiniPlugin> holder, @NotNull InventoryClickEvent event) {
      val game = BigGameSystem.getGame(BRBigGame.class);
      game.setField(fieldSupplier.get());
      game.prefix.broadCast("<gray>今回の舞台は <u>" + name);
    }
  }
}
