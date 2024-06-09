package github.moriyoshi.comminiplugin.game.survivalsniper;

import com.google.gson.JsonObject;
import github.moriyoshi.comminiplugin.system.player.InterfaceGamePlayer;
import github.moriyoshi.comminiplugin.system.player.InventoryGamePlayer;
import lombok.Getter;
import lombok.val;

@Getter
public class SSPlayer implements InterfaceGamePlayer, InventoryGamePlayer {

  private SSSlot inventorySlot;

  @Override
  public JsonObject generateSaveData() {
    val object = new JsonObject();
    object.add(getInventorySlotPath(), getInventorySlotJson(inventorySlot));
    return object;
  }

  @Override
  public void generateLoadData(JsonObject object) {
    this.inventorySlot = getInventorySlot(SSSlot.class, object.get(getInventorySlotPath()));
  }
}
