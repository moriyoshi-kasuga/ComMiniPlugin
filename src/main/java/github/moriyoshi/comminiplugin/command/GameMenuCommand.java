package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.system.GameSystem;

public class GameMenuCommand extends CommandAPICommand {

  public GameMenuCommand() {
    super("gamemenu");
    executesPlayer((player, args) -> {
      if (!GameSystem.inGame()) {
        ComMiniPrefix.MAIN.send(player, "<red>ゲーム中ではありません");
        return;
      }

      if (!GameSystem.canOpenMenu()) {
        ComMiniPrefix.MAIN.send(player, "<red>もうMenuは開けません");
        return;
      }

      GameSystem.nowGame().gameMenu(player).openInv(player);
    });
  }
}
