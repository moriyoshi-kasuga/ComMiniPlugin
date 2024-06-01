package github.moriyoshi.comminiplugin.game.battleroyale;

import java.util.Optional;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.constant.ComMiniWorld;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.IGetGame;
import github.moriyoshi.comminiplugin.system.buttons.GameStartButton;
import github.moriyoshi.comminiplugin.system.menu.OnlyBeforeStartGameMenu;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import lombok.val;

public class BRAdminMenu extends MenuHolder<ComMiniPlugin> implements IGetGame<BRGame>, OnlyBeforeStartGameMenu {
  private static class FieldButton extends ItemButton<MenuHolder<ComMiniPlugin>> {
    private final Supplier<BRField> fieldSupplier;
    private final String name;

    public FieldButton(Material material, String name, Supplier<BRField> fieldSupplier) {
      super(new ItemBuilder(material).name(name).build());
      this.fieldSupplier = fieldSupplier;
      this.name = name;
    }

    @Override
    public void onClick(@NotNull MenuHolder<ComMiniPlugin> holder, @NotNull InventoryClickEvent event) {
      val game = GameSystem.getGame(BRGame.class);
      game.setField(fieldSupplier.get());
      game.prefix.cast("<gray>今回の舞台は " + name);
    }

  }

  public BRAdminMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>OP:バトルロワイヤル");
    setButton(16, GameStartButton.of());
    setButton(13, new FieldButton(Material.DIRT, "<green>バイオームフィールド",
        () -> new BRField("biome", new Location(ComMiniWorld.GAME_WORLD, 1000.5, 60, 1000.5), 400, 50)));
    setButton(3, new ItemButton<>(new ItemBuilder(Material.GREEN_CONCRETE).name("<green>宝箱の設置開始").build()) {
      @Override
      public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
        Optional.ofNullable(getGame().getField()).ifPresentOrElse(field -> {
          field.getTreasure().addPlayer((Player) event.getWhoClicked());
          ComMiniPrefix.SYSTEM.send(event.getWhoClicked(), "<green>宝箱設定開始しました");
          event.getWhoClicked().getInventory().addItem(new BRTreasureItem().getItem());
        }, () -> ComMiniPrefix.SYSTEM.send(event.getWhoClicked(), "<red>宝箱を配置するフィールドを設定してから実行してください"));
      }
    });
    setButton(5, new ItemButton<>(new ItemBuilder(Material.RED_CONCRETE).name("<red>宝箱の設置終了").build()) {
      @Override
      public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
        Optional.ofNullable(getGame().getField()).ifPresentOrElse(field -> {
          field.getTreasure().removePlayer((Player) event.getWhoClicked());
          ComMiniPrefix.SYSTEM.send(event.getWhoClicked(), "<red>宝箱設定終了しました");
        }, () -> ComMiniPrefix.SYSTEM.send(event.getWhoClicked(), "<red>宝箱を配置するフィールドを設定してから実行してください"));
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
}
