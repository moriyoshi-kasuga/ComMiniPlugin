package github.moriyoshi.comminiplugin.game.survivalsniper;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.game.survivalsniper.SSGame.Mode;
import github.moriyoshi.comminiplugin.system.game.IGetGame;
import github.moriyoshi.comminiplugin.system.buttons.GameStartButton;
import github.moriyoshi.comminiplugin.system.menu.OnlyBeforeStartGameMenu;
import github.moriyoshi.comminiplugin.lib.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class SSAdminMenu extends MenuHolder<ComMiniPlugin>
    implements IGetGame<SSGame>, OnlyBeforeStartGameMenu {

  private final BukkitRunnable task = createAutoCloseTask();

  public SSAdminMenu() {
    super(ComMiniPlugin.getPlugin(), 27, "<blue>OP:サバイバルスナイパー");
    setButton(
        13,
        new ItemButton<>(
            new ItemBuilder(Material.BLACK_CONCRETE)
                .name("<yellow>FFA mode")
                .lore("<gray>default")
                .build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            getGame().setMode(Mode.FFA);
          }
        });
    setButton(
        14,
        new ItemButton<>(new ItemBuilder(Material.END_CRYSTAL).name("<rainbow>Team mode").build()) {
          @Override
          public void onClick(@NotNull MenuHolder<?> holder, @NotNull InventoryClickEvent event) {
            getGame().setMode(Mode.TEAM);
          }
        });
    setButton(16, GameStartButton.of());
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
