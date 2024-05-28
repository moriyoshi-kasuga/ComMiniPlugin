package github.moriyoshi.comminiplugin.command;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.api.JsonAPI;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import lombok.Getter;
import lombok.val;

public class LocationsCommands extends JsonAPI {

  public static class PutLocCommand extends CommandAPICommand {
    public PutLocCommand() {
      super("putloc");
      withPermission(CommandPermission.OP);
      withArguments(new StringArgument("name").replaceSuggestions(ArgumentSuggestions.stringsAsync(info -> {
        return CompletableFuture.supplyAsync(() -> {
          return getManager().locations.keySet().toArray(String[]::new);
        });
      })));
      executesPlayer((p, args) -> {
        getManager().locations.put((String) args.get("name"), p.getLocation());
      });
    }
  }

  public static class MvLocCommand extends CommandAPICommand {
    @SuppressWarnings("unchecked")
    public MvLocCommand() {
      super("mvloc");
      withPermission(CommandPermission.OP);
      withArguments(new StringArgument("name").replaceSuggestions(ArgumentSuggestions.stringsAsync(info -> {
        return CompletableFuture.supplyAsync(() -> {
          return getManager().locations.keySet().toArray(String[]::new);
        });
      })));
      withOptionalArguments(new EntitySelectorArgument.ManyPlayers("players"));
      executesPlayer((p, args) -> {
        if (!getManager().locations.containsKey((String) args.get("name"))) {
          ComMiniPrefix.SYSTEM.send(p, "<red>その名前のlocはありません");
          return;
        }
        val loc = getManager().locations.get((String) args.get("name"));
        args.getOptional("players").ifPresentOrElse((players) -> {
          ((Collection<Player>) players).forEach(player -> player.teleport(loc));
        }, () -> p.teleport(loc));
      });
    }
  }

  public static class DelLocCommand extends CommandAPICommand {
    public DelLocCommand() {
      super("delloc");
      withPermission(CommandPermission.OP);
      withArguments(new StringArgument("name").replaceSuggestions(ArgumentSuggestions.stringsAsync(info -> {
        return CompletableFuture.supplyAsync(() -> {
          return getManager().locations.keySet().toArray(String[]::new);
        });
      })));
      executesPlayer((p, args) -> {
        val name = (String) args.get("name");
        if (!getManager().locations.containsKey(name)) {
          ComMiniPrefix.SYSTEM.send(p, "<red>その名前のlocはありません");
          return;
        }
        getManager().locations.remove(name);
      });
    }
  }

  public static class ListLocCommand extends CommandAPICommand {
    public ListLocCommand() {
      super("listloc");
      withPermission(CommandPermission.OP);
      executesPlayer((p, args) -> {
        getManager().locations.forEach((name, location) -> {
          ComMiniPrefix.SYSTEM.send(p,
              "<gray>" + name + " : " + location.getWorld().getName() + " " + location.getX() + " " + location.getY()
                  + " " + location.getZ());
        });
      });
    }
  }

  @Getter(lazy = true)
  private static final LocationsCommands manager = new LocationsCommands();

  private Map<String, Location> locations;

  private LocationsCommands() {
    super(ComMiniPlugin.getPlugin(), "LocationsCommands");
  }

  @Override
  protected JsonElement generateSaveData() {
    return ComMiniPlugin.gson.toJsonTree(locations);

  }

  @Override
  protected void generateLoadData(JsonElement dataElement) {
    locations = ComMiniPlugin.gson.fromJson(dataElement, new TypeToken<Map<String, Location>>() {
    }.getType());
  }

}
