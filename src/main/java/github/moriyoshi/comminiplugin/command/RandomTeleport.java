package github.moriyoshi.comminiplugin.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.Location2DArgument;
import dev.jorel.commandapi.arguments.WorldArgument;
import dev.jorel.commandapi.wrappers.Location2D;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import org.bukkit.World;

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
      if (!BukkitUtil.randomTeleport(p, (World) args.get("world"), loc.getBlockX(), loc.getBlockZ(),
          (int) args.get("radius"),
          (int) args.getOrDefault("maxTry", 100)
      )) {
        ComMiniPrefix.MAIN.send(p,
            "<red>そのあたりにテレポートはできません(もしかしたら海などです)"
        );
      }
    });
  }
}
