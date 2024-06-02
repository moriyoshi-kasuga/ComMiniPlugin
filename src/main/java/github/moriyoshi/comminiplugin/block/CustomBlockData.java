package github.moriyoshi.comminiplugin.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.api.JsonAPI;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import lombok.Getter;
import lombok.val;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class CustomBlockData extends JsonAPI {

  @Getter(lazy = true)
  private static final CustomBlockData instance = new CustomBlockData(ComMiniPlugin.getPlugin());

  public CustomBlockData(Plugin plugin) {
    super(plugin, "customBlockData");
  }

  @Override
  protected void generateLoadData(JsonElement dataElement) {
    val data = dataElement.getAsJsonObject();
    if (!data.has("blocks")) {
      data.add("blocks", new JsonArray());
    }
    data.getAsJsonArray("blocks")
        .forEach(
            jsonElement -> {
              JsonObject object = jsonElement.getAsJsonObject();
              String identifier = object.get("identifier").getAsString();
              if (!CustomBlock.isRegister(identifier)) {
                ComMiniPrefix.SYSTEM.logError(identifier + "はCustomBlockに登録されていませんのでロードされませんでした");
                return;
              }
              Location loc = ComMiniPlugin.gson.fromJson(object.get("location"), Location.class);
              JsonElement blockData = object.get("data");
              CustomBlock.loadCustomBlock(identifier, loc, blockData);
            });
  }

  @Override
  protected JsonElement generateSaveData() {
    JsonObject object = new JsonObject();
    JsonArray data = new JsonArray();
    CustomBlock.getBlocks()
        .forEach(
            (location, customBlock) -> {
              JsonObject json = new JsonObject();
              val element = customBlock.getBlockData();
              if (element == null || element.isJsonNull()) {
                customBlock.clearData();
                return;
              }
              json.add("data", element);
              json.add("location", ComMiniPlugin.gson.toJsonTree(location));
              json.addProperty("identifier", customBlock.getIdentifier());
              data.add(json);
              customBlock.clearData();
            });
    object.add("blocks", data);
    return object;
  }
}
