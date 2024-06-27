package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import github.moriyoshi.comminiplugin.system.BigGameSystem;
import github.moriyoshi.comminiplugin.system.biggame.AdminBigGameMenu;

public class AdminGameMenuCommand extends CommandAPICommand {

  public AdminGameMenuCommand() {
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
