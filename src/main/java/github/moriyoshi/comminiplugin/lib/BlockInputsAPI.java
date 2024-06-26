package github.moriyoshi.comminiplugin.lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

@Getter
@SuppressWarnings("deprecation")
public abstract class BlockInputsAPI<T> extends JsonAPI {

  private final Map<UUID, Player> players = new HashMap<>();

  private Map<Location, T> locations;

  public BlockInputsAPI(InterfaceAPIPlugin plugin, String name) {
    super(plugin, name);
  }

  public BlockInputsAPI(InterfaceAPIPlugin plugin, String path, String name) {
    super(plugin, path, name);
  }

  public final void addLocation(Location location, Player player) {
    location.setPitch(0);
    location.setYaw(BukkitUtil.convertBlockFaceToYaw(player.getFacing().getOppositeFace()));
    innerAddLocation(location.toBlockLocation(), player);
  }

  protected abstract void innerAddLocation(Location location, Player player);

  protected final void finalAddLocation(Location location, T data) {
    removeLocation(location);
    locations.put(location, data);
    showLocation(location);
  }

  public final void addPlayer(Player player) {
    if (players.put(player.getUniqueId(), player) == null) {
      showAllLocation(player);
    }
  }

  public final void removePlayer(Player player) {
    if (players.remove(player.getUniqueId()) != null) {
      hideAllLocation(player);
    }
  }

  public final void clearPlayer() {
    players.values().forEach(this::hideAllLocation);
    players.clear();
  }

  public final boolean containsPlayer(Player player) {
    return players.containsKey(player.getUniqueId());
  }

  public final T getLocationData(Location loc) {
    val location = loc.toBlockLocation();
    location.setPitch(0);
    for (val yaw : List.of(180, 90, -90, 0)) {
      location.setYaw(yaw);
      val finalLoc = locations.get(location);
      if (finalLoc != null) {
        return finalLoc;
      }
    }
    return null;
  }

  public final BlockFace getLocationBlockFace(Location loc) {
    val location = loc.toBlockLocation();
    location.setPitch(0);
    for (val yaw : List.of(180, 90, -90, 0)) {
      location.setYaw(yaw);
      if (locations.containsKey(location)) {
        return BukkitUtil.convertYawToBlockFace(yaw);
      }
    }
    return null;
  }

  public final boolean containsLocation(Location loc) {
    val location = loc.toBlockLocation();
    location.setPitch(0);
    for (val yaw : List.of(180, 90, -90, 0)) {
      location.setYaw(yaw);
      if (locations.containsKey(location)) {
        return true;
      }
    }
    return false;
  }

  public final Location removeLocation(Location loc) {
    val location = loc.toBlockLocation();
    location.setPitch(0);
    for (val yaw : List.of(180, 90, -90, 0)) {
      location.setYaw(yaw);
      if (locations.remove(location) != null) {
        hideLocation(location);
        return location;
      }
    }
    return null;
  }

  public final void hideAllLocation(Player player) {
    locations.keySet().forEach(loc -> hideLocation(loc, player));
  }

  public final void hideLocation(Location location) {
    players.values().forEach(p -> hideLocation(location, p));
  }

  public final void hideLocation(Location loc, Player player) {
    val location = loc.toBlockLocation();
    try {
      PluginLib.getGlowingBlocks().unsetGlowing(location, player);
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }
  }

  public final void showAllLocation(Player player) {
    locations.keySet().forEach(loc -> showLocation(loc, player));
  }

  public final void showLocation(Location location) {
    players.values().forEach(p -> showLocation(location, p));
  }

  public final void showLocation(Location loc, Player player) {
    val location = loc.toBlockLocation();
    try {
      PluginLib.getGlowingBlocks().setGlowing(location, player, getColor(location));
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }
  }

  public abstract ChatColor getColor(Location location);

  public abstract T loadLocData(JsonElement element);

  public abstract JsonElement saveLocData(T data);

  @Override
  protected void generateLoadData(JsonElement dataElement) {
    locations = new HashMap<>();
    val data = dataElement.getAsJsonObject();
    if (data.has("locations")) {
      data.getAsJsonArray("locations")
          .forEach(
              element -> {
                val obj = element.getAsJsonObject();
                getLocations()
                    .put(
                        PluginLib.gson.fromJson(obj.get("loc"), Location.class),
                        loadLocData(obj.get("data")));
              });
    }
  }

  @Override
  protected JsonElement generateSaveData() {
    val object = new JsonObject();
    val array = new JsonArray();
    locations.forEach(
        (loc, data) -> {
          val obj = new JsonObject();
          obj.add("loc", PluginLib.gson.toJsonTree(loc));
          obj.add("data", saveLocData(data));
          array.add(obj);
        });
    object.add("locations", array);
    return object;
  }
}
