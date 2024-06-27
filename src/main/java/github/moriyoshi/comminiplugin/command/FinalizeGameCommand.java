package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.system.BigGameSystem;

public class FinalizeGameCommand extends CommandAPICommand {

  public FinalizeGameCommand() {
    super("finalize");
    withPermission(CommandPermission.OP);
    executesPlayer(
        (p, args) -> {
          if (!BigGameSystem.finalGame()) {
            ComMiniPlugin.MAIN.send(p, "<red>現在ゲームは開催されていません");
          }
        });
  }
}
