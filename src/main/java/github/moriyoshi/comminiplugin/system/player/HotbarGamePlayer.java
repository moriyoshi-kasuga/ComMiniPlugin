package github.moriyoshi.comminiplugin.system.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import github.moriyoshi.comminiplugin.lib.PluginLib;
import github.moriyoshi.comminiplugin.system.slot.HotbarSlot;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

public interface HotbarGamePlayer {

  default <T extends HotbarSlot> T getHotbarSlot(Class<T> clazz, @Nullable JsonElement element) {

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

    throw new IllegalArgumentException("Hotbar element is not valid");
  }

  HotbarSlot getHotbarSlot();

  default JsonElement getHotbarSlotJson(HotbarSlot slot) {
    return PluginLib.gson.toJsonTree(slot);
  }

  default String getHotbarJsonPath() {
    return "hotbar";
  }
}
