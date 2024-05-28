package github.moriyoshi.comminiplugin.system.player;

import com.google.gson.JsonObject;

public interface InterfaceGamePlayer {

  JsonObject generateSaveData();

  void generateLoadData(JsonObject object);
}
