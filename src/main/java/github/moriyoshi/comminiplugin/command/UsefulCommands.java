package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.object.jumppad.JumpPadItem;
import github.moriyoshi.comminiplugin.system.ComMiniPlayer;
import lombok.val;

public class UsefulCommands {

  public static class JumpPad extends CommandAPICommand {
    public JumpPad() {
      super("jumppad");
      executesPlayer((player, args) -> {
        player.getInventory().addItem(new JumpPadItem().getItem());
      });
    }
  }

  public static class DebugMode extends CommandAPICommand {
    public DebugMode() {
      super("debugmode");
      executesPlayer((sender, args) -> {
        final ComMiniPlayer player = ComMiniPlayer.getPlayer(sender.getUniqueId());
        val flag = !player.isDebug();
        player.setDebug(flag);
        ComMiniPrefix.SYSTEM.send(sender, flag ? "<red>Debug Enabled" : "<green>Debug Disable");
      });
    }
  }
}
