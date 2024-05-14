package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.system.GameSystem;

public class FinalizeGameCommand extends CommandAPICommand {

  public FinalizeGameCommand() {
    super("finalize");
    withPermission(CommandPermission.OP);
    executesPlayer((p, args) -> {
      if (!GameSystem.finalizeGame()) {
        ComMiniPrefix.MAIN.send(p, "<red>現在ゲームは開催されていません");
      }
    });
  }
}
