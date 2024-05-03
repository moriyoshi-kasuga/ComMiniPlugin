package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import github.moriyoshi.comminiplugin.constant.MenuItem;

/**
 * MenuCommand
 */
public class MenuCommand extends CommandAPICommand {

  public MenuCommand() {
    super("menu");
    executesPlayer((p, args) -> {
      MenuItem.open(p);
    });
  }

}
