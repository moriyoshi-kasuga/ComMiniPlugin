package github.moriyoshi.comminiplugin.api.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationSerializer implements JsonSerializer<Location>, JsonDeserializer<Location> {

  @Override
  public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject element = new JsonObject();
    src.serialize().forEach((s, o) -> element.addProperty(s, o.toString()));
    return element;
  }

  @Override
  public Location deserialize(JsonElement jsonElement, Type type,
      JsonDeserializationContext context) throws JsonParseException {
    JsonObject obj = jsonElement.getAsJsonObject();
    return new Location(Bukkit.getWorld(obj.get("world").getAsString()),
        obj.get("x").getAsDouble(), obj.get("y").getAsDouble(), obj.get("z").getAsDouble(),
        obj.get("yaw").getAsFloat(), obj.get("pitch").getAsFloat());
  }

}
