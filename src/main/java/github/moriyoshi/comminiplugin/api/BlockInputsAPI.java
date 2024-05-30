package github.moriyoshi.comminiplugin.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.util.BukkitUtil;
import lombok.Getter;
import lombok.val;

@SuppressWarnings("deprecation")
public abstract class BlockInputsAPI<T> extends JsonAPI {

  @Getter
  private final Set<Player> players = new HashSet<>();

  @Getter
  private final Map<Location, T> locations = new HashMap<>();

  public BlockInputsAPI(Plugin plugin, String name) {
    super(plugin, name);
  }

  public BlockInputsAPI(Plugin plugin, String path, String name) {
    super(plugin, path, name);
  }

  public final void addLocation(Location location, Player player) {
    location.setYaw(BukkitUtil.convertBlockFaceToYaw(player.getFacing().getOppositeFace()));
    innerAddLocation(location, player);
  }

  protected abstract void innerAddLocation(Location location, Player player);

  protected final void finalAddLocation(Location location, T data) {
    removeLocation(location);
    locations.put(location, data);
    showLocation(location);
  }

  public final void addPlayer(Player player) {
    players.add(player);
    showAllLocation(player);
  }

  public final void removePlayer(Player player) {
    players.remove(player);
    hideAllLocation(player);
  }

  public final boolean containsLocation(Location location) {
    for (val yaw : List.of(180, 90, -90, 0)) {
      location.setYaw(yaw);
      if (locations.containsKey(location)) {
        return true;
      }
    }
    return false;
  }

  public final void removeLocation(Location location) {
    for (val yaw : List.of(180, 90, -90, 0)) {
      location.setYaw(yaw);
      if (locations.remove(location) != null) {
        hideLocation(location);
      }
    }
  }

  public final void hideAllLocation(Player player) {
    locations.keySet().forEach(loc -> hideLocation(loc, player));
  }

  public final void hideLocation(Location location) {
    players.forEach(p -> hideLocation(location, p));
  }

  public final void hideLocation(Location location, Player player) {
    try {
      ComMiniPlugin.getGlowingBlocks().unsetGlowing(location, player);
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }
  }

  public final void showAllLocation(Player player) {
    locations.keySet().forEach(loc -> showLocation(loc, player));
  }

  public final void showLocation(Location location) {
    players.forEach(p -> showLocation(location, p));
  }

  public final void showLocation(Location location, Player player) {
    try {
      ComMiniPlugin.getGlowingBlocks().setGlowing(location, player, ChatColor.WHITE);
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }
  }

  public abstract T loadLocData(JsonElement element);

  public abstract JsonElement saveLocData(T data);

  @Override
  protected void generateLoadData(JsonElement dataElement) {
    val data = dataElement.getAsJsonObject();
    if (data.has("locations")) {
      data.getAsJsonArray("locations").forEach(element -> {
        val obj = element.getAsJsonObject();
        locations.put(ComMiniPlugin.gson.fromJson(obj.get("loc"), Location.class), loadLocData(obj.get("data")));
      });
    }
  }

  @Override
  protected JsonElement generateSaveData() {
    val object = new JsonObject();
    val array = new JsonArray();
    locations.forEach((loc, data) -> {
      val obj = new JsonObject();
      obj.add("loc", ComMiniPlugin.gson.toJsonTree(loc));
      obj.add("data", saveLocData(data));
      array.add(obj);
    });
    object.add("locations", array);
    return object;
  }

}
