package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import github.moriyoshi.comminiplugin.system.AdminBigGameMenu;
import github.moriyoshi.comminiplugin.system.BigGameSystem;

public class AdminMenuCommand extends CommandAPICommand {

  public AdminMenuCommand() {
    super("adminmenu");
    withPermission(CommandPermission.OP);
    executesPlayer(
        (player, args) -> {
          if (BigGameSystem.isIn()) {
            BigGameSystem.getGame().createAdminMenu().openInv(player);
          } else {
            new AdminBigGameMenu().openInv(player);
          }
        });
  }
}
