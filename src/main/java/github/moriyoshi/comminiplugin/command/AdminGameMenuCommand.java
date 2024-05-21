package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import github.moriyoshi.comminiplugin.system.AdminGameMenu;
import github.moriyoshi.comminiplugin.system.GameSystem;

public class AdminGameMenuCommand extends CommandAPICommand {

  public AdminGameMenuCommand() {
    super("adminmenu");
    withPermission(CommandPermission.OP);
    executesPlayer((player, args) -> {
      if (GameSystem.isIn()) {
        GameSystem.getGame().createAdminMenu().openInv(player);
      } else {
        new AdminGameMenu().openInv(player);
      }
    });
  }
}
