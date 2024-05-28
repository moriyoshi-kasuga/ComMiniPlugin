package github.moriyoshi.comminiplugin.game.survivalsniper;

import com.google.gson.JsonObject;

import github.moriyoshi.comminiplugin.system.player.HotbarGamePlayer;
import github.moriyoshi.comminiplugin.system.player.InterfaceGamePlayer;
import lombok.Getter;
import lombok.val;

public class SSPlayer implements InterfaceGamePlayer, HotbarGamePlayer {

  @Getter
  private SSSlot hotbar;

  @Override
  public JsonObject generateSaveData() {
    val object = new JsonObject();
    object.add(getHotBarPath(), getHotBarJson(hotbar));
    return object;
  }

  @Override
  public void generateLoadData(JsonObject object) {
    this.hotbar = getHotBar(SSSlot.class, object.get(getHotBarPath()));
  }

}
