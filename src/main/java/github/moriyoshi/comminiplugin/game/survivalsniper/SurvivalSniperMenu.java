package github.moriyoshi.comminiplugin.game.survivalsniper;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class SurvivalSniperMenu extends MenuHolder<ComMiniPlugin> {

  private static final ItemStack JOIN = new ItemBuilder(Material.BLUE_CONCRETE).name(
      "<blue>参加する")
      .lore("<white>もう一度クリックで参加を取りやめ").build();
  private static final ItemStack SPEC = new ItemBuilder(Material.GRAY_CONCRETE).name(
      "<gray>観戦する")
      .lore("<white>もう一度クリックで観戦を取りやめ").build();

  public SurvivalSniperMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>サバイバルスナイパー");
    setButton(12, new ItemButton<>(JOIN) {
      @Override
      public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
        SurvivalSniperGame.getInstance().joinPlayer(((Player) event.getWhoClicked()), true);
      }
    });
    setButton(14, new ItemButton<>(SPEC) {
      @Override
      public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
        SurvivalSniperGame.getInstance().joinPlayer(((Player) event.getWhoClicked()), false);
      }
    });
  }
}
