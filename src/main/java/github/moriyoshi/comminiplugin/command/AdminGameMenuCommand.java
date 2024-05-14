package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import github.moriyoshi.comminiplugin.system.game.AdminGameMenu;
import github.moriyoshi.comminiplugin.system.game.GameSystem;

public class AdminGameMenuCommand extends CommandAPICommand {

  public AdminGameMenuCommand() {
    super("adminmenu");
    withPermission(CommandPermission.OP);
    executesPlayer((player, args) -> {
      if (GameSystem.inGame()) {
        GameSystem.getNowGame().createAdminMenu().openInv(player);
      } else {
        new AdminGameMenu().openInv(player);
      }
    });
  }
}
