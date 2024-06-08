package github.moriyoshi.comminiplugin.game.battleroyale;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.game.IGetGame;
import github.moriyoshi.comminiplugin.system.buttons.HotbarSlotButton;
import github.moriyoshi.comminiplugin.system.menu.OnlyBeforeStartGameMenu;
import github.moriyoshi.comminiplugin.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class BRMenu extends MenuHolder<ComMiniPlugin>
    implements IGetGame<BRGame>, OnlyBeforeStartGameMenu {

  private final BukkitRunnable task = createAutoCloseTask();

  private final ItemStack JOIN =
      new ItemBuilder(Material.BLUE_CONCRETE)
          .name("<blue>参加する")
          .lore("<white>もう一度クリックで参加を取りやめ")
          .build();
  private final ItemStack SPEC =
      new ItemBuilder(Material.GRAY_CONCRETE)
          .name("<gray>観戦する")
          .lore("<white>もう一度クリックで観戦を取りやめ")
          .build();

  public BRMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<yellow>バトルロワイヤル");
    setButton(
        12,
        new ItemButton<>(JOIN) {
          @Override
          public void onClick(
              @NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
            getGame().joinPlayer(((Player) event.getWhoClicked()), true);
          }
        });
    setButton(
        14,
        new ItemButton<>(SPEC) {
          @Override
          public void onClick(
              @NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
            getGame().joinPlayer(((Player) event.getWhoClicked()), false);
          }
        });
    setButton(4, new HotbarSlotButton(BRPlayer.class));
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    this.task.cancel();
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    if (isClosed()) {
      return;
    }
    super.onClick(event);
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {
    this.task.runTaskTimer(getPlugin(), 1, 1);
  }
}
