package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.game.survivalsniper.SSGame.Mode;
import github.moriyoshi.comminiplugin.system.buttons.InventorySlotButton;
import github.moriyoshi.comminiplugin.system.game.IGetGame;
import github.moriyoshi.comminiplugin.system.menu.OnlyBeforeStartGameMenu;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import github.moriyoshi.comminiplugin.lib.BukkitUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class SSFFAMenu extends MenuHolder<ComMiniPlugin>
    implements IGetGame<SSGame>, OnlyBeforeStartGameMenu {

  private final ItemStack JOIN = new ItemBuilder(Material.BLUE_CONCRETE).name("<blue>参加する").build();
  private final ItemStack SPEC = new ItemBuilder(Material.GRAY_CONCRETE).name("<gray>観戦する").build();
  private final ItemStack LEAVE = new ItemBuilder(Material.RED_CONCRETE).name("<red>抜ける").build();

  private final BukkitRunnable task = createAutoCloseTask();

  public SSFFAMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>サバイバルスナイパー");
    setButton(
        12,
        new ItemButton<>(JOIN) {
          @Override
          public void onClick(
              @NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
            getGame().joinPlayer(((Player) event.getWhoClicked()), true, null);
          }
        });
    setButton(
        14,
        new ItemButton<>(SPEC) {
          @Override
          public void onClick(
              @NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
            getGame().joinPlayer(((Player) event.getWhoClicked()), false, null);
          }
        });
    setButton(
        22,
        new ItemButton<>(LEAVE) {
          @Override
          public void onClick(
              @NotNull final MenuHolder<?> holder, @NotNull final InventoryClickEvent event) {
            getGame().leavePlayer((Player) event.getWhoClicked());
          }
        });
    setButton(4, new InventorySlotButton(SSPlayer.class));
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

  @Override
  public Component additional() {
    if (getGame().getMode() == Mode.FFA) {
      return null;
    }
    return BukkitUtil.mm("<red>モードが変わりました、もう一度メニューを開いてください");
  }
}
