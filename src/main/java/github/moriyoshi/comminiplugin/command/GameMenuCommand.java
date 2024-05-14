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
      if (GameSystem.isStarted() && GameSystem.getNowGame().isGamePlayer(player)) {
        ComMiniPrefix.MAIN.send(player, "<red>Menuは開けません");
        return;
      }
      GameSystem.getNowGame().createGameMenu(player).openInv(player);
    });
  }
}
