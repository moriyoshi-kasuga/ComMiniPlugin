package github.moriyoshi.comminiplugin.item;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CustomItemFlag {
  DISABLE_DROP("drop"),
  DISABLE_CLICK_INTERACT("click_interact"),
  DISABLE_MOVE_INV("move_inv"),
  DISABLE_MOVE_OTHER_INV("move_other_inv"),
  DISABLE_ATTACK("attack"),
  DISABLE_ATTACK_TO_PLAYER("attack_to_player"),
  DISABLE_ITEM_SPAWN("item_spawn");

  public final String id;
}
