package github.moriyoshi.comminiplugin.item;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CustomItemFlag {
  DISABLE_DROP("drop"),
  DISABLE_CLICK_INTERACT("click_interact"),
  DISABLE_MOVE_INV("move_inv"),
  DISABLE_ITEM_SPAWN("item_spawn");

  public final String id;
}
