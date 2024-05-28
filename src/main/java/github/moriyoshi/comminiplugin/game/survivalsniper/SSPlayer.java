package github.moriyoshi.comminiplugin.game.survivalsniper;

import com.google.gson.JsonObject;

import github.moriyoshi.comminiplugin.system.player.HotbarGamePlayer;
import github.moriyoshi.comminiplugin.system.player.InterfaceGamePlayer;
import lombok.Getter;
import lombok.val;

public class SSPlayer implements InterfaceGamePlayer, HotbarGamePlayer {

  @Getter
  private SSSlot slot;

  @Override
  public JsonObject generateSaveData() {
    val object = new JsonObject();
    object.add(getHotBarPath(), getHotBarJson(slot));
    return object;
  }

  @Override
  public void generateLoadData(JsonObject object) {
    this.slot = getHotBar(SSSlot.class, object.get(getHotBarPath()));
  }

}
