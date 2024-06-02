package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.system.buttons.AddSpecButton;
import github.moriyoshi.comminiplugin.system.buttons.GameMenuButton;
import github.moriyoshi.comminiplugin.system.buttons.TeleportLobbyButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class MenuCommand extends CommandAPICommand {

  private static class InnerMenu extends MenuHolder<ComMiniPlugin> {

    private final BukkitRunnable task;

    public InnerMenu() {
      super(ComMiniPlugin.getPlugin(), 27, "<yellow>Menu");
      setButton(11, TeleportLobbyButton.of());
      setButton(13, GameMenuButton.of());
      setButton(14, AddSpecButton.of());
      this.task =
          new BukkitRunnable() {

            @Override
            public void run() {
              setButton(11, TeleportLobbyButton.of());
              setButton(13, GameMenuButton.of());
              setButton(14, AddSpecButton.of());
            }
          };
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
      task.cancel();
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
      task.runTaskTimer(getPlugin(), 1, 1);
    }
  }

  public static boolean open(final Player p) {
    if (GameSystem.isStarted() && GameSystem.getGame().isGamePlayer(p)) {
      ComMiniPrefix.MAIN.send(p, "<red>あなたはmenuを開けません");
      return false;
    }
    new InnerMenu().openInv(p);
    return true;
  }

  public MenuCommand() {
    super("menu");
    executesPlayer(
        (p, args) -> {
          open(p);
        });
  }
}
