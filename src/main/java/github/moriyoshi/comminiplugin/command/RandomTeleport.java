package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.IntegerArgument;
import github.moriyoshi.comminiplugin.util.BukkitUtil;

public class RandomTeleport extends CommandAPICommand {
  public RandomTeleport() {
    super("rtp");
    withPermission(CommandPermission.OP);
    withArguments(new IntegerArgument("radius", 1));
    withOptionalArguments(new IntegerArgument("maxTry", 1));
    executesPlayer((p, args) -> {
      BukkitUtil.randomTeleport(p, p.getLocation(), (int) args.get("radius"), (int) args.getOrDefault("maxTry", 100));
    });
  }
}
