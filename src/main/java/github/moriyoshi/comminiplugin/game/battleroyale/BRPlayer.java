package github.moriyoshi.comminiplugin.game.battleroyale;

import com.google.gson.JsonObject;
import github.moriyoshi.comminiplugin.system.player.HotbarGamePlayer;
import github.moriyoshi.comminiplugin.system.player.InterfaceGamePlayer;
import lombok.Getter;
import lombok.val;

@Getter
public class BRPlayer implements InterfaceGamePlayer, HotbarGamePlayer {

  private BRSlot hotbarSlot;

  @Override
  public JsonObject generateSaveData() {
    val object = new JsonObject();
    object.add(getHotbarJsonPath(), getHotbarSlotJson(hotbarSlot));
    return object;
  }

  @Override
  public void generateLoadData(JsonObject object) {
    this.hotbarSlot = getHotbarSlot(BRSlot.class, object.get(getHotbarJsonPath()));
  }
}
