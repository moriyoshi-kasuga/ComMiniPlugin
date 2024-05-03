package github.moriyoshi.comminiplugin.api.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Base64;
import org.bukkit.inventory.ItemStack;

public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

  @Override
  public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(Base64.getEncoder().encodeToString(src.serializeAsBytes()));
  }

  @Override
  public ItemStack deserialize(JsonElement jsonElement, Type type,
      JsonDeserializationContext context) throws JsonParseException {
    return ItemStack.deserializeBytes(Base64.getDecoder().decode(jsonElement.getAsString()));
  }

}
