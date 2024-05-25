package github.moriyoshi.comminiplugin.command;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.WorldArgument;
import lombok.val;

public class MultiverseTeleportCommand extends CommandAPICommand {

  public MultiverseTeleportCommand() {
    super("mvtp");
    withArguments(new WorldArgument("world"));
    withOptionalArguments(new EntitySelectorArgument.ManyPlayers("player"));
    withOptionalArguments(new LocationArgument("location"));
    executesPlayer((p, args) -> {
      val world = ((World) args.get("world"));
      args.getOptional("player").ifPresentOrElse((temp) -> {
        @SuppressWarnings("unchecked")
        val players = (Collection<Player>) temp;
        args.getOptional("location").ifPresentOrElse((temp2) -> {
          val location = (Location) temp2;
          location.setWorld(world);
          players.forEach(player -> {
            player.teleport(location);
          });
        }, () -> {
          players.forEach(player -> {
            player.teleport(world.getSpawnLocation());
          });
        });
      }, () -> {
        p.teleport(world.getSpawnLocation());
      });
    });
  }
}
