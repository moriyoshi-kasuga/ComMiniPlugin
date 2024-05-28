package github.moriyoshi.comminiplugin.block;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.api.JsonAPI;
import github.moriyoshi.comminiplugin.constant.ComMiniPrefix;
import lombok.val;

public class CustomBlockData extends JsonAPI {

  public CustomBlockData(Plugin plugin) {
    super(plugin, "customBlockData");
  }

  @Override
  protected void generateLoadData(JsonElement dataElement) {
    val data = dataElement.getAsJsonObject();
    if (!data.has("blocks")) {
      data.add("blocks", new JsonArray());
    }
    data.getAsJsonArray("blocks").forEach(jsonElement -> {
      JsonObject object = jsonElement.getAsJsonObject();
      Location loc = ComMiniPlugin.gson.fromJson(object.getAsJsonObject("location"), Location.class);
      String identifier = object.get("identifier").getAsString();
      JsonObject blockData = object.getAsJsonObject("data");
      if (!CustomBlock.isRegister(identifier)) {
        ComMiniPrefix.SYSTEM.logError(identifier + "はCustomBlockに登録されていませんのでロードされませんでした");
        return;
      }
      CustomBlock.loadCustomBlock(identifier, loc, blockData);
    });
  }

  @Override
  protected JsonElement generateSaveData() {
    JsonObject object = new JsonObject();
    JsonArray data = new JsonArray();
    CustomBlock.getBlocks().forEach((location, customBlock) -> {
      JsonObject json = new JsonObject();
      json.add("location", ComMiniPlugin.gson.toJsonTree(location));
      json.addProperty("identifier", customBlock.getIdentifier());
      json.add("data", customBlock.getBlockData());
      customBlock.clearData(customBlock.getLocation());
      data.add(json);
    });
    object.add("blocks", data);
    return object;
  }

}
