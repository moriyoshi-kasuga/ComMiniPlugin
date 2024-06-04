package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.game.survivalsniper.SSCustomMenu;
import github.moriyoshi.comminiplugin.object.jumppad.JumpPadItem;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import lombok.val;

@SuppressWarnings("unused")
public class UsefulCommands {

  private static class JumpPadCommand extends CommandAPICommand {
    public JumpPadCommand() {
      super("jumppad");
      executesPlayer(
          (player, args) -> {
            player.getInventory().addItem(new JumpPadItem().getItem());
          });
    }
  }

  private static class DebugModeCommand extends CommandAPICommand {
    public DebugModeCommand() {
      super("debugmode");
      executesPlayer(
          (sender, args) -> {
            final ComMiniPlayer player = ComMiniPlayer.getPlayer(sender.getUniqueId());
            val flag = !player.isDebug();
            player.setDebug(flag);
            ComMiniPrefix.SYSTEM.send(sender, flag ? "<red>Debug Enabled" : "<green>Debug Disable");
          });
    }
  }

  private static class SSCustomMenuCommand extends CommandAPICommand {
    public SSCustomMenuCommand() {
      super("sscustommenu");
      executesPlayer(
          (sender, args) -> {
            sender.openInventory(new SSCustomMenu().getInventory());
          });
    }
  }
}
