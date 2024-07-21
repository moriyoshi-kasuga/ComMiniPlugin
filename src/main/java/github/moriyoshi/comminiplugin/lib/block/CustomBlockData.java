package github.moriyoshi.comminiplugin.lib.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.moriyoshi.comminiplugin.lib.InterfaceAPIPlugin;
import github.moriyoshi.comminiplugin.lib.JsonAPI;
import github.moriyoshi.comminiplugin.lib.PluginLib;
import lombok.val;
import org.bukkit.Location;

public class CustomBlockData extends JsonAPI {

  private JsonArray arr;

  public CustomBlockData(final InterfaceAPIPlugin plugin) {
    super(plugin, "customBlockData");
  }

  @Override
  protected void generateLoadData(final JsonElement dataElement) {
    arr = new JsonArray();
    val data = dataElement.getAsJsonObject();
    if (!data.has("blocks")) {
      data.add("blocks", new JsonArray());
    }
    data.getAsJsonArray("blocks")
        .forEach(
            jsonElement -> {
              final JsonObject object = jsonElement.getAsJsonObject();
              final String identifier = object.get("identifier").getAsString();
              if (!CustomBlock.isRegister(identifier)) {
                plugin
                    .getSystemPrefix()
                    .logError(identifier + "はCustomBlockに登録されていませんのでロードされませんでした");
                arr.add(object);
                return;
              }
              final Location loc = PluginLib.gson.fromJson(object.get("location"), Location.class);
              final JsonElement blockData = object.get("data");
              CustomBlock.loadCustomBlock(identifier, loc, blockData);
            });
  }

  @Override
  protected JsonElement generateSaveData() {
    final JsonObject object = new JsonObject();
    CustomBlock.getBlocks()
        .forEach(
            (location, customBlock) -> {
              final JsonObject json = new JsonObject();
              val element = customBlock.getBlockData();
              if (element == null || element.isJsonNull()) {
                customBlock.clearData();
                return;
              }
              json.add("data", element);
              json.add("location", PluginLib.gson.toJsonTree(location));
              json.addProperty("identifier", customBlock.getIdentifier());
              arr.add(json);
              customBlock.clearData();
            });
    object.add("blocks", arr);
    return object;
  }
}
