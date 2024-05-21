package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.button.RedirectItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.GamePlayer;
import github.moriyoshi.comminiplugin.system.IGetGame;
import github.moriyoshi.comminiplugin.system.hotbar.HotBarSlotMenu;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SSMenu extends MenuHolder<ComMiniPlugin> implements IGetGame<SSGame> {

  private final ItemStack JOIN = new ItemBuilder(Material.BLUE_CONCRETE).name("<blue>参加する")
      .lore("<white>もう一度クリックで参加を取りやめ").build();
  private final ItemStack SPEC = new ItemBuilder(Material.GRAY_CONCRETE).name("<gray>観戦する")
      .lore("<white>もう一度クリックで観戦を取りやめ").build();

  public SSMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>サバイバルスナイパー");
    setButton(12, new ItemButton<>(JOIN) {
      @Override
      public void onClick(@NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
        getGame().joinPlayer(((Player) event.getWhoClicked()), true);
      }
    });
    setButton(14, new ItemButton<>(SPEC) {
      @Override
      public void onClick(@NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
        getGame().joinPlayer(((Player) event.getWhoClicked()), false);
      }
    });
    setButton(4, new RedirectItemButton<>(new ItemBuilder(Material.BOOK).name("<green>スロットを設定する").build(),
        (holder,
            event) -> new HotBarSlotMenu(
                GamePlayer.getPlayer(event.getWhoClicked().getUniqueId()).getSurvivapsniperSlot())
                .getInventory()));
  }
}
