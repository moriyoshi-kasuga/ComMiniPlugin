package github.moriyoshi.comminiplugin.command;

import org.bukkit.World;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.Location2DArgument;
import dev.jorel.commandapi.arguments.WorldArgument;
import dev.jorel.commandapi.wrappers.Location2D;
import github.moriyoshi.comminiplugin.util.BukkitUtil;

public class RandomTeleport extends CommandAPICommand {
  public RandomTeleport() {
    super("rtp");
    withPermission(CommandPermission.OP);
    withArguments(new WorldArgument("world"));
    withArguments(new Location2DArgument("loc"));
    withArguments(new IntegerArgument("radius", 1));
    withOptionalArguments(new IntegerArgument("maxTry", 1));
    executesPlayer((p, args) -> {
      Location2D loc = (Location2D) args.get("loc");
      BukkitUtil.randomTeleport(p, (World) args.get("world"), loc.getBlockX(), loc.getBlockZ(),
          (int) args.get("radius"),
          (int) args.getOrDefault("maxTry", 100));
    });
  }
}
