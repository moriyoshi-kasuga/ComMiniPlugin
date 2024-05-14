package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.constant.MenuItem;

public class MenuCommand extends CommandAPICommand {

  public MenuCommand() {
    super("menu");
    executesPlayer((p, args) -> {
      if (MenuItem.open(p)) {
        return;
      }
      ComMiniPrefix.MAIN.send(p, "<red>あなたはmenuを開けません");
    });
  }

}
