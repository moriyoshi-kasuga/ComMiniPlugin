package github.moriyoshi.comminiplugin.command;

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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class LocationsCommands extends JsonAPI {

  @Getter() private static final LocationsCommands manager = new LocationsCommands();
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
    locations =
        ComMiniPlugin.gson.fromJson(
            dataElement, new TypeToken<Map<String, Location>>() {}.getType());
  }

  private static class PutLocCommand extends CommandAPICommand {
    public PutLocCommand() {
      super("putloc");
      withPermission(CommandPermission.OP);
      withArguments(
          new StringArgument("name")
              .replaceSuggestions(
                  ArgumentSuggestions.stringsAsync(
                      info ->
                          CompletableFuture.supplyAsync(
                              () -> getManager().locations.keySet().toArray(String[]::new)))));
      executesPlayer(
          (p, args) -> {
            getManager().locations.put((String) args.get("name"), p.getLocation());
          });
    }
  }

  private static class MvLocCommand extends CommandAPICommand {
    @SuppressWarnings("unchecked")
    public MvLocCommand() {
      super("mvloc");
      withPermission(CommandPermission.OP);
      withArguments(
          new StringArgument("name")
              .replaceSuggestions(
                  ArgumentSuggestions.stringsAsync(
                      info ->
                          CompletableFuture.supplyAsync(
                              () -> getManager().locations.keySet().toArray(String[]::new)))));
      withOptionalArguments(new EntitySelectorArgument.ManyPlayers("players"));
      executesPlayer(
          (p, args) -> {
            val loc = getManager().locations.get((String) args.get("name"));
            if (loc == null) {
              ComMiniPrefix.SYSTEM.send(p, "<red>その名前のlocはありません");
              return;
            }
            args.getOptional("players")
                .ifPresentOrElse(
                    (players) ->
                        ((Collection<Player>) players).forEach(player -> player.teleport(loc)),
                    () -> p.teleport(loc));
          });
    }
  }

  private static class DelLocCommand extends CommandAPICommand {
    public DelLocCommand() {
      super("delloc");
      withPermission(CommandPermission.OP);
      withArguments(
          new StringArgument("name")
              .replaceSuggestions(
                  ArgumentSuggestions.stringsAsync(
                      info ->
                          CompletableFuture.supplyAsync(
                              () -> getManager().locations.keySet().toArray(String[]::new)))));
      executesPlayer(
          (p, args) -> {
            if (getManager().locations.remove((String) args.get("name")) == null) {
              ComMiniPrefix.SYSTEM.send(p, "<red>その名前のlocはありません");
            }
          });
    }
  }

  private static class ListLocCommand extends CommandAPICommand {
    public ListLocCommand() {
      super("listloc");
      withPermission(CommandPermission.OP);
      executesPlayer(
          (p, args) -> {
            getManager()
                .locations
                .forEach(
                    (name, location) ->
                        ComMiniPrefix.SYSTEM.send(
                            p,
                            "<gray>"
                                + name
                                + " : "
                                + location.getWorld().getName()
                                + " "
                                + location.getX()
                                + " "
                                + location.getY()
                                + " "
                                + location.getZ()));
          });
    }
  }
}
