package github.moriyoshi.comminiplugin.command;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.dependencies.ui.button.ItemButton;
import github.moriyoshi.comminiplugin.dependencies.ui.menu.MenuHolder;
import github.moriyoshi.comminiplugin.system.buttons.GameMenuButton;
import github.moriyoshi.comminiplugin.system.buttons.TeleportLobbyButton;
import github.moriyoshi.comminiplugin.system.GameSystem;
import github.moriyoshi.comminiplugin.util.ItemBuilder;

public class MenuCommand extends CommandAPICommand {

  private static class InnerMenu extends MenuHolder<ComMiniPlugin> {

    public InnerMenu() {
      super(ComMiniPlugin.getPlugin(), 27, "<yellow>Menu");
      setButton(11, TeleportLobbyButton.of());
      setButton(13, GameMenuButton.of());
      if (GameSystem.isStarted()) {
        return;
      }
      setButton(14, new ItemButton<>(new ItemBuilder(Material.BEDROCK).name("まだ開始されていません").build()));
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
    executesPlayer((p, args) -> {
      open(p);
    });
  }

}
