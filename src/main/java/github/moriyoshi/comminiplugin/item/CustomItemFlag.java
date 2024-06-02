package github.moriyoshi.comminiplugin.item;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CustomItemFlag {
  DROP("drop"),
  CLICK_INTERACT("click_interact"),
  MOVE_INV("move_inv"),
  ITEM_SPAWN("item_spawn");

  public final String id;
}
