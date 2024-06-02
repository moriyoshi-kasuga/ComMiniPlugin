package github.moriyoshi.comminiplugin.game.battleroyale;

import com.google.gson.JsonObject;
import github.moriyoshi.comminiplugin.system.player.HotbarGamePlayer;
import github.moriyoshi.comminiplugin.system.player.InterfaceGamePlayer;
import lombok.Getter;
import lombok.val;

@Getter
public class BRPlayer implements InterfaceGamePlayer, HotbarGamePlayer {

  private BRSlot hotbar;

  @Override
  public JsonObject generateSaveData() {
    val object = new JsonObject();
    object.add(getHotBarPath(), getHotBarJson(hotbar));
    return object;
  }

  @Override
  public void generateLoadData(JsonObject object) {
    this.hotbar = getHotBar(BRSlot.class, object.get(getHotBarPath()));
  }
}
