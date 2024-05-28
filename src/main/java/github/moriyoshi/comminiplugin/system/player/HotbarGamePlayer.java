package github.moriyoshi.comminiplugin.system.player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import github.moriyoshi.comminiplugin.ComMiniPlugin;
import github.moriyoshi.comminiplugin.system.hotbar.HotbarSlot;

public interface HotbarGamePlayer {

  default <T extends HotbarSlot> T getHotBar(Class<T> clazz, @Nullable JsonElement element) {

    if (element == null || element.isJsonNull()) {
      try {
        return clazz.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | NoSuchMethodException | SecurityException e) {
        e.printStackTrace();
      }
    }

    List<Integer> list = ComMiniPlugin.gson.fromJson(element, new TypeToken<ArrayList<Integer>>() {
    }.getType());

    try {
      return clazz.getDeclaredConstructor(Collection.class)
          .newInstance(list);
    } catch (JsonSyntaxException | InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      e.printStackTrace();
    }

    throw new IllegalArgumentException("Hotbar element is not valid");
  }

  HotbarSlot getHotbar();

  default JsonElement getHotBarJson(HotbarSlot slot) {
    return ComMiniPlugin.gson.toJsonTree(slot);
  }

  default String getHotBarPath() {
    return "hotbar";
  }
}
