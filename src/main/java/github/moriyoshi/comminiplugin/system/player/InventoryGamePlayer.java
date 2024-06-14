package github.moriyoshi.comminiplugin.system.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import github.moriyoshi.comminiplugin.lib.PluginLib;
import github.moriyoshi.comminiplugin.system.slot.InventorySlot;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

public interface InventoryGamePlayer {

  default <T extends InventorySlot> T getInventorySlot(
      Class<T> clazz, @Nullable JsonElement element) {

    if (element == null || element.isJsonNull()) {
      try {
        return clazz.getDeclaredConstructor().newInstance();
      } catch (InstantiationException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException
          | NoSuchMethodException
          | SecurityException e) {
        e.printStackTrace();
      }
    }

    List<Integer> list =
        PluginLib.gson.fromJson(element, new TypeToken<ArrayList<Integer>>() {}.getType());

    try {
      return clazz.getDeclaredConstructor(Collection.class).newInstance(list);
    } catch (JsonSyntaxException
        | InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      e.printStackTrace();
    }

    throw new IllegalArgumentException("Inventory element is not valid");
  }

  InventorySlot getInventorySlot();

  default JsonElement getInventorySlotJson(InventorySlot slot) {
    return PluginLib.gson.toJsonTree(slot);
  }

  default String getInventorySlotPath() {
    return "inventory";
  }
}
